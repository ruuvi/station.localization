package domain

import model.TranslationString
import model.Translations
import java.io.File
import java.nio.file.Paths

object IosImporter {
    fun import(): Translations {
        val result = Translations(mutableListOf())
        readFileLineByLineUsingForEachLine(
            result,
            Paths.get("src\\data\\en.lproj\\Localizable.strings").toAbsolutePath().toString(),
            ::setEn
        )
        readFileLineByLineUsingForEachLine(
            result,
            Paths.get("src\\data\\fi.lproj\\Localizable.strings").toAbsolutePath().toString(),
            ::setFi
        )
        readFileLineByLineUsingForEachLine(
            result,
            Paths.get("src\\data\\ru.lproj\\Localizable.strings").toAbsolutePath().toString(),
            ::setRu
        )
        readFileLineByLineUsingForEachLine(
            result,
            Paths.get("src\\data\\sv.lproj\\Localizable.strings").toAbsolutePath().toString(),
            ::setSv
        )
        return result
    }

    private fun readFileLineByLineUsingForEachLine(
        target: Translations,
        fileName: String,
        setFunc: (TranslationString, String) -> Unit)
            = File(fileName).forEachLine { line ->
        val reg = Regex("(?<=^\")(.+?)(?=\")|(?<=\\=\\s\")(.+)(?=\";.*\$)")
        val results = reg.findAll(line)
        if (results.count() == 2) {
            val ident = results.elementAt(0)
            val value = results.elementAt(1)

            var translation = target.translations.firstOrNull { it.ident_ios == ident.value}
            if (translation == null) {
                translation = TranslationString(ident.value, "","","","","")
                target.translations.add(translation)
            }
            setFunc(translation, value.value)
        }
    }

    private fun setEn(translation: TranslationString, value: String) {
        translation.en = value
    }

    private fun setFi(translation: TranslationString, value: String) {
        translation.fi = value
    }

    private fun setRu(translation: TranslationString, value: String) {
        translation.ru = value
    }

    private fun setSv(translation: TranslationString, value: String) {
        translation.sv = value
    }
}