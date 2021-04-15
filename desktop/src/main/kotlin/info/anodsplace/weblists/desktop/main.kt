package info.anodsplace.weblists.desktop

import DatabaseDriverFactory
import HtmlClientFactory
import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntSize
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import info.anodsplace.weblists.common.AppCoroutineScope
import info.anodsplace.weblists.common.AppPreferences
import info.anodsplace.weblists.common.CommonAppViewModel
import info.anodsplace.weblists.common.DesktopExporter
import info.anodsplace.weblists.common.MainScreen
import info.anodsplace.weblists.common.Preferences
import info.anodsplace.weblists.common.export.Exporter
import info.anodsplace.weblists.common.StringProvider
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.common.ui.theme.WebListTheme
import info.anodsplace.weblists.db.WebListsDb
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.PrintLogger
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

val appModule = module {
    single {
        val sqlDriver = DatabaseDriverFactory().createDriver()
        AppDatabase(WebListsDb(sqlDriver))
    }
    single<Logger> { PrintLogger(Level.DEBUG) }
    single<AppPreferences> { Preferences() }
    single { HtmlClientFactory().create() }
    single { Yaml(configuration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property
    )) }
    single<Exporter> { DesktopExporter(get<Logger>()) }
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

fun main() {
    val appCoroutineScope = AppCoroutineScope()
    startKoin {
        koin.loadModules(listOf(module {
            single { appCoroutineScope } bind AppCoroutineScope::class
        }))
        modules(appModule)
    }

    val viewModel = CommonAppViewModel(appCoroutineScope)

    appCoroutineScope.launch {
        viewModel.createDocumentRequest.collect { destName ->
            val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
            fileChooser.selectedFile = File(destName)
            fileChooser.fileFilter = FileNameExtensionFilter("WebList definition (yaml, yml)", "yaml", "yml")
            val returnValue = fileChooser.showSaveDialog(null)
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val selectedFile = fileChooser.selectedFile
                val destFile = if (selectedFile.isDirectory) {
                    File(fileChooser.selectedFile, destName)
                } else selectedFile
                println("You chose to export to: " + destFile.absolutePath)
                viewModel.onExportUri(true, destFile.absolutePath)
            } else {
                viewModel.onExportUri(false, "")
            }
        }
    }

    Window(
        title = "Web lists",
        size = IntSize(400, 800),
        icon = loadWindowIcon(),
    ) {
        WebListTheme(darkTheme = true) {
            MainScreen(viewModel, strings = StringProvider.Default())
        }
    }
}