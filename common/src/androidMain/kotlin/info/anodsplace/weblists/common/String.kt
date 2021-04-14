package info.anodsplace.weblists.common

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun String.isValidUrl(): Boolean {
    if (isEmpty()) {
        return false
    }
    return this.toHttpUrlOrNull() != null
}