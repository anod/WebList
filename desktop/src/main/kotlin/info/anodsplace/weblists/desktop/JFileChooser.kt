package info.anodsplace.weblists.desktop

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private val yamlFilter = FileNameExtensionFilter("WebList definition (yaml, yml)", "yaml", "yml")

fun JFileChooser.showSaveDialog(destFileName: String): File? {
    selectedFile = File(destFileName)
    fileFilter = yamlFilter
    val returnValue = showSaveDialog(null)
    return if (returnValue == JFileChooser.APPROVE_OPTION) {
        val selectedFile = selectedFile
        if (selectedFile.isDirectory) {
            File(selectedFile, destFileName)
        } else selectedFile
    } else {
        null
    }
}

fun JFileChooser.showOpenDialog(): File? {
    fileFilter = yamlFilter
    val returnValue = showOpenDialog(null)
    return if (returnValue == JFileChooser.APPROVE_OPTION) {
        val selectedFile = selectedFile
        if (selectedFile.isDirectory) {
            null
        } else selectedFile
    } else {
        null
    }
}

