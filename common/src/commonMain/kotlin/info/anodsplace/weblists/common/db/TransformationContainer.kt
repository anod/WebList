package info.anodsplace.weblists.common.db

import info.anodsplace.weblists.common.rules.ElementTransformation
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class TransformationContainer(
    val transformations: List<ElementTransformation>
) {
    constructor(single: ElementTransformation) : this(listOf(single))

    fun encode(): String {
        return encode(this)
    }

    companion object {
        fun decode(value: String): TransformationContainer {
            return Json.decodeFromString(value)
        }
        fun encode(container: TransformationContainer): String {
            return Json.encodeToString(container)
        }
    }
}
