package ir.romroid.secureboxrecorder.presentation.box

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.FileModel
import ir.romroid.secureboxrecorder.domain.model.MessageResult
import ir.romroid.secureboxrecorder.domain.model.Result
import ir.romroid.secureboxrecorder.domain.repository.BoxRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BoxViewModel @Inject constructor(
    private val boxRepo: BoxRepository
) : BaseViewModel() {

    private val _liveFileList = SingleLiveData<List<FileModel>>()
    val liveFileList: LiveData<List<FileModel>>
        get() = _liveFileList

    private val _liveShareFile = SingleLiveData<File>()
    val liveShareFile: LiveData<File>
        get() = _liveShareFile

    private val _liveExportPath = SingleLiveData<String>()
    val liveExportPath: SingleLiveData<String>
        get() = _liveExportPath

    private val _liveTempFile = SingleLiveData<FileModel>()
    val liveTempFile: LiveData<FileModel>
        get() = _liveTempFile


    fun fetchFileList() = viewModelIO {
        _liveFileList.postValue(boxRepo.getSavedFiles())
    }

    fun deleteFile(id: Long) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))

        val file = _liveFileList.value?.firstOrNull { it.id == id }

        if (file != null && boxRepo.deleteFile(file.uri)) {
            _liveMessage.postValue(MessageResult.Loading(false))
            fetchFileList()
        } else
            _liveMessage.postValue(MessageResult.Error(R.string.error_delete_file))
    }

    fun shareFile(uri: Uri) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))

        when (val file = boxRepo.getFile(uri)) {
            is Result.Error -> _liveMessage.postValue(MessageResult.Error(R.string.error_share_file))
            is Result.Success -> {
                _liveMessage.postValue(MessageResult.Loading(false))
                _liveShareFile.postValue(file.data.uri.toFile())
            }
        }

    }

    fun addFile(uri: Uri) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))

        if (boxRepo.saveAndEncrypt(uri)) {
            _liveMessage.postValue(MessageResult.Loading(false))
            fetchFileList()
        } else
            _liveMessage.postValue(MessageResult.Error(R.string.error_add_file))
    }

    fun exportData() = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))

        boxRepo.exportFiles()
            .let {
                when (it) {
                    is Result.Error -> _liveMessage.postValue(
                        MessageResult.Error(msg = it.exception.message ?: "")
                    )
                    is Result.Success -> {
                        _liveMessage.postValue(MessageResult.Loading(false))
                        _liveExportPath.postValue(it.data)
                    }
                }
            }
    }

    fun clearTemp() = boxRepo.clearTemp()

    fun getFile(uri: Uri) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))
        when (val file = boxRepo.getFile(uri)) {
            is Result.Error -> _liveMessage.postValue(MessageResult.Error(R.string.error_open_file))
            is Result.Success -> {
                _liveMessage.postValue(MessageResult.Loading(false))
                _liveTempFile.postValue(file.data)
            }
        }

    }

}