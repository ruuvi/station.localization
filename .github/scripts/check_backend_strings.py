#!/usr/bin/env python3
"""Validate backend string files (backend/**/*.json).

Errors fail the check; missing translations are reported as warnings.
Output uses GitHub Actions annotations so problems show up on the PR diff.
"""
import json
import re
import sys
from pathlib import Path

LANGUAGES = ('en', 'fi', 'sv', 'de', 'fr', 'pl')
KEY_PATTERN = re.compile(r'^[a-z][A-Za-z0-9]*$')
PLACEHOLDER = re.compile(r'\{\{\{?\s*([^}]+?)\s*\}?\}\}')

errors = 0
warnings = 0


def error(path, message):
    global errors
    errors += 1
    print(f'::error file={path}::{message}')


def warning(path, message):
    global warnings
    warnings += 1
    print(f'::warning file={path}::{message}')


def check_file(path):
    try:
        data = json.loads(path.read_text(encoding='utf-8'))
    except (ValueError, UnicodeDecodeError) as exc:
        error(path, f'Invalid JSON: {exc}')
        return
    if not isinstance(data, dict) or not data:
        error(path, 'Expected a non-empty object of "key": {"<language>": "text"}')
        return
    for key, translations in data.items():
        if not KEY_PATTERN.match(key):
            error(path, f'Key "{key}" must be camelCase letters and digits')
        if not isinstance(translations, dict):
            error(path, f'"{key}" must be an object of language codes to strings')
            continue
        unknown = sorted(set(translations) - set(LANGUAGES))
        if unknown:
            error(path, f'"{key}" has unknown language codes: {", ".join(unknown)}')
        for lang, text in translations.items():
            if not isinstance(text, str) or not text.strip():
                error(path, f'"{key}" ({lang}) must be a non-empty string')
        english = translations.get('en')
        if not isinstance(english, str) or not english.strip():
            error(path, f'"{key}" is missing English (en), which is the fallback for every language')
            continue
        # Every language must use the same placeholders, or the backend renders a broken string.
        expected = sorted(PLACEHOLDER.findall(english))
        for lang, text in translations.items():
            if lang != 'en' and isinstance(text, str) and sorted(PLACEHOLDER.findall(text)) != expected:
                error(path, f'"{key}" ({lang}) placeholders {sorted(PLACEHOLDER.findall(text))} differ from English {expected}')
        missing = [lang for lang in LANGUAGES if lang not in translations]
        if missing:
            warning(path, f'"{key}" is missing {", ".join(missing)} (English is used instead)')


def main():
    files = sorted(Path('backend').rglob('*.json'))
    for path in files:
        check_file(path)
    print(f'Checked {len(files)} backend string files: {errors} errors, {warnings} warnings')
    return 1 if errors else 0


if __name__ == '__main__':
    sys.exit(main())
