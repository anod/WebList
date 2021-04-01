package info.anodsplace.weblists

fun CharSequence.findAll(
    string: String,
    startIndex: Int = 0,
    ignoreCase: Boolean = false,
    last: Boolean = false,
    minLength: Int = 3
): List<Int> {
    val trimmed = string.trim()
    if (trimmed.isEmpty() || trimmed.length < minLength) {
        return emptyList()
    }

    val result = mutableListOf<Int>()

    var index = startIndex
    do {
        val loc = if (!last)
            indexOf(string, index, ignoreCase)
        else
            lastIndexOf(string, index, ignoreCase)

        if (loc < 0) {
            return result
        }
        result.add(loc)
        index = loc + string.length
    } while (index < length)

    return result
}