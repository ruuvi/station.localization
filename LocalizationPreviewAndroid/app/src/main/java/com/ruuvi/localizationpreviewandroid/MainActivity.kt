package com.ruuvi.localizationpreviewandroid

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruuvi.localizationpreviewandroid.ui.theme.LocalizationPreviewAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FastLocalizerApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastLocalizerApp() {
    val scope = rememberCoroutineScope()

    var branches by remember { mutableStateOf(listOf("master", "dev")) }
    var selectedBranch by remember { mutableStateOf("dev") }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val branchJsonCache = remember { mutableStateMapOf<String, JSONObject>() }
    var entries by remember { mutableStateOf<List<TranslationEntry>>(emptyList()) }

    var ident by remember { mutableStateOf("") }
    var foundJson by remember { mutableStateOf<JSONObject?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var searchHasFocus by remember { mutableStateOf(false) }
    var suggestionsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching {
            isLoading = true
            branches = fetchBranches("ruuvi", "station.localization")
            if (selectedBranch !in branches) selectedBranch = branches.firstOrNull() ?: "master"
        }
        isLoading = false
    }

    LaunchedEffect(selectedBranch) {
        if (branchJsonCache[selectedBranch] == null) {
            isLoading = true
            error = null
            runCatching {
                val root = fetchLocalizationJson(
                    owner = "ruuvi",
                    repo = "station.localization",
                    branch = selectedBranch,
                    path = "station.localization.json"
                )
                branchJsonCache[selectedBranch] = root
                entries = buildIndex(root)
                foundJson = null
            }.onFailure {
                error = it.message ?: it.toString()
                entries = emptyList()
                foundJson = null
            }
            isLoading = false
        } else {
            entries = buildIndex(branchJsonCache.getValue(selectedBranch))
            foundJson = null
        }
    }

    val filtered = remember(searchQuery, entries) {
        if (searchQuery.isBlank()) emptyList()
        else entries.asSequence()
            .filter { it.ident.contains(searchQuery, ignoreCase = true) }
            .take(20).toList()
    }

    LaunchedEffect(searchHasFocus, searchQuery, filtered) {
        suggestionsExpanded = searchHasFocus && filtered.isNotEmpty()
    }

    MaterialTheme (colorScheme = darkColorScheme()) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF083C3D)) {
            Column(
                Modifier
                    .systemBarsPadding()
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Ruuvi Localization Quick Viewer",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                // Branch selector
                var branchMenu by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = branchMenu,
                    onExpandedChange = { branchMenu = !branchMenu }
                ) {
                    TextField(
                        value = selectedBranch,
                        onValueChange = {},
                        label = { Text("Branch") },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(branchMenu) }
                    )
                    ExposedDropdownMenu(
                        expanded = branchMenu,
                        onDismissRequest = { branchMenu = false }
                    ) {
                        branches.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b) },
                                onClick = {
                                    selectedBranch = b
                                    branchMenu = false
                                }
                            )
                        }
                    }
                }

                // Search field with suggestions DROPPING BELOW (doesn't cover the field)
                ExposedDropdownMenuBox(
                    expanded = suggestionsExpanded,
                    onExpandedChange = { /* we control expansion manually */ }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search ident_android (e.g., co2)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { suggestionsExpanded = false }
                        ),
                        modifier = Modifier
                            .menuAnchor() // anchor for the dropdown
                            .fillMaxWidth()
                            .onFocusChanged { searchHasFocus = it.isFocused }
                    )

                    ExposedDropdownMenu(
                        expanded = suggestionsExpanded,
                        onDismissRequest = { suggestionsExpanded = false }
                    ) {
                        filtered.forEach { e ->
                            DropdownMenuItem(
                                text = { Text(e.ident) },
                                onClick = {
                                    searchQuery = e.ident
                                    ident = e.ident
                                    foundJson = e.obj
                                    suggestionsExpanded = false
                                }
                            )
                        }
                    }
                }

                if (error != null) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }

                // Details only (removed bottom matches area)
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                    foundJson?.let { obj ->
                        val fields = listOf("ident_android","en","fi","ru","sv","fr","de")
                        fields.forEach { key ->
                            if (obj.has(key)) KeyValue(key, obj.optString(key))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Raw JSON", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(obj.toString(2), style = MaterialTheme.typography.bodySmall, color = Color.White)
                    } ?: Text("Type to search and pick an ident.", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun KeyValue(key: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(key, style = MaterialTheme.typography.labelMedium, color = Color.White)
        if (key == "ident_android") {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        } else {
            MarkupText(value)
        }
        Divider(Modifier.padding(vertical = 8.dp))
    }
}

/* ---------------- networking + parsing ---------------- */

private suspend fun fetchBranches(owner: String, repo: String): List<String> =
    withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/$owner/$repo/branches")
        (url.openConnection() as HttpURLConnection).run {
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "RuuviLocalizationViewer/1.3 (Android)")
            inputStream.bufferedReader().use(BufferedReader::readText)
        }.let { body ->
            val arr = JSONArray(body)
            buildList {
                for (i in 0 until arr.length()) {
                    val name = arr.getJSONObject(i).optString("name")
                    if (name.isNotBlank()) add(name)
                }
            }
        }
    }

private suspend fun fetchLocalizationJson(
    owner: String,
    repo: String,
    branch: String,
    path: String
): JSONObject = withContext(Dispatchers.IO) {
    // Contents API → jsDelivr → raw
    runCatching {
        val apiUrl = "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
        val body = httpGetText(apiUrl)
        val json = JSONObject(body)
        val encoded = json.optString("content")
        if (encoded.isNotBlank()) {
            val bytes = Base64.decode(encoded.replace("\n", ""), Base64.DEFAULT)
            return@withContext JSONObject(String(bytes, Charsets.UTF_8))
        }
        val direct = json.optString("download_url")
        if (direct.isNotBlank()) return@withContext JSONObject(httpGetText(direct))
    }.getOrElse { /* try CDN */ }

    runCatching {
        val cdn = "https://cdn.jsdelivr.net/gh/$owner/$repo@$branch/$path"
        return@withContext JSONObject(httpGetText(cdn))
    }.getOrElse { /* try raw */ }

    val raw = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
    JSONObject(httpGetText(raw))
}

private fun httpGetText(urlStr: String): String {
    val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
        connectTimeout = 10000
        readTimeout = 10000
        requestMethod = "GET"
        setRequestProperty("User-Agent", "RuuviLocalizationViewer/1.3 (Android)")
        setRequestProperty("Accept", "application/json")
    }
    return conn.inputStream.bufferedReader().use(BufferedReader::readText).also { conn.disconnect() }
}

private fun buildIndex(root: JSONObject): List<TranslationEntry> {
    val arr = root.optJSONArray("translations") ?: return emptyList()
    val list = ArrayList<TranslationEntry>(arr.length())
    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val ident = obj.optString("ident_android")
        if (ident.isNotBlank()) list.add(TranslationEntry(ident, obj))
    }
    return list.sortedBy { it.ident.lowercase() }
}

private fun findByIdentAndroid(root: JSONObject, identAndroid: String): JSONObject? {
    if (identAndroid.isBlank()) return null
    val translations = root.optJSONArray("translations") ?: return null
    for (i in 0 until translations.length()) {
        val obj = translations.getJSONObject(i)
        if (obj.optString("ident_android") == identAndroid) return obj
    }
    return null
}

private data class TranslationEntry(val ident: String, val obj: JSONObject)