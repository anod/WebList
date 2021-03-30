package info.anodsplace.weblists.rules

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.joinAnnotatedString
import info.anodsplace.weblists.toAnnotatedString
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element

interface ElementTransformation {
    fun apply(element: Element): List<AnnotatedString>
    fun definition(): TransformationDefinition
}

class TextTransformation: ElementTransformation {
    companion object {
        internal const val type = "text"
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition): ElementTransformation {
            return TextTransformation()
        }
    }

    override fun apply(element: Element): List<AnnotatedString> {
        return listOf(element.text().toAnnotatedString())
    }

    override fun definition() = TransformationDefinition(type, "{}")
}

class ConstTransformation(private val params: Param): ElementTransformation {
    companion object {
        const val type = "const"
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition): ElementTransformation {
            val param: Param = Json.decodeFromString(def.parameter)
            return ConstTransformation(param)
        }
    }

    constructor(text: String, spanStyle: SpanStyle) : this(Param(text, StyleParameters(spanStyle)))

    constructor(text: String) : this(Param(text, null))

    @Serializable
    data class Param(val text: String, val spanStyle: StyleParameters? = null)

    override fun apply(element: Element): List<AnnotatedString> = listOf(
        params.text.toAnnotatedString(params.spanStyle?.toSpanStyle())
    )

    override fun definition() = TransformationDefinition(type, Json.encodeToString(params))
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
    constructor(spanStyle: SpanStyle) : this(
        color = if (spanStyle.color == Color.Unspecified) null else spanStyle.color.value,
        fontSize = if (spanStyle.fontSize == TextUnit.Unspecified) null else spanStyle.fontSize.value,
        fontWeight = spanStyle.fontWeight?.weight,
        letterSpacing = if (spanStyle.letterSpacing == TextUnit.Unspecified) null else spanStyle.letterSpacing.value,
        background = if (spanStyle.background == Color.Unspecified) null else spanStyle.background.value,
        textDecoration = spanStyle.textDecoration?.mask
    )

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
        internal const val type = "style"
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition): ElementTransformation {
            val param: StyleParameters = Json.decodeFromString(def.parameter)
            return StyleTransformation(param)
        }
    }

    constructor(spanStyle: SpanStyle) : this(StyleParameters(spanStyle))

    override fun apply(element: Element): List<AnnotatedString> {
        return listOf(with(AnnotatedString.Builder()) {
            pushStyle(params.toSpanStyle())
            append(element.text())
            pop()
            toAnnotatedString()
        })
    }

    override fun definition() = TransformationDefinition(type, Json.encodeToString(params))
}

class ConcatTransformation(private val params: Param): ElementTransformation {
    companion object {
        internal const val type = "concat"
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition): ElementTransformation {
            val param: Param = Json.decodeFromString(def.parameter)
            return ConcatTransformation(param)
        }
    }

    @Serializable
    data class Param(val values: List<TransformationDefinition>, val separator: String )

    constructor(values: List<ElementTransformation>, separator: String = "\n")
            : this(Param(values.map { it.definition() }, separator))

    override fun apply(element: Element): List<AnnotatedString> {
        return listOf(params.values.flatMap { it.create().apply(element) }.joinAnnotatedString(params.separator))
    }

    override fun definition() = TransformationDefinition(type, Json.encodeToString(params))
}

class CssTransformation(private val params: Param): ElementTransformation {
    companion object {
        internal const val type = "css"
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition): ElementTransformation {
            val param: Param = Json.decodeFromString(def.parameter)
            return CssTransformation(param)
        }
    }

    constructor(cssQuery: String) : this(Param(cssQuery, TextTransformation().definition()))

    constructor(cssQuery: String, transformation: () -> ElementTransformation) : this(Param(cssQuery, transformation().definition()))

    @Serializable
    data class Param(val cssQuery: String, val transformation: TransformationDefinition)

    override fun apply(element: Element): List<AnnotatedString> {
        val result = mutableListOf<AnnotatedString>()
        val elements = element.select(params.cssQuery)
        val transformation = params.transformation.create()
        for (current in elements) {
            result.addAll(transformation.apply(current))
        }
        return result
    }

    override fun definition() = TransformationDefinition(type, Json.encodeToString(params))
}

class FilterTransformation(private val params: Param): ElementTransformation {
    companion object {
        internal const val type = "filter"
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition): ElementTransformation {
            val param: Param = Json.decodeFromString(def.parameter)
            return FilterTransformation(param)
        }
    }

    constructor(cssQuery: String, values: List<String>, excludes: Boolean)
        : this(Param(cssQuery, values, excludes))

    @Serializable
    data class Param(val cssQuery: String, val values: List<String>, val excludes: Boolean)

    override fun apply(element: Element): List<AnnotatedString> {
        val text = element.selectFirst(params.cssQuery).text()
        val filter = if (params.excludes) {
            params.values.contains(text)
        } else {
            !params.values.contains(text)
        }
        return if (filter) emptyList() else listOf(text.toAnnotatedString())
    }

    override fun definition() = TransformationDefinition(type, Json.encodeToString(params))
}