package info.anodsplace.weblists

import DatabaseDriverFactory
import HtmlClientFactory
import android.app.Application
import android.content.Context
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import info.anodsplace.weblists.backup.Backup
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.db.WebListsDb
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        val sqlDriver = DatabaseDriverFactory(get()).createDriver()
        AppDatabase(WebListsDb(sqlDriver))
    }
    single { Preferences(get<Context>()) }
    single { HtmlClientFactory().create() }
    single { Yaml(configuration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property
    )) }
    single { Backup(get()) }
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