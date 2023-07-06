package model

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import java.io.FileReader
import java.io.PrintWriter

data class Translations(
    val translations: MutableList<TranslationString>
) {
    fun writeToFile(filename: String) {
        val writer = PrintWriter(filename)
        val gsonPretty = GsonBuilder().setPrettyPrinting().create()

        val json = gsonPretty.toJson(this)
        writer.append(json)
        writer.close()
    }

    fun exportForAndroid() {

        val writers = mapOf(
            "en" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values/strings.xml"),
            "fi" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values-fi/strings.xml"),
            "sv" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values-sv/strings.xml"),
            "ru" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values-ru/strings.xml"),
            "fr" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values-fr/strings.xml"),
            "de" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values-de/strings.xml"),
            "pl" to PrintWriter("../../com.ruuvi.station/app/src/main/res/values-pl/strings.xml")
        )

        for (writer in writers.values) {
            startFile(writer)
        }

        for (entry in translations.sortedBy { it.ident_android }) {
            println(entry.ident_android)
            if (entry.ident_android.isNotEmpty()) entry.export(writers)
        }

        for (writer in writers.values) {
            closeFile(writer)
        }
    }

    fun startFile(writer: PrintWriter) {
        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        writer.println("<resources>")
    }

    fun closeFile(writer: PrintWriter) {
        writer.print("</resources>")
        writer.close()
    }

    companion object {
        fun loadFromFile(filename: String): Translations {
            val gsonPretty = GsonBuilder().setPrettyPrinting().create()
            val reader = JsonReader(FileReader(filename))
            return gsonPretty.fromJson<Translations>(reader, Translations::class.java)
        }
    }
}
