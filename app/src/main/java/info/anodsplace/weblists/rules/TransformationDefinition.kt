package info.anodsplace.weblists.rules

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
