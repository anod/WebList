package info.anodsplace.weblists.common

import androidx.compose.runtime.Composable
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.common.export.Exporter
import info.anodsplace.weblists.common.export.Importer
import info.anodsplace.weblists.common.theme.WebListTheme
import info.anodsplace.weblists.db.WebListsDb
import info.anodsplace.weblists.db.WebListsDb.Companion.Schema
import io.ktor.client.*
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.PrintLogger
import org.koin.core.module.Module
import org.koin.dsl.module
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

actual fun createAppModule(): Module = module {
    single {
        val sqlDriver = DatabaseDriverFactory().createDriver()
        AppDatabase(WebListsDb(sqlDriver))
    }
    single<Logger> { PrintLogger(Level.DEBUG) }
    single<AppPreferences> { Preferences() }
    single<HtmlParser> { JsoupParser() }
    single<HtmlClient> { HtmlClientNetwork(HttpClient(), get()) }
    single { Yaml(configuration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property
    )
    ) }
    single<Exporter> { DesktopExporter(get()) }
    single<Importer> { DesktopImporter(get(), get())}
}

@Composable
fun DesktopApp(viewModel: AppViewModel) {
    WebListTheme(darkTheme = true) {
        MainScreen(viewModel, strings = StringProvider.Default())
    }
}