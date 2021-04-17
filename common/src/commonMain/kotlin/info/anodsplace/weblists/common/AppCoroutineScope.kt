package info.anodsplace.weblists.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class AppCoroutineScope(context: CoroutineContext) : CoroutineScope {

    constructor(dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate)
            : this(SupervisorJob() + dispatcher)

    override val coroutineContext: CoroutineContext = context
}