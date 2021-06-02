# station.localization
This project was created to get single place for localization strings used in Ruuvi Station application for both platforms at once: iOS and Android. This should help to enforce unified user experience. 

## Supported languages 
- English
- Finnish
- Sweden
- French
- Russian

If you want to add new language and ready to help with translation send email to denis@ruuvi.com. We'll make preparations for new language. 

## Repository structure

#### File station.localization.json
Main file of repository containig all localized strings

#### Directory localize.converter.android
Tool we use to convert from *station.localization.json* to *string.xml* files for Android project

## Branches
**master** - main branch for confirmed translations
**dev** - working branch. If you want to participate in translation project you should fork from this branch.

## Structure of station.localization.json
File consist of one root `translations` element with many childs. Each child is a translation of one specific string. If string is used in Android application `ident_android` will be filled. Same for iOS and `ident_ios`. These 2 fields are being filled by developers and should not be modified.

You can modify `en` - for English, `fi` - for Finnish, `ru` - for Russian, `sv` - for Sweden and `fr` - for French. 

    {
    	"translations": [
    		{
    			"ident_ios": "Menu.Label.AppSettings.text",
    			"ident_android": "menu_app_settings",
    			"en": "App Settings",
    			"fi": "Asetukset",
    			"ru": "Настройки",
    			"sv": "App Inställningar",
    			"fr": "Réglages"
    		},
    		...
    	]
    }

If original English string contain some strange chars like `{%@^%1$s}` or `%1$s` - don't worry just leave it as is. It's a template where application will insert value during runtime. But you can change the position of this template if grammar rules of your language demand it. 
Example:

    {
      "ident_ios": "",
      "ident_android": "time_since",
      "en": "{%@^%1$s} ago",
      "fi": "{%@^%1$s} sitten",
      "ru": "{%@^%1$s} назад",
      "sv": "{%@^%1$s} sedan",
      "fr": "il y'a {%@^%1$s}"
    }

###### Remark for developers about using value template
If specific string is used only in one platform you can leave your template as is: `%@` or `%1$s`. But if it used by Android and iOS at the same time please use format `{iOS_Pattern^Android_Pattern}`. 

## How to contribute
1. Fork from https://github.com/ruuvi/station.localization
2. Make changes to station.localization.json in dev branch
3. Create pull request to apply your changes

## Get in touch

Join our [Telegram](https://t.me/ruuvicom) community. Feel free to ask ``@morgan_ru`` about Localization project. 
