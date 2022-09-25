package ir.romroid.secureboxrecorder.presentation.safe

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.provider.FileProviderListener
import ir.romroid.secureboxrecorder.domain.repository.AppRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SafeViewModel @Inject constructor(
    private val appRepo: AppRepository
) : BaseViewModel() {

    private val _liveUnzip = SingleLiveData<UnzipResult>()
    val liveUnzip: SingleLiveData<UnzipResult>
        get() = _liveUnzip


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

    fun getUserKey() = appRepo.appCache.userKey

    fun getFileName(uri: Uri): String {
        return appRepo.fileProvider.getFileNameFromCursor(uri) ?: ""
    }

    fun unzipFile(file: Uri) = viewModelIO {

        val fileTemp = appRepo.fileProvider.copyToTemp(file)

        if (fileTemp != null) {
            appRepo.fileProvider.unzipToSaveFolder(
                fileTemp,
                object : FileProviderListener {
                    override fun onProgress() {
                        _liveUnzip.postValue(UnzipResult.Progress)
                    }

                    override fun onSuccess(filePath: String) {
                        _liveUnzip.postValue(UnzipResult.Success(filePath))
                        fileTemp.delete()
                    }

                    override fun onError(e: Exception) {
                        _liveUnzip.postValue(UnzipResult.Error(e.message ?: ""))
                        fileTemp.delete()
                    }

                })

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