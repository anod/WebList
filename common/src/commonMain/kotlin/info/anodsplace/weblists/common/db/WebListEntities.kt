package info.anodsplace.weblists.common.db

import info.anodsplace.weblists.common.HtmlDocument
import info.anodsplace.weblists.common.HtmlElements
import androidx.compose.ui.text.AnnotatedString
import info.anodsplace.weblists.common.extensions.toBool
import info.anodsplace.weblists.common.rules.ElementTransformation
import info.anodsplace.weblists.common.rules.FilterTransformation
import info.anodsplace.weblists.db.DbWebList
import info.anodsplace.weblists.db.DbWebSite
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class WebSite(
    @Transient
    val id: Long = 0,
    val url: String,
    val title: String,
) {
    constructor(dbRow: DbWebSite) : this(
        dbRow.id, dbRow.url, dbRow.title
    )

    companion object {
        val empty =  WebSite(0L, "", "")
    }
}

@Serializable
data class WebList(
    @Transient // kotlin.serialization
    val id: Long = 0,
    @Transient // kotlin.serialization
    val siteId: Long = 0,
    @Transient // kotlin.serialization
    val order: Int = 0,
    val cssQuery: String,
    val horizontal: Boolean,
    val apply: TransformationContainer
) {
    constructor(siteId: Long, order: Int, cssQuery: String, transformations: List<ElementTransformation>, isHorizontal: Boolean = false)
        : this(0, siteId, order, cssQuery, isHorizontal, TransformationContainer(transformations))

    constructor(siteId: Long, order: Int, cssQuery: String, transformation: ElementTransformation, isHorizontal: Boolean = false)
            : this(0, siteId, order, cssQuery, isHorizontal, TransformationContainer(transformation))

    constructor(dbList: DbWebList) : this(
        dbList.id,
        dbList.siteId,
        dbList.order.toInt(),
        dbList.cssQuery,
        dbList.horizontal.toBool(),
        TransformationContainer.decode(dbList.apply)
    )

    fun apply(elements: HtmlElements): List<AnnotatedString> {
        val result = mutableListOf<AnnotatedString>()
        for (element in elements) {
            for (transformation in apply.transformations) {
                val values = transformation.apply(element)
                if (transformation is FilterTransformation) {
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

class WebSection(
    val isHorizontal: Boolean,
    val list: List<AnnotatedString>
)

@Serializable
data class WebSiteLists(
    // @Embedded
    val site: WebSite,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "siteId"
//    )
    val lists: List<WebList>
) {

    constructor(siteId: Long) :
            this(WebSite(siteId, "", ""), emptyList())

    companion object {
        val empty =  WebSiteLists(0L)
    }

    private val listsOrdered: List<WebList>
        get() = lists.sortedWith { l, r ->
             if (l.order == r.order) {
                 l.id.compareTo(r.id)
             } else {
                 l.order.compareTo(r.order)
             }
        }

    fun apply(doc: HtmlDocument): List<WebSection> {
        val list = mutableListOf<WebSection>()
        for (rule in listsOrdered) {
            val elements = doc.select(rule.cssQuery)
            list.add(WebSection(rule.horizontal, rule.apply(elements)))
        }
        return list
    }
}