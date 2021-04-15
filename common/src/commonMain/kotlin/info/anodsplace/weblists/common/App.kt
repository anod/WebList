package info.anodsplace.weblists.common

import com.squareup.sqldelight.db.SqlDriver

expect fun formatString(format: String, vararg args: Any?): String
expect fun parseColor(hexStr: String): Int
expect fun isValidUrl(url: String): Boolean

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}