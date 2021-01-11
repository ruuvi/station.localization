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
        val writer = PrintWriter("../../com.ruuvi.station.github/app/src/main/res/values/strings.xml")
        val writerFi = PrintWriter("../../com.ruuvi.station.github/app/src/main/res/values-fi/strings.xml")
        val writerSv = PrintWriter("../../com.ruuvi.station.github/app/src/main/res/values-sv/strings.xml")
        val writerRu = PrintWriter("../../com.ruuvi.station.github/app/src/main/res/values-ru/strings.xml")

        startFile(writer)
        startFile(writerFi)
        startFile(writerSv)
        startFile(writerRu)

        for (entry in translations.sortedBy { it.ident_android }) {
            if (entry.ident_android.isNotEmpty()) entry.export(writer, writerFi, writerSv, writerRu)
        }

        closeFile(writer)
        closeFile(writerFi)
        closeFile(writerSv)
        closeFile(writerRu)
    }

    fun startFile(writer: PrintWriter) {
        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        writer.println("<resources>")
    }

    fun closeFile(writer: PrintWriter) {
        writer.println("</resources>")
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
