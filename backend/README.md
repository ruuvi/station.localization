# Ruuvi Cloud strings

Texts that Ruuvi Cloud sends to users, such as emails. App strings are in
[`station.localization.json`](../station.localization.json).

Each message has its own file, for example `email/account_delete.json`. Every key maps to the text in each language:

```json
{
  "title": {
    "en": "Delete your account?",
    "fi": "Poistetaanko tilisi?"
  }
}
```

## Translating

* Languages: `en`, `fi`, `sv`, `de`, `fr`, `pl`. English is required and is used when a translation is missing.
* Keep placeholders such as `{{minutes}}` exactly as they are. You can move them within the sentence.
* In French, write `\u00a0` (a non-breaking space) instead of the space before `?`, `!`, `:` and `;`, so the punctuation
  never wraps onto a line of its own.
* Use plain text only, and the same terms as the Ruuvi Station app.
* Keep texts about as short as the English. Buttons and titles have little space on phones.
* Don't rename or remove keys. New keys are added by the Ruuvi team together with the code that uses them.

## Checks

Every push and pull request runs a check that fails on missing English, unknown language codes or broken placeholders,
and warns about missing translations. Run it locally from the repository root:

```
python3 .github/scripts/check_backend_strings.py
```
