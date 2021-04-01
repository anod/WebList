package info.anodsplace.weblists

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import info.anodsplace.weblists.rules.AnnotationAttributes
import org.jsoup.nodes.Element

fun Element.toAnnotatedString(): AnnotatedString {
    return AnnotatedString.Builder(this.text()).toAnnotatedString()
}

fun AnnotatedString.asListHeader(): AnnotatedString  {
    return AnnotationAttributes.header(this)
}

fun String.toAnnotatedString(spanStyle: SpanStyle? = null): AnnotatedString {
    return AnnotatedString.Builder().let {
        if (spanStyle != null) {
            it.pushStyle(spanStyle)
            it.append(this)
            it.pop()
        } else {
            it.append(this)
        }
        it.toAnnotatedString()
    }
}

fun Iterable<AnnotatedString>.joinAnnotatedString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "..."): AnnotatedString {
    val builder =  AnnotatedString.Builder()
    builder.append(prefix.toString())
    var count = 0
    for (element in this) {
        if (++count > 1) builder.append(separator.toString())
        if (limit < 0 || count <= limit) {
            builder.append(element)
        } else break
    }
    if (limit >= 0 && count > limit) builder.append(truncated.toString())
    builder.append(postfix.toString())
    return builder.toAnnotatedString()
}