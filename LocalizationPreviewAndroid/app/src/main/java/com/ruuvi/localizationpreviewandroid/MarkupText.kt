package com.ruuvi.localizationpreviewandroid

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.apache.commons.text.StringEscapeUtils

@Composable
fun MarkupText(rawEscaped: String) {

    val linkColor = Color(0xFF35AD9F)
    val headerColor = Color.White
    val textColor = Color(0xCCFFFFFF)

    //val raw = StringEscapeUtils.unescapeHtml4(sanitizeMarkup(rawEscaped))

    val ruuviStationFontsSizes = RuuviStationFontSizes()
    val ruuviStationFonts = RuuviStationFonts()

    val raw = remember(rawEscaped) { sanitizeMarkup(rawEscaped) }


    val parsed = remember(raw) {
        parseModernMarkup(
            input = raw,
            tagStyles = mapOf(
                "title" to SpanStyle(
                    fontSize = ruuviStationFontsSizes.normal,
                    fontFamily = ruuviStationFonts.mulishBold,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                ),
                "b" to SpanStyle(
                    fontSize = ruuviStationFontsSizes.compact,
                    fontFamily = ruuviStationFonts.mulishBold,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                "link" to SpanStyle(
                    color = linkColor,
                    fontFamily = ruuviStationFonts.mulishBold,
                    fontSize = ruuviStationFontsSizes.compact,
                    textDecoration = TextDecoration.Underline
                )
            ),
            defaultStyle = SpanStyle(
                color = textColor,
                fontFamily = ruuviStationFonts.mulishRegular,
                fontSize = ruuviStationFontsSizes.compact
            ),
            )
    }

    BasicText(text = parsed)
}

private fun sanitizeMarkup(source: String): String {
    // 1) Replace {from^to} → keep "to"
    val step1 = Regex("\\{(.+?)\\^(.+?)\\}").replace(source) { m -> m.groupValues[2] }

    // 2) Unescape HTML (&amp; → &, &quot; → ")
    val step2 = org.apache.commons.text.StringEscapeUtils.unescapeHtml4(step1)

    // 3) If we STILL have double-escaped sequences, normalize them.
    // Detect by checking presence of literal '\n' without any real newline.
    val hasLiteralEscapes = "\\n" in step2 && !step2.contains('\n')

    val step3 = if (hasLiteralEscapes) {
        step2
            .replace("\\\\", "\\")   // \\ → \
            .replace("\\\"", "\"")   // \" → "
            .replace("\\n", "\n")    // \n → newline
            .replace("\\t", "\t")    // \t → tab (optional)
    } else {
        step2
    }

    // 4) Optional: treat [br] or <br> as newline (if your content uses it)
    return step3
        .replace("[br]", "\n")
        .replace("<br>", "\n")
}


data class RuuviStationFontSizes(
    val tiny: TextUnit = 9.5.sp,
    val petite: TextUnit = 11.sp,
    val miniature: TextUnit = 12.sp,
    val small: TextUnit = 14.sp,
    val compact: TextUnit = 15.sp,
    val normal: TextUnit = 16.sp,
    val extended: TextUnit = 18.sp,
    val big: TextUnit = 20.sp,
    val huge: TextUnit = 32.sp,
    val bigValue: TextUnit = 56.sp
)

data class RuuviStationFonts constructor(
    val mulishRegular: FontFamily = FontFamily(Font(R.font.mulish_regular)),
    val mulishBold: FontFamily = FontFamily(Font(R.font.mulish_bold)),
    val mulishExtraBold: FontFamily = FontFamily(Font(R.font.mulish_extrabold)),
    val mulishSemiBoldItalic: FontFamily = FontFamily(Font(R.font.mulish_semibolditalic)),

    val oswaldRegular: FontFamily = FontFamily(Font(R.font.oswald_regular)),
    val oswaldBold: FontFamily = FontFamily(Font(R.font.oswald_bold_ttf)),

    val montserratBold: FontFamily = FontFamily(Font(R.font.montserrat_bold_ttf)),
    val montserratRegular: FontFamily = FontFamily(Font(R.font.montserrat_regular)),
    val montserratExtraBold: FontFamily = FontFamily(Font(R.font.montserrat_extra_bold_ttf)),
)

fun parseModernMarkup(
    input: String,
    tagStyles: Map<String, SpanStyle>,
    defaultStyle: SpanStyle? = null
): AnnotatedString {
    val builder = AnnotatedString.Builder()


    var cursor = 0
    val tagRegex = Regex("""\[(\w+)(?:\s+url\s*=\s*"([^"]+)")?]""")

    while (cursor < input.length) {
        val match = tagRegex.find(input, cursor)
        if (match == null) {
            // Append remaining text
            val remaining = input.substring(cursor)
            if (remaining.isNotEmpty()) {
                defaultStyle?.let { builder.pushStyle(it) }
                builder.append(remaining)
                if (defaultStyle != null) builder.pop()
            }
            break
        }

        val tagStart = match.range.first
        val tagEnd = match.range.last + 1
        val tag = match.groupValues[1]
        val url = match.groupValues.getOrNull(2).takeIf { it?.isNotBlank() == true }

        // Append plain text before tag
        if (tagStart > cursor) {
            val plain = input.substring(cursor, tagStart)
            defaultStyle?.let { builder.pushStyle(it) }
            builder.append(plain)
            if (defaultStyle != null) builder.pop()
        }

        // Find corresponding closing tag
        val closingTag = "[/$tag]"
        val closeIndex = input.indexOf(closingTag, tagEnd)
        if (closeIndex == -1) {
            // Malformed tag: treat as plain text
            val fallback = input.substring(tagStart, tagEnd)
            defaultStyle?.let { builder.pushStyle(it) }
            builder.append(fallback)
            if (defaultStyle != null) builder.pop()
            cursor = tagEnd
            continue
        }

        val content = input.substring(tagEnd, closeIndex)
        val style = tagStyles[tag]

        when {
            tag == "link" && url != null -> {
                builder.withLink(
                    LinkAnnotation.Url(url, TextLinkStyles(style = style ?: SpanStyle())),
                ) {
                    append(content)
                }
            }
            style != null -> {
                builder.pushStyle(style)
                builder.append(content)
                builder.pop()
            }
            else -> {
                // Unknown tag, fallback to default style
                defaultStyle?.let { builder.pushStyle(it) }
                builder.append(content)
                if (defaultStyle != null) builder.pop()
            }
        }

        cursor = closeIndex + closingTag.length
    }

    return builder.toAnnotatedString()
}
