package info.anodsplace.weblists.desktop

import DatabaseDriverFactory
import HtmlClientFactory
import androidx.compose.desktop.AppManager
import androidx.compose.desktop.Window
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.MenuItem
import androidx.compose.ui.window.Tray
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import info.anodsplace.weblists.common.AppCoroutineScope
import info.anodsplace.weblists.common.CommonAppViewModel
import info.anodsplace.weblists.common.StringProvider
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.common.AppPreferences
import info.anodsplace.weblists.common.ui.theme.WebListTheme
import info.anodsplace.weblists.common.Preferences
import info.anodsplace.weblists.common.ExportDesktop
import info.anodsplace.weblists.common.MainScreen
import info.anodsplace.weblists.db.WebListsDb
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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
    )) }
    single { ExportDesktop() }
}

fun loadWindowIcon(): BufferedImage {
    try {
        val imageRes = "images/icon_1024.png"
        val img = Thread.currentThread().contextClassLoader.getResource(imageRes)
        val bitmap: BufferedImage? = ImageIO.read(img)
        if (bitmap != null) {
            return bitmap
        }
    } catch (e: Exception) {
        print(e.message)
        e.printStackTrace()
    }
    return BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
}

val appCoroutineScope = AppCoroutineScope()

fun main() = Window(
    title = "Web lists",
    size = IntSize(400, 800),
    icon = loadWindowIcon()
) {
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