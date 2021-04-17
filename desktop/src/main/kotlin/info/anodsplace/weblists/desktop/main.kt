package info.anodsplace.weblists.desktop

import androidx.compose.desktop.Window
import androidx.compose.ui.unit.IntSize
import info.anodsplace.weblists.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

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
    val appCoroutineScope = AppCoroutineScope(Dispatchers.Swing.immediate)
    startKoin {
        koin.loadModules(listOf(module {
            single { appCoroutineScope } bind AppCoroutineScope::class
        }))
        modules(createAppModule())
    }

    val viewModel = CommonAppViewModel(appCoroutineScope)

    appCoroutineScope.launch {
        viewModel.documentRequest.collect { request ->
            val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
            val file = when(request) {
                is DocumentRequest.Create -> fileChooser.showSaveDialog(request.name)
                DocumentRequest.Open -> fileChooser.showOpenDialog()
                else -> null
            }
            if (file == null) {
                viewModel.documentRequestResult.value = DocumentRequest.Result.Error
            } else {
                viewModel.documentRequestResult.value = DocumentRequest.Result.Success(file.absolutePath)
            }
        }
    }

    Window(
        title = "Web lists",
        size = IntSize(400, 800),
        icon = loadWindowIcon(),
    ) {
        DesktopApp(viewModel = viewModel)
    }
}