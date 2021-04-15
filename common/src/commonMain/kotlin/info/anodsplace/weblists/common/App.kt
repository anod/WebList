package info.anodsplace.weblists.common

import com.squareup.sqldelight.db.SqlDriver
import org.koin.core.module.Module

expect fun formatString(format: String, vararg args: Any?): String
expect fun parseColor(hexStr: String): Int
expect fun isValidUrl(url: String): Boolean
expect fun createAppModule(): Module

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}