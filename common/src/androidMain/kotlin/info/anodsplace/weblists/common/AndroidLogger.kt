package info.anodsplace.weblists.common

import android.util.Log
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

class AndroidLogger : Logger(Level.DEBUG) {

    override fun log(level: Level, msg: MESSAGE) {
        when(level) {
            Level.DEBUG -> Log.d("WebLists", msg)
            Level.INFO -> Log.i("WebLists", msg)
            Level.ERROR -> Log.e("WebLists", msg)
            Level.NONE -> { }
        }
    }
}
