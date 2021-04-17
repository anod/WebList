package info.anodsplace.weblists.backup

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class CreateDocument : ActivityResultContract<CreateDocument.Args, Uri?>() {
    private val inner = ActivityResultContracts.CreateDocument()
    class Args(val initialUri: Uri, val dataType: String, val title: String)

    @SuppressLint("MissingSuperCall")
    override fun createIntent(context: Context, input: Args): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            putExtra(Intent.EXTRA_TITLE, input.title)
            try {
                type = input.dataType
//                setDataAndType(input.initialUri, input.dataType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, input.initialUri)
                }
            } catch (e: Exception) {
                Log.e("WebLists", "CreateDocument: ${e.message}", e)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return inner.parseResult(resultCode, intent)
    }
}
