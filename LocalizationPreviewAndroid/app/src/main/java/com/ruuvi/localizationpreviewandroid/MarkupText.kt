package com.ruuvi.localizationpreviewandroid

import androidx.annotation.StringRes
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.apache.commons.text.StringEscapeUtils

val ruuviStationFontsSizes = RuuviStationFontSizes()
val ruuviStationFonts = RuuviStationFonts()
val linkColor = Color(0xFF35AD9F)
val headerColor = Color.White
val textColor = Color(0xCCFFFFFF)
@Composable
fun MarkupText(rawEscaped: String) {

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
            paragraphStyles = mapOf(
                "li" to ParagraphStyle(
                    textIndent = TextIndent(firstLine = 0.sp, restLine = 20.sp),
                ),
                "li2" to ParagraphStyle(
                    textIndent = TextIndent(firstLine = 20.sp, restLine = 40.sp),
                ),
            ),
            defaultStyle = SpanStyle(
                color = textColor,
                fontFamily = ruuviStationFonts.mulishRegular,
                fontSize = ruuviStationFontsSizes.compact
            ),

            )
    }

    Text(
        text = parsed.text,
        inlineContent = parsed.inlineContent
    )
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
    paragraphStyles: Map<String, ParagraphStyle> = emptyMap(),
    defaultStyle: SpanStyle? = null
): ParsedMarkup  {
    val builder = AnnotatedString.Builder()
    val inlineContent = mutableMapOf<String, InlineTextContent>()

    // Provide a single reusable inline "bullet" gutter
    val bulletId = "bullet"
    val bulletId2 = "bullet2"

    inlineContent[bulletId] = InlineTextContent(
        Placeholder(
            width = 20.sp,
            height = ruuviStationFontsSizes.compact,
            placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
        )
    ) {
        Text(
            text = "•",
            style = LocalTextStyle.current.merge(
                TextStyle(
                    fontFamily = ruuviStationFonts.mulishBold,
                    fontWeight = FontWeight.Bold,
                    fontSize = ruuviStationFontsSizes.compact,
                    color = defaultStyle?.color ?: textColor
                )
            )
        )
    }

    inlineContent[bulletId2] = InlineTextContent(
        Placeholder(
            width = 20.sp,
            height = ruuviStationFontsSizes.compact,
            placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
        )
    ) {
        Text(
            text = "◦",
            style = LocalTextStyle.current.merge(
                TextStyle(
                    fontFamily = ruuviStationFonts.mulishBold,
                    fontWeight = FontWeight.Bold,
                    fontSize = ruuviStationFontsSizes.compact,
                    color = defaultStyle?.color ?: textColor
                )
            )
        )
    }


    var cursor = 0
    val tagRegex = Regex("""\[(\w+)(?:\s+url\s*=\s*(?:"([^"]+)"|([^\]\s]+)))?]""")

    fun appendWithDefault(text: String) {
        if (text.isEmpty()) return
        if (defaultStyle != null) builder.pushStyle(defaultStyle)
        builder.append(text)
        if (defaultStyle != null) builder.pop()
    }

    while (cursor < input.length) {
        val match = tagRegex.find(input, cursor)
        if (match == null) {
            appendWithDefault(input.substring(cursor))
            break
        }

        val tagStart = match.range.first
        val tagEnd = match.range.last + 1
        val tag = match.groupValues[1]
        val url = match.groups[2]?.value?.takeIf { it.isNotBlank() }
            ?: match.groups[3]?.value?.takeIf { it.isNotBlank() }

        if (tagStart > cursor) appendWithDefault(input.substring(cursor, tagStart))

        val closingTag = "[/$tag]"
        val closeIndex = input.indexOf(closingTag, tagEnd)
        if (closeIndex == -1) {
            appendWithDefault(input.substring(tagStart, tagEnd))
            cursor = tagEnd
            continue
        }

        val content = input.substring(tagEnd, closeIndex)
        val spanStyle = tagStyles[tag]

        when {
            tag == "link" && url != null -> {
                builder.withLink(LinkAnnotation.Url(url, TextLinkStyles(style = spanStyle ?: SpanStyle()))) {
                    append(content)
                }
            }

            tag == "li" -> {
                val paraStart = builder.length

                builder.appendInlineContent(bulletId, "[•]")

                val inner = parseModernMarkup(
                    input = content,
                    tagStyles = tagStyles,
                    paragraphStyles = paragraphStyles,
                    defaultStyle = defaultStyle
                )

                builder.append(inner.text)
                inlineContent.putAll(inner.inlineContent)

                val paraEnd = builder.length
                paragraphStyles["li"]?.let { ps ->
                    builder.addStyle(ps, paraStart, paraEnd)
                }
            }

            tag == "li2" -> {
                val paraStart = builder.length

                builder.appendInlineContent(bulletId2, "[◦]")

                val inner = parseModernMarkup(
                    input = content,
                    tagStyles = tagStyles,
                    paragraphStyles = paragraphStyles,
                    defaultStyle = defaultStyle
                )

                builder.append(inner.text)
                inlineContent.putAll(inner.inlineContent)

                val paraEnd = builder.length
                paragraphStyles["li2"]?.let { ps ->
                    builder.addStyle(ps, paraStart, paraEnd)
                }
            }

            spanStyle != null -> {
                builder.pushStyle(spanStyle)
                builder.append(content)
                builder.pop()
            }

            else -> appendWithDefault(content)
        }

        cursor = closeIndex + closingTag.length
    }

    return ParsedMarkup(builder.toAnnotatedString(), inlineContent)
}

data class ParsedMarkup(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent> = emptyMap()
)