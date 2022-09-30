package ir.romroid.secureboxrecorder.presentation.fileManager

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.FileModel
import ir.romroid.secureboxrecorder.domain.model.FileType
import ir.romroid.secureboxrecorder.domain.provider.FileProviderListener
import ir.romroid.secureboxrecorder.domain.repository.AppRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileManagerViewModel @Inject constructor(
    private val appRepository: AppRepository
) : BaseViewModel() {

    companion object {
        private const val TAG = "FileManagerVM"
    }

    private val _liveFileList = SingleLiveData<List<FileModel>>()
    val liveFileList: LiveData<List<FileModel>>
        get() = _liveFileList

    private val _liveAddFile = SingleLiveData<Boolean>()
    val liveAddFile: LiveData<Boolean>
        get() = _liveAddFile

    private val _liveDeleteFile = SingleLiveData<Boolean>()
    val liveDeleteFile: LiveData<Boolean>
        get() = _liveDeleteFile

    private val _liveShareFile = SingleLiveData<File?>()
    val liveShareFile: LiveData<File?>
        get() = _liveShareFile


    private val _liveExport = SingleLiveData<ExportResult>()
    val liveExport: SingleLiveData<ExportResult>
        get() = _liveExport

    private val _liveTempFile = SingleLiveData<FileModel?>()
    val liveTempFile: LiveData<FileModel?>
        get() = _liveTempFile


    fun fetchFileList() = viewModelIO {
        _liveFileList.postValue(
            appRepository.getSavedFiles().map {
                val fileSuffix = it.first.substringAfterLast(".")

                FileModel(
                    name = it.first,
                    type = FileType.getType(fileSuffix) ?: FileType.Other,
                    uri = it.second.toUri()
                )
            }
        )
    }

    fun deleteFile(id: Long) = viewModelIO {
        _liveFileList.value?.firstOrNull { it.id == id }?.let {
            _liveDeleteFile.postValue(appRepository.deleteFile(it.uri))
        } ?: _liveDeleteFile.postValue(false)
    }

    fun shareFile(uri: Uri) = viewModelIO {
        _liveShareFile.postValue(appRepository.copyToShare(uri))
    }

    fun addFile(uri: Uri) = viewModelIO {
        _liveAddFile.postValue(appRepository.saveAndEncrypt(uri))
    }

    fun exportData() = viewModelIO {
        appRepository.exportFiles(object : FileProviderListener {
            override fun onProgress() {
                _liveExport.postValue(ExportResult.Progress)
            }

            override fun onSuccess(filePath: String) {
                _liveExport.postValue(ExportResult.Success(filePath))
            }

            override fun onError(e: Exception) {
                _liveExport.postValue(ExportResult.Error(e.message ?: ""))
            }

        })
    }

    fun clearTemp() = appRepository.clearTemp()

    fun tempFile(uri: Uri) = viewModelIO {

        appRepository.copyToTemp(uri)?.let {
            val model = FileModel(
                name = it.name,
                type = FileType.getType(it.extension) ?: FileType.Other,
                uri = it.toUri()
            )

            _liveTempFile.postValue(model)
        } ?: _liveTempFile.postValue(null)
    }

    sealed class ExportResult {
        object Progress : ExportResult()
        class Success(val filePath: String) : ExportResult()
        class Error(val message: String) : ExportResult()
    }

}