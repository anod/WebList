package info.anodsplace.weblists.rule

import androidx.compose.ui.text.AnnotatedString
import info.anodsplace.weblists.toAnnotatedString
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element

interface ResultTransformation {
    val def: TransformationDefinition
    fun apply(element: Element): List<AnnotatedString>
}

class CssResultTransformation(override val def: TransformationDefinition): ResultTransformation {

    companion object {
        const val type = "css"
        fun def(cssQuery: String): TransformationDefinition {
            return TransformationDefinition(type, cssQuery)
        }
        fun def(cssQuery: String, transformation: () -> ElementTransformation): TransformationDefinition {
            return TransformationDefinition(type, cssQuery, transformation)
        }
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition) = CssResultTransformation(def)
    }

    override fun apply(element: Element): List<AnnotatedString> {
        val transformation = def.transformation ?: TextTransformation()
        return CssTransformation(def.parameter, transformation).apply(element)
    }
}

@Serializable
data class CssFilterValues(val cssQuery: String, val values: List<String>, val excludes: Boolean)

class FilterResultTransformation(override val def: TransformationDefinition): ResultTransformation {

    companion object {
        const val type = "filter"
        fun def(cssQuery: String, values: List<String>, excludes: Boolean): TransformationDefinition {
            val param = Json.encodeToString(CssFilterValues(cssQuery, values, excludes))
            return TransformationDefinition(0, type, param, null)
        }
    }

    class Factory : TransformationDefinition.Factory {
        override fun create(def: TransformationDefinition) = FilterResultTransformation(def)
    }

    override fun apply(element: Element): List<AnnotatedString> {
        val param: CssFilterValues = Json.decodeFromString(def.parameter)
        val text = element.selectFirst(param.cssQuery).text()
        val filter = if (param.excludes) {
            param.values.contains(text)
        } else {
            !param.values.contains(text)
        }
        return if (filter) emptyList() else listOf(text.toAnnotatedString())
    }
}
