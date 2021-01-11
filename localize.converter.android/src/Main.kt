import model.Translations

fun main() {
    exportForAndroid()
}

fun exportForAndroid() {
    val translations = Translations.loadFromFile("..\\station.localization.json")
    translations.exportForAndroid()
}