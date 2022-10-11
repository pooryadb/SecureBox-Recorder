package ir.romroid.secureboxrecorder.presentation.keys

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.MessageResult
import ir.romroid.secureboxrecorder.domain.repository.BoxRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import javax.inject.Inject

@HiltViewModel
class KeyViewModel @Inject constructor(
    private val boxRepo: BoxRepository
) : BaseViewModel() {

    private val _liveUnzip = SingleLiveData<Boolean>()
    val liveUnzip: SingleLiveData<Boolean>
        get() = _liveUnzip

    fun shouldSetUserKey(): Boolean {
        return boxRepo.userKey().isEmpty()
    }

    fun saveUserKey(key: String) {
        boxRepo.saveUserKey(key)
    }

    fun getUserKey() = boxRepo.userKey()

    fun unzipFile(file: Uri) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))
        val extract = boxRepo.extractBackup(file)
        if (extract) {
            _liveUnzip.postValue(true)
            _liveMessage.postValue(MessageResult.Loading(false))
        } else
            _liveMessage.postValue(MessageResult.Error(R.string.restore_backup_error))
    }

}