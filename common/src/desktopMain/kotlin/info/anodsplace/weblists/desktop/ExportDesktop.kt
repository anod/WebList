package info.anodsplace.weblists.desktop

import info.anodsplace.weblists.common.export.Export

class ExportDesktop: Export {
    override suspend fun export(destUri: String, content: String): Int {
        return Export.ERROR_UNEXPECTED
    }
}