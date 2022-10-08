package ir.romroid.secureboxrecorder.presentation.recorder

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.AudioModel
import ir.romroid.secureboxrecorder.domain.repository.RecorderRepository
import ir.romroid.secureboxrecorder.ext.viewModelIO
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val recorderRepo: RecorderRepository
) : BaseViewModel() {

    val tempFile: File by lazy {
        recorderRepo.prepareTempFile()
    }

    private val _liveRecords = SingleLiveData<List<AudioModel>>()
    val liveRecords: LiveData<List<AudioModel>>
        get() = _liveRecords

    private val _liveDeleteRecord = SingleLiveData<Boolean>()
    val liveDeleteRecord: LiveData<Boolean>
        get() = _liveDeleteRecord

    private val _liveSaveRecord = SingleLiveData<Boolean>()
    val liveSaveRecord: LiveData<Boolean>
        get() = _liveSaveRecord

    fun fetchRecords() = viewModelIO {
        _liveRecords.postValue(recorderRepo.getRecords())
    }

    fun deleteRecord(id: Long) = viewModelIO {
        _liveRecords.value?.find { it.id == id }?.let {
            _liveDeleteRecord.postValue(recorderRepo.deleteFile(it.uri))
        } ?: run {
            _liveDeleteRecord.postValue(false)
        }
    }

    fun saveVoice(name: String) = viewModelIO {
        val resultSave = recorderRepo.saveRecord(tempFile, name)
        _liveSaveRecord.postValue(resultSave != null)
    }

    fun deleteTemp() {
        recorderRepo.deleteFile(tempFile.toUri())
    }

}