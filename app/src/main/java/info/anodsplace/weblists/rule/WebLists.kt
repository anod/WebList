package info.anodsplace.weblists.rule

import androidx.compose.ui.text.AnnotatedString
import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

@Serializable
class TransformationDefinition(
    val type: String,
    val parameter: String,
    val transformation: TransformationDefinition? = null
) {
    constructor(type: String, parameter: String) :
            this(type, parameter, null)

    constructor(type: String, parameter: String, transformations: () -> TransformationDefinition) :
            this(type, parameter, transformations())

    fun create(): ElementTransformation {
        return factories[type]!!.create(this)
    }

    interface Factory {
        fun create(def: TransformationDefinition): ElementTransformation
    }

    companion object {
        private val factories: Map<String, Factory> = mapOf(
            FilterTransformation.type to FilterTransformation.Factory(),
            CssTransformation.type to CssTransformation.Factory(),
            ConcatTransformation.type to ConcatTransformation.Factory(),
            StyleTransformation.type to StyleTransformation.Factory(),
            ConstTransformation.type to ConstTransformation.Factory(),
            TextTransformation.type to TextTransformation.Factory()
        )
    }
}

@Serializable
class TransformationContainer(
    var list: List<TransformationDefinition>
) {
    class Converters {
        @TypeConverter
        fun decode(value: String): TransformationContainer {
            return Json.decodeFromString(value)
        }

        @TypeConverter
        fun encode(container: TransformationContainer): String {
            return Json.encodeToString(container)
        }
    }
}

@Entity(tableName = "web_list",)
data class WebList(
    @PrimaryKey val id: Int,
    @ForeignKey(
        entity = WebSite::class,
        parentColumns = ["id"],
        childColumns = ["siteId"],
        onDelete = ForeignKey.CASCADE
    ) val siteId: Int,
    val order: Int,
    val cssQuery: String,
    val transformations: TransformationContainer,
    val isHorizontal: Boolean
) {
    constructor(siteId: Int, order: Int, cssQuery: String, transformations: List<ElementTransformation>, isHorizontal: Boolean = false)
        : this(0, siteId, order, cssQuery, TransformationContainer(transformations.map { it.definition() }), isHorizontal)

    constructor(siteId: Int, order: Int, cssQuery: String, transformation: ElementTransformation, isHorizontal: Boolean = false)
            : this(0, siteId, order, cssQuery, TransformationContainer(listOf(transformation.definition())), isHorizontal)

    fun apply(elements: Elements): List<AnnotatedString> {
        val result = mutableListOf<AnnotatedString>()
        for (element in elements) {
            for (def in transformations.list) {
                val transformation = def.create()
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

class WebSection(val isHorizontal: Boolean, val list: List<AnnotatedString>)

@Entity(tableName = "web_site")
class WebSite(
    @PrimaryKey val id: Int,
    val url: String,
    val title: String,
)

data class WebSiteLists(
    @Embedded val site: WebSite,
    @Relation(
        parentColumn = "id",
        entityColumn = "siteId"
    )
    val lists: List<WebList>
) {

    val listsOrdered: List<WebList>
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