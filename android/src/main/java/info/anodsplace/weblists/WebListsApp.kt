package info.anodsplace.weblists

import info.anodsplace.weblists.common.DatabaseDriverFactory
import android.app.Application
import android.content.Context
import android.util.Log
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import info.anodsplace.weblists.backup.AndroidExporter
import info.anodsplace.weblists.common.AppPreferences
import info.anodsplace.weblists.common.HtmlParser
import info.anodsplace.weblists.common.JsoupParser
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.db.WebListsDb
import io.ktor.client.*
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.dsl.bind
import org.koin.dsl.module

class AndroidLogger : Logger(Level.DEBUG) {

    override fun log(level: Level, msg: MESSAGE) {
        when(level) {
            Level.DEBUG -> Log.d("WebLists", msg)
            Level.INFO -> Log.i("WebLists", msg)
            Level.ERROR -> Log.e("WebLists", msg)
            Level.NONE -> { }
        }
    }
}

val appModule = module {
    single {
        val sqlDriver = DatabaseDriverFactory(get()).createDriver()
        AppDatabase(WebListsDb(sqlDriver))
    }
    single<Logger> { AndroidLogger() }
    single<AppPreferences> { Preferences(get<Context>()) }
    single<HtmlParser> { JsoupParser() }
    single { Yaml(configuration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property
    )) }
    single { AndroidExporter(get(), get()) }
}

class WebListsApp: Application() {

    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin{
            koin.loadModules(listOf(module {
                single<Context> { this@WebListsApp } bind Application::class
            }))
            modules(appModule)
        }
    }
}