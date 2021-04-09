package info.anodsplace.weblists.common.rules

import HtmlElement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.common.extensions.ColorAsHexSerializer
import info.anodsplace.weblists.joinAnnotatedString
import info.anodsplace.weblists.toAnnotatedString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ElementTransformation {
    open fun apply(element: HtmlElement): List<AnnotatedString> = emptyList()
}

object AnnotationAttributes {
    const val tag = "web-list"
    const val header = "header"

    fun header(builder: AnnotatedString.Builder) {
        builder.addStringAnnotation(tag, header, 0, 0)
    }

    fun header(string: AnnotatedString): AnnotatedString {
        return with(AnnotatedString.Builder(string)) {
            header(this)
            toAnnotatedString()
        }
    }
}

@Serializable
@SerialName(TextTransformation.type)
class TextTransformation: ElementTransformation() {
    companion object {
        internal const val type = "text"
    }

    override fun apply(element: HtmlElement): List<AnnotatedString> {
        return listOf(element.text().toAnnotatedString())
    }
}

@Serializable
@SerialName(ConstTransformation.type)
class ConstTransformation(private val params: Param): ElementTransformation() {
    companion object {
        const val type = "const"
    }

    constructor(text: String, spanStyle: SpanStyle) : this(Param(text, StyleParameters(spanStyle)))

    @Serializable
    data class Param(val text: String, val style: StyleParameters? = null)

    override fun apply(element: HtmlElement): List<AnnotatedString> = listOf(
        params.text.toAnnotatedString(params.style?.toSpanStyle())
    )
}

@Serializable
data class StyleParameters @OptIn(ExperimentalUnsignedTypes::class) constructor(
    @Serializable(with = ColorAsHexSerializer::class)
    val color: Color? = null,
    val fontSize: Float? = null,
    val fontWeight: Int? = null,
    val letterSpacing: Float? = null,
    @Serializable(with = ColorAsHexSerializer::class)
    val background: Color? = null,
    val textDecoration: Int? = null,
    val annotations: List<String> = emptyList()
) {
    constructor(spanStyle: SpanStyle, annotations: List<String> = emptyList()) : this(
        color = spanStyle.color,
        fontSize = if (spanStyle.fontSize == TextUnit.Unspecified) null else spanStyle.fontSize.value,
        fontWeight = spanStyle.fontWeight?.weight,
        letterSpacing = if (spanStyle.letterSpacing == TextUnit.Unspecified) null else spanStyle.letterSpacing.value,
        background = spanStyle.background,
        textDecoration = spanStyle.textDecoration?.mask,
        annotations = annotations
    )

    fun toSpanStyle() = SpanStyle(
        color = color ?: Color.Unspecified,
        fontSize = fontSize?.sp ?: TextUnit.Unspecified,
        fontWeight = if (fontWeight == null) null else FontWeight(fontWeight),
        letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
        background = background ?: Color.Unspecified,
        textDecoration = when(textDecoration) {
            null -> null
            0x0 -> TextDecoration.None
            0x1 -> TextDecoration.Underline
            0x2 -> TextDecoration.LineThrough
            else -> null
        }
    )
}

@Serializable
@SerialName(StyleTransformation.type)
class StyleTransformation(private val params: StyleParameters): ElementTransformation() {
    companion object {
        internal const val type = "style"
    }

    constructor(spanStyle: SpanStyle) : this(StyleParameters(spanStyle))

    constructor(
        color: Color = Color.Unspecified,
        fontSize: TextUnit = TextUnit.Unspecified,
        fontWeight: FontWeight? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
        background: Color = Color.Unspecified,
        textDecoration: TextDecoration? = null,
        annotations: List<String> = emptyList()
    ) : this(
        StyleParameters(SpanStyle(
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        background = background,
        textDecoration = textDecoration
    ), annotations)
    )

    override fun apply(element: HtmlElement): List<AnnotatedString> {
        return listOf(with(AnnotatedString.Builder()) {
            if (params.annotations.isNotEmpty()) {
                for (annotation in params.annotations) {
                    addStringAnnotation(AnnotationAttributes.tag, annotation, 0, 0)
                }
            }
            pushStyle(params.toSpanStyle())
            append(element.text())
            pop()

            toAnnotatedString()
        })
    }
}

@Serializable
@SerialName(ConcatTransformation.type)
class ConcatTransformation(private val params: Param): ElementTransformation() {
    companion object {
        internal const val type = "concat"
    }

    @Serializable
    data class Param(val values: List<ElementTransformation>, val separator: String )

    constructor(values: List<ElementTransformation>, separator: String = "\n")
            : this(Param(values, separator))

    override fun apply(element: HtmlElement): List<AnnotatedString> {
        return listOf(params.values.flatMap { it.apply(element) }.joinAnnotatedString(params.separator))
    }
}

@Serializable
@SerialName(CssTransformation.type)
class CssTransformation(private val params: Param): ElementTransformation() {
    companion object {
        internal const val type = "css"
    }

    constructor(cssQuery: String) : this(Param(cssQuery, TextTransformation()))

    constructor(cssQuery: String, transformation: () -> ElementTransformation) : this(Param(cssQuery, transformation()))

    @Serializable
    data class Param(val cssQuery: String, val transformation: ElementTransformation)

    override fun apply(element: HtmlElement): List<AnnotatedString> {
        val result = mutableListOf<AnnotatedString>()
        val elements = element.select(params.cssQuery)
        val transformation = params.transformation
        for (current in elements) {
            result.addAll(transformation.apply(current))
        }
        return result
    }
}

@Serializable
@SerialName(FilterTransformation.type)
class FilterTransformation(private val params: Param): ElementTransformation() {
    companion object {
        internal const val type = "filter"
    }

    constructor(cssQuery: String, values: List<String>, excludes: Boolean)
        : this(Param(cssQuery, values, excludes))

    @Serializable
    data class Param(val cssQuery: String, val values: List<String>, val excludes: Boolean)

    override fun apply(element: HtmlElement): List<AnnotatedString> {
        val text = element.selectFirst(params.cssQuery).text()
        val filter = if (params.excludes) {
            params.values.contains(text)
        } else {
            !params.values.contains(text)
        }
        return if (filter) emptyList() else listOf(text.toAnnotatedString())
    }
}