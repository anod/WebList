package info.anodsplace.weblists

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import info.anodsplace.weblists.backup.CreateDocument
import info.anodsplace.weblists.common.AndroidApp
import info.anodsplace.weblists.common.theme.WebListTheme
import info.anodsplace.weblists.ui.LocalBackPressedDispatcher
import info.anodsplace.weblists.ui.MainScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var createDocumentLauncher: ActivityResultLauncher<CreateDocument.Args>
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { destUri ->
            viewModel.importFrom(destUri.toString())
        }

        createDocumentLauncher = registerForActivityResult(CreateDocument()) { destUri ->
            viewModel.onExportUri(true, destUri.toString())
        }

        lifecycleScope.launch {
            viewModel.createDocumentRequest.collect { title ->
                createDocumentLauncher.launch(CreateDocument.Args(Uri.EMPTY, "text/yaml", title))
            }
        }

        lifecycleScope.launch {
            viewModel.openDocument.collect { _ ->
                openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            }
        }

        setContent {
            CompositionLocalProvider(LocalBackPressedDispatcher provides this) {
                AndroidApp {
                    MainScreen(viewModel, strings = AndroidStrings(applicationContext))
                }
            }
        }
    }
}
