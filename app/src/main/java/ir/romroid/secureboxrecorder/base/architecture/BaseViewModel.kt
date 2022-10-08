package ir.romroid.secureboxrecorder.base.architecture

import androidx.lifecycle.ViewModel
import ir.romroid.secureboxrecorder.ext.viewModelIO
import kotlinx.coroutines.flow.Flow

open class BaseViewModel : ViewModel() {

    fun <T> observeFlow(
        flow: Flow<T>,
        observeFunction: (T) -> Unit,
    ) = viewModelIO {
        flow.collect {
            observeFunction(it)
        }
    }

}