package ir.romroid.secureboxrecorder.presentation.safe

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.nahad.common.ext.viewModelIO
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.provider.FileProviderListener
import ir.romroid.secureboxrecorder.domain.repository.AppRepository
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SafeViewModel @Inject constructor(
    private val appRepo: AppRepository
) : BaseViewModel() {

    private val _unzip = SingleLiveData<UnzipResult>()
    val unzip: SingleLiveData<UnzipResult>
        get() = _unzip


    fun shouldSetUserKey(): Boolean {
        return appRepo.userKey().isEmpty()
    }

    fun shouldChangeUserKey(): Boolean {
        val lastTime = appRepo.userKeyTime()
        val maxDiffTime = 604800000 // 1 week

        val diff = abs(System.currentTimeMillis() - lastTime)

        return diff > maxDiffTime
    }

    fun saveUserKey(key: String) {
        appRepo.appCache.userKey = key
        appRepo.appCache.userKeyTime = System.currentTimeMillis()
    }

    fun saveRecoveryKey(key: String) {
        appRepo.appCache.recoveryKey = key
    }

    fun getFileName(uri: Uri): String {
        return appRepo.fileProvider.getFileNameFromCursor(uri) ?: ""
    }

    fun unzipFile(file: Uri) = viewModelIO {

        val fileTemp = appRepo.fileProvider.copyToTemp(file)

        if (fileTemp != null) {
            appRepo.fileProvider.unzipToSave(
                fileTemp,
                object : FileProviderListener {
                    override fun onProgress() {
                        _unzip.postValue(UnzipResult.Progress)
                    }

                    override fun onSuccess(file: Uri) {
                        _unzip.postValue(UnzipResult.Success(file))
                    }

                    override fun onError(e: Exception) {
                        _unzip.postValue(UnzipResult.Error(e.message ?: ""))
                    }

                })

//            fileTemp.deleteOnExit()
        } else {
            _unzip.value = UnzipResult.Error("File not saved")
        }

    }

    sealed class UnzipResult {
        object Progress : UnzipResult()
        class Success(val file: Uri) : UnzipResult()
        class Error(val message: String) : UnzipResult()
    }

}