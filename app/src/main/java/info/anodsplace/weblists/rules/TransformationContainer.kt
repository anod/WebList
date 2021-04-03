package info.anodsplace.weblists.rules

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class TransformationContainer(
    var list: List<ElementTransformation>
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
