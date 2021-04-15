package info.anodsplace.weblists

import android.app.Application
import android.content.Context
import info.anodsplace.weblists.common.createAppModule
import io.ktor.client.*
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

class WebListsApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin{
            koin.loadModules(listOf(module {
                single<Context> { this@WebListsApp } bind Application::class
            }))
            modules(createAppModule())
        }
    }
}