package ir.romroid.secureboxrecorder.presentation.recordList

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import ir.romroid.secureboxrecorder.base.architecture.BaseViewModel
import ir.romroid.secureboxrecorder.domain.model.AudioModel
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.utils.VOICE_SAVED_FOLDER_NAME
import ir.romroid.secureboxrecorder.utils.liveData.SingleLiveData
import java.io.File

class RecorderListViewModel : BaseViewModel() {

    private val _liveRecordedList = SingleLiveData<List<AudioModel>>()
    val liveRecordedList: LiveData<List<AudioModel>>
        get() = _liveRecordedList


    fun fetchRecordedList(context: Context) {
        val folder = File(context.cacheDir.path + "/" + VOICE_SAVED_FOLDER_NAME)
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


}