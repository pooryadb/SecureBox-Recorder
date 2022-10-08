package ir.romroid.secureboxrecorder.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

val newCoroutineScopeCompute get() = CoroutineScope(Dispatchers.Default + Job())
val newCoroutineScopeIO get() = CoroutineScope(Dispatchers.IO + Job())
val newCoroutineScopeMain get() = CoroutineScope(Dispatchers.Main.immediate + Job())

val newSupervisorScopeCompute get() = CoroutineScope(Dispatchers.Default + SupervisorJob())
val newSupervisorScopeIO get() = CoroutineScope(Dispatchers.IO + SupervisorJob())
val newSupervisorScopeMain get() = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

fun CoroutineScope.superLaunch(
    context: CoroutineContext? = null,
    block: suspend CoroutineScope.() -> Unit,
) = if (context != null) launch(context) { supervisorScope(block) }
else launch { supervisorScope(block) }

suspend fun <T> withCompute(
    block: suspend CoroutineScope.() -> T,
) = withContext(Dispatchers.Default) { block(this) }

suspend fun <T> withIO(
    block: suspend CoroutineScope.() -> T,
) = withContext(Dispatchers.IO) { block(this) }

suspend fun <T> withMain(
    block: suspend CoroutineScope.() -> T,
) = withContext(Dispatchers.Main.immediate) { block(this) }

fun <T> CoroutineScope.launchCompute(
    block: suspend CoroutineScope.() -> T
) = launch(Dispatchers.Default) { block() }

fun <T> CoroutineScope.launchIO(
    block: suspend CoroutineScope.() -> T
) = launch(Dispatchers.IO) { block() }

fun <T> CoroutineScope.launchMain(
    block: suspend CoroutineScope.() -> T
) = launch(Dispatchers.Main.immediate) { block() }

fun <T> CoroutineScope.superlaunchCompute(
    block: suspend CoroutineScope.() -> T
) = superLaunch(Dispatchers.Default) { block() }

fun <T> CoroutineScope.superlaunchIO(
    block: suspend CoroutineScope.() -> T
) = superLaunch(Dispatchers.IO) { block() }

fun <T> CoroutineScope.superlaunchMain(
    block: suspend CoroutineScope.() -> T
) = superLaunch(Dispatchers.Main.immediate) { block() }

fun <T> CoroutineScope.asyncCompute(
    start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> T
) = async(Dispatchers.Default, start, block)

fun <T> CoroutineScope.asyncIO(
    start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> T
) = async(Dispatchers.IO, start, block)

fun <T> CoroutineScope.asyncMain(
    start: CoroutineStart = CoroutineStart.DEFAULT, block: suspend CoroutineScope.() -> T
) = async(Dispatchers.Main.immediate, start, block)

fun ViewModel.viewModelCompute(
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launchCompute(block)

fun ViewModel.viewModelMain(
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launchMain(block)

fun ViewModel.viewModelIO(
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launchIO(block)

fun <T> runBlockingCompute(block: suspend CoroutineScope.() -> T) {
    runBlocking(Dispatchers.Default) { block() }
}

fun <T> runBlockingIO(block: suspend CoroutineScope.() -> T) {
    runBlocking(Dispatchers.IO) { block() }
}

fun <T> runBlockingMain(block: suspend CoroutineScope.() -> T) {
    runBlocking(Dispatchers.Main.immediate) { block() }
}