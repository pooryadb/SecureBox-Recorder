package ir.romroid.secureboxrecorder.presentation.recorder

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.R
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.AudioModel
import ir.romroid.secureboxrecorder.domain.model.MessageResult
import ir.romroid.secureboxrecorder.domain.repository.RecorderRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val recorderRepo: RecorderRepository
) : BaseViewModel() {

    val tempFile: File by lazy {
        recorderRepo.prepareTempFile()
    }

    private val _liveRecords = MutableLiveData<List<AudioModel>>()
    val liveRecords: LiveData<List<AudioModel>>
        get() = _liveRecords

    fun fetchRecords() = viewModelIO {
        val data = recorderRepo.getRecords()
        _liveMessage.postValue(MessageResult.Loading(false))
        _liveRecords.postValue(data.toList())
    }

    fun deleteRecord(id: Long) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))
        _liveRecords.value?.find { it.id == id }?.let {
            val deleteResult = recorderRepo.deleteFile(it.uri)
            if (deleteResult) {
                fetchRecords()
            } else {
                _liveMessage.postValue(MessageResult.Error(R.string.cant_find_file))
            }
        } ?: run {
            _liveMessage.postValue(MessageResult.Error(R.string.cant_find_file))
        }
    }

    fun saveVoice(name: String) = viewModelIO {
        _liveMessage.postValue(MessageResult.Loading(true))
        val resultSave = recorderRepo.saveRecord(tempFile, name)
        if (resultSave != null)
            fetchRecords()
        else
            _liveMessage.postValue(MessageResult.Error(R.string.error_save_file))
    }

    fun deleteTemp() {
        recorderRepo.deleteFile(tempFile.toUri())
    }

}