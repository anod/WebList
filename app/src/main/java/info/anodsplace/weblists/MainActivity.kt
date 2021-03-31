package info.anodsplace.weblists

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import info.anodsplace.weblists.ui.screen.MainScreen
import info.anodsplace.weblists.ui.theme.WebListTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebListTheme {
                MainScreen(viewModel)
            }
        }
    }
}
