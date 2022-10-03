package ir.romroid.secureboxrecorder.presentation.recorder

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.AudioModel
import ir.romroid.secureboxrecorder.domain.repository.AppRepository
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.utils.VOICE_SAVED_FOLDER_NAME
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecorderListViewModel @Inject constructor(
    private val appRepository: AppRepository
) : BaseViewModel() {

    private val _liveRecordedList = SingleLiveData<List<AudioModel>>()
    val liveRecordedList: LiveData<List<AudioModel>>
        get() = _liveRecordedList


    fun fetchRecordedList(context: Context) {// FIXME: use provider instead pass context
        val folder = File(context.getExternalFilesDir(null), VOICE_SAVED_FOLDER_NAME)
        _liveRecordedList.value = folder.listFiles()?.map {
            it.logD("audioModel fetchRecordedList")
            AudioModel(
                name = it.name,
                uri = it.toUri()
            ).apply {
                id = it.name.hashCode().toLong()
            }
        } ?: ArrayList<AudioModel>()
    }

    fun deleteRecord(id: Long): Boolean =
        try {
            _liveRecordedList.value?.find { it.id == id }?.let {
                return File(it.uri.path).delete()
            }

            false
        } catch (e: Exception) {
            false
        }

}