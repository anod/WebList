package info.anodsplace.weblists.rule

import androidx.compose.ui.text.AnnotatedString
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class TransformationDefinition(
    val id: Int,
    val type: String,
    val parameter: String,
    val transformation: ElementTransformation?
) {
    constructor(type: String, parameter: String) :
            this(0, type, parameter, null)

    constructor(type: String, parameter: String, transformations: () -> ElementTransformation) :
            this(0, type, parameter, transformations())

    fun create(): ResultTransformation {
        return create(this)
    }

    interface Factory {
        fun create(def: TransformationDefinition): ResultTransformation
    }

    companion object {
        private val factories: Map<String, Factory> = mapOf(
            FilterResultTransformation.type to FilterResultTransformation.Factory(),
            CssResultTransformation.type to CssResultTransformation.Factory(),
        )
        fun create(def: TransformationDefinition) = factories[def.type]!!.create(def)
    }
}

class WebList(
    val id: Int,
    val cssQuery: String,
    val transformations: List<TransformationDefinition>,
    val isHorizontal: Boolean = false
) {
    fun apply(elements: Elements): List<AnnotatedString> {
        val result = mutableListOf<AnnotatedString>()
        for (element in elements) {
            for (def in transformations) {
                val transformation = def.create()
                val values = transformation.apply(element)
                if (transformation is FilterResultTransformation) {
                    if (values.isEmpty()) {
                        break
                    }
                } else {
                    result.addAll(values)
                }
            }
        }
        return result
    }
}

class WebSection(val isHorizontal: Boolean, val list: List<AnnotatedString>)

class WebLists(
    val id: Int,
    val url: String,
    val title: String,
    val lists: List<WebList>
) {
    fun apply(doc: Document): List<WebSection> {
        val list = mutableListOf<WebSection>()
        for (rule in lists) {
            val elements = doc.select(rule.cssQuery)
            list.add(WebSection(rule.isHorizontal, rule.apply(elements)))
        }
        return list
    }
}