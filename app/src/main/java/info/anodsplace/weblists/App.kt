package info.anodsplace.weblists

import android.app.Application
import android.content.Context
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.create(get()) }
    single { Preferences(get<Context>()) }
}

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin{
            koin.loadModules(listOf(module {
                single<Context> { this@App } bind Application::class
            }))
            modules(appModule)
        }
    }
}