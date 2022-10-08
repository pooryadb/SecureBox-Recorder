package ir.romroid.secureboxrecorder.presentation.safe

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.Result
import ir.romroid.secureboxrecorder.domain.repository.BoxRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import javax.inject.Inject

@HiltViewModel
class SafeViewModel @Inject constructor(
    private val appRepo: BoxRepository
) : BaseViewModel() {

    private val _liveUnzip = SingleLiveData<UnzipResult>()
    val liveUnzip: SingleLiveData<UnzipResult>
        get() = _liveUnzip


    fun shouldSetUserKey(): Boolean {
        return appRepo.userKey().isEmpty()
    }

    fun saveUserKey(key: String) {
        appRepo.appCache.userKey = key
    }

    fun getUserKey() = appRepo.appCache.userKey

    fun unzipFile(file: Uri) = viewModelIO {

        val fileTemp = appRepo.boxProvider.copyToTemp(file)

        if (fileTemp != null) {
            appRepo.boxProvider.unzipToSaveFolder(fileTemp).collect {
                when (it) {
                    is Result.Error -> {
                        _liveUnzip.postValue(UnzipResult.Error(it.exception.message ?: ""))
                        fileTemp.delete()
                    }
                    Result.Loading -> _liveUnzip.postValue(UnzipResult.Progress)
                    is Result.Success -> {
                        _liveUnzip.postValue(UnzipResult.Success(it.data))
                        fileTemp.delete()
                    }
                }
            }
        } else {
            _liveUnzip.value = UnzipResult.Error("File not saved")
        }

    }

    sealed class UnzipResult {
        object Progress : UnzipResult()
        class Success(val filePath: String) : UnzipResult()
        class Error(val message: String) : UnzipResult()
    }

}