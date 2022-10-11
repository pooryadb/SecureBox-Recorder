package ir.romroid.secureboxrecorder.base.architecture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.romroid.secureboxrecorder.domain.model.MessageResult

open class BaseViewModel : ViewModel() {

    protected val _liveMessage = MutableLiveData<MessageResult>()
    val liveMessage: LiveData<MessageResult>
        get() = _liveMessage

}