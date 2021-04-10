package info.anodsplace.weblists.common.export

interface Export {
    suspend fun export(destUri: String, content: String): Int

    companion object {
        const val NO_RESULT = -1
        const val RESULT_DONE = 0
        const val ERROR_FILE_READ = 3
        const val ERROR_FILE_WRITE = 4
        const val ERROR_DESERIALIZE = 5
        const val ERROR_UNEXPECTED = 6
        const val ERROR_INCORRECT_FORMAT = 7
    }
}