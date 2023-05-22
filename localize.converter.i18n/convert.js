var fs = require('fs');
var obj = JSON.parse(fs.readFileSync('../station.localization.json', 'utf8'));

// languages we want
var getLangs = ["en","fi","sv","fr","de"];

var output = {};
for (var i = 0; i < getLangs.length; i++) {
    output[getLangs[i]] = {translation: {}}
}
for (var i = 0; i < obj.translations.length; i++) {
    var t = obj.translations[i];
    for (var j = 0; j < getLangs.length; j++) {
        if (t.ident_webui) {
            if (t[getLangs[j]]) {
                output[getLangs[j]].translation[t.ident_webui] = t[getLangs[j]]
            } else {
                console.log("MISSING " + t.ident_webui +" in " + getLangs[j])
            }
        }
    }
}

fs.writeFileSync("localization_i18n.json",JSON.stringify(output, null, 2), 'utf8');
