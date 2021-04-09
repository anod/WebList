package info.anodsplace.weblists.common.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import formatString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import parseColor

object ColorAsHexSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ColorHex", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Color) {
        if (value == Color.Unspecified) {
            encoder.encodeNull()
        } else {
            val argb = value.toArgb()
            val hexStr = formatString("#%08X", argb)
            encoder.encodeString(hexStr)
        }
    }

    override fun deserialize(decoder: Decoder): Color {
        val hexStr = decoder.decodeString()
        if (hexStr.isEmpty()) {
            return Color.Unspecified
        }
        return try {
            val intValue = parseColor(hexStr)
            Color(intValue)
        } catch (e: IllegalArgumentException) {
            Color.Unspecified
        }
    }
}