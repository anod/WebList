package info.anodsplace.weblists.common

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.sqlite.db.SupportSQLiteDatabase
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.common.theme.WebListTheme
import info.anodsplace.weblists.db.WebListsDb
import info.anodsplace.weblists.db.WebListsDb.Companion.Schema
import io.ktor.client.*
import org.koin.core.logger.Logger
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun formatString(format: String, vararg args: Any?): String = String.format(format, *args)
actual fun parseColor(hexStr: String): Int = android.graphics.Color.parseColor(hexStr)
actual fun isValidUrl(url: String): Boolean = url.isValidUrl()

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            Schema,
            context,
            "web_lists.db",
            callback = object : AndroidSqliteDriver.Callback(Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA foreign_keys=ON;");
                }
            })
    }
}

actual fun createAppModule(): Module = module {
    single {
        val sqlDriver = DatabaseDriverFactory(get()).createDriver()
        AppDatabase(WebListsDb(sqlDriver))
    }
    single<Logger> { AndroidLogger() }
    single<AppPreferences> { Preferences(get<Context>()) }
    single<HtmlParser> { JsoupParser() }
    single<HtmlClient> { HtmlClientNetwork(HttpClient(), get()) }
    single { Yaml(configuration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property
    )
    ) }
    single { AndroidExporter(get(), get()) }
}

@Composable
fun AndroidApp(content: @Composable() () -> Unit) {
    WebListTheme(darkTheme = isSystemInDarkTheme()) {
        content()
    }
}