package info.anodsplace.weblists.common

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import info.anodsplace.weblists.db.WebListsDb.Companion.Schema
import java.lang.IllegalArgumentException

actual fun formatString(format: String, vararg args: Any?): String = String.format(format, *args)

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Schema.create(driver)
        return driver
    }
}

actual fun parseColor(hexStr: String): Int {
    if (hexStr[0] == '#') {
        // Use a long to avoid rollovers on #ffXXXXXX
        var color: Long = hexStr.substring(1).toLong(16)
        if (hexStr.length == 7) {
            // Set the alpha value
            color = color or -0x1000000
        } else require(hexStr.length == 9) { "Unknown color" }
        return color.toInt()
    }
    throw IllegalArgumentException("Unknown color")
}

actual fun isValidUrl(url: String): Boolean {
    return url.startsWith("http", ignoreCase = true)
}