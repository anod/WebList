package info.anodsplace.weblists.rules

import androidx.compose.ui.text.AnnotatedString
import androidx.room.*
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

@Entity(tableName = "web_site")
data class WebSite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
)

@Entity(
    tableName = "web_list",
    foreignKeys = [
        ForeignKey(
            entity = WebSite::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("siteId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siteId")]
)
data class WebList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val order: Int,
    val cssQuery: String,
    val isHorizontal: Boolean,
    val apply: TransformationContainer
    ) {
    constructor(siteId: Long, order: Int, cssQuery: String, transformations: List<ElementTransformation>, isHorizontal: Boolean = false)
        : this(0, siteId, order, cssQuery, isHorizontal, TransformationContainer(transformations))

    constructor(siteId: Long, order: Int, cssQuery: String, transformation: ElementTransformation, isHorizontal: Boolean = false)
            : this(0, siteId, order, cssQuery, isHorizontal, TransformationContainer(transformation))

    fun apply(elements: Elements): List<AnnotatedString> {
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

data class WebSiteLists(
    @Embedded val site: WebSite,
    @Relation(
        parentColumn = "id",
        entityColumn = "siteId"
    )
    val lists: List<WebList>
) {
    private val listsOrdered: List<WebList>
        get() = lists.sortedWith { l, r ->
             if (l.order == r.order) {
                 l.id.compareTo(r.id)
             } else {
                 l.order.compareTo(r.order)
             }
        }

    fun apply(doc: Document): List<WebSection> {
        val list = mutableListOf<WebSection>()
        for (rule in listsOrdered) {
            val elements = doc.select(rule.cssQuery)
            list.add(WebSection(rule.isHorizontal, rule.apply(elements)))
        }
        return list
    }
}