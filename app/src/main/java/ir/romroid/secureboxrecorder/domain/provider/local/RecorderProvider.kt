package ir.romroid.secureboxrecorder.domain.provider.local

import android.content.Context
import android.net.Uri
import ir.romroid.secureboxrecorder.domain.model.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject

class RecorderProvider @Inject constructor(
    context: Context,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFileProvider(context, ioDispatcher) {

    fun getRecords(): List<File> = folderRecords.listFiles()?.toList() ?: emptyList()

    suspend fun copyToTemp(contentUri: Uri) =
        copyTo(contentUri, folderTemp.path)

    suspend fun unzipToSaveFolder(
        file: File,
        listener: ((Result<String>) -> Unit)? = null
    ): Boolean = unzip(file, folderBox.path, listener)

    fun clearTemp() = folderTemp.delete()

}