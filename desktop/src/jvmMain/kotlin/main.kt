import androidx.compose.desktop.Window
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import info.anodsplace.weblists.common.AppCoroutineScope
import info.anodsplace.weblists.common.AppPreferences
import info.anodsplace.weblists.common.CommonAppViewModel
import info.anodsplace.weblists.common.StringProvider
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.common.ui.theme.WebListTheme
import info.anodsplace.weblists.db.WebListsDb
import info.anodsplace.weblists.desktop.ExportDesktop
import info.anodsplace.weblists.desktop.MainScreen
import info.anodsplace.weblists.desktop.Preferences
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        val sqlDriver = DatabaseDriverFactory().createDriver()
        AppDatabase(WebListsDb(sqlDriver))
    }
    single<AppPreferences> { Preferences() }
    single { HtmlClientFactory().create() }
    single { Yaml(configuration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property
    )
    ) }
    single { ExportDesktop() }
}

val appCoroutineScope = AppCoroutineScope()

fun main() = Window {
    startKoin {
        koin.loadModules(listOf(module {
            single { appCoroutineScope } bind AppCoroutineScope::class
        }))
        modules(appModule)
    }
    val viewModel = CommonAppViewModel(appCoroutineScope)
    WebListTheme(darkTheme = true) {
        MainScreen(viewModel, strings = StringProvider.Default())
    }
}