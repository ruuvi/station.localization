package model

import java.io.PrintWriter

data class TranslationString (
    val ident_ios: String,
    val ident_android: String,
    var en: String,
    var fi: String,
    var ru: String,
    var sv: String,
    var fr: String
) {
    fun export(
        writer: PrintWriter,
        writerFi: PrintWriter,
        writerSv: PrintWriter,
        writerRu: PrintWriter,
        writerFr: PrintWriter
    ) {
        if (en.isNotEmpty()) writer.println(prepareString(en))
        if (fi.isNotEmpty()) writerFi.println(prepareString(fi))
        if (sv.isNotEmpty()) writerSv.println(prepareString(sv))
        if (ru.isNotEmpty()) writerRu.println(prepareString(ru))
        if (fr.isNotEmpty()) writerFr.println(prepareString(fr))
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
        return "<string name=\"$ident_android\">$result</string>"
    }
}
