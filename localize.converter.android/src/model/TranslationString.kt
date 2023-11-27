package model

import java.io.PrintWriter

data class TranslationString (
    val ident_ios: String,
    val ident_android: String,
    var en: String,
    var fi: String,
    var ru: String,
    var sv: String,
    var fr: String,
    var de: String,
    var pl: String
) {
    fun export(
        writers: Map<String, PrintWriter>
    ) {
        if (en.isNotEmpty()) writers["en"]?.println(prepareString(en))
        if (fi.isNotEmpty()) writers["fi"]?.println(prepareString(fi))
        if (sv.isNotEmpty()) writers["sv"]?.println(prepareString(sv))
        if (ru.isNotEmpty()) writers["ru"]?.println(prepareString(ru))
        if (fr.isNotEmpty()) writers["fr"]?.println(prepareString(fr))
        if (de.isNotEmpty()) writers["de"]?.println(prepareString(de))
        if (pl.isNotEmpty()) writers["pl"]?.println(prepareString(pl))
    }

    private fun prepareString(sourceString: String): String {
        var result = sourceString
        result = result.replace("'","\\'")
        //result = result.replace("\"","\\\"")
        result = result.replace("\u0026","\\u0026")
        //result = result.replace("\n","\\n")

        val reg = Regex("\\{(.+?)\\^(.+?)\\}")
        val formated = reg.findAll(result)
        for (entry in formated){
            if (entry.groupValues.size == 3) {
                result = result.replaceFirst(entry.value, entry.groupValues[2])
            }
        }
        return "    <string name=\"$ident_android\">$result</string>"
    }
}
