package info.anodsplace.weblists.rule

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.joinAnnotatedString
import info.anodsplace.weblists.toAnnotatedString
import kotlinx.serialization.Serializable
import org.jsoup.nodes.Element

interface ElementTransformation {
    fun apply(element: Element): List<AnnotatedString>
}

class TextTransformation: ElementTransformation {
    override fun apply(element: Element): List<AnnotatedString> {
        return listOf(element.text().toAnnotatedString())
    }
}

@Serializable
data class StyleParameters @OptIn(ExperimentalUnsignedTypes::class) constructor(
    val color: ULong? = null,
    val fontSize: Float? = null,
    val fontWeight: Int? = null,
    val letterSpacing: Float? = null,
    val background: ULong? = null,
    val textDecoration: Int? = null,
) {
    fun toSpanStyle() = SpanStyle(
            color = if (color == null) Color.Unspecified else Color(color),
            fontSize = fontSize?.sp ?: TextUnit.Unspecified,
            fontWeight = if (fontWeight == null) null else FontWeight(fontWeight),
            letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
            background = if (background == null) Color.Unspecified else Color(background),
            textDecoration = when(textDecoration) {
                null -> null
                0x0 -> TextDecoration.None
                0x1 -> TextDecoration.Underline
                0x2 -> TextDecoration.LineThrough
                else -> null
            }
        )
}

class StyleTransformation(private val params: StyleParameters): ElementTransformation {
    companion object {
        fun span(spanStyle: SpanStyle): ElementTransformation {
            return StyleTransformation(StyleParameters(
                color = if (spanStyle.color == Color.Unspecified) null else spanStyle.color.value,
                fontSize = if (spanStyle.fontSize == TextUnit.Unspecified) null else spanStyle.fontSize.value,
                fontWeight = spanStyle.fontWeight?.weight,
                letterSpacing = if (spanStyle.letterSpacing == TextUnit.Unspecified) null else spanStyle.letterSpacing.value,
                background = if (spanStyle.background == Color.Unspecified) null else spanStyle.background.value,
                textDecoration = spanStyle.textDecoration?.mask
            ))
        }
    }

    override fun apply(element: Element): List<AnnotatedString> {
        return listOf(with(AnnotatedString.Builder()) {
            pushStyle(params.toSpanStyle())
            append(element.text())
            pop()
            toAnnotatedString()
        })
    }
}


class ConcatTransformation(private val values: List<ElementTransformation>, private val separator: String = "\n"): ElementTransformation {
    override fun apply(element: Element): List<AnnotatedString> {
        return listOf(values.flatMap { it.apply(element) }.joinAnnotatedString(separator))
    }
}


class CssTransformation(private val cssQuery: String, private val transformation: ElementTransformation): ElementTransformation {

    constructor(cssQuery: String) : this(cssQuery, TextTransformation())

    constructor(cssQuery: String, transformation: () -> ElementTransformation) : this(cssQuery, transformation())

    override fun apply(element: Element): List<AnnotatedString> {
        val result = mutableListOf<AnnotatedString>()
        val elements = element.select(cssQuery)
        for (current in elements) {
            result.addAll(transformation.apply(current))
        }
        return result
    }
}