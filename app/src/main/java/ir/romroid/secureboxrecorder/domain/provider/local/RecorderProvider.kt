package ir.romroid.secureboxrecorder.domain.provider.local

import android.content.Context
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.utils.VOICE_FORMAT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RecorderProvider @Inject constructor(
    context: Context,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFileProvider(context, ioDispatcher) {

    fun getRecords(): List<File> = folderRecords.listFiles()?.toList() ?: emptyList()

    fun prepareTempFile(): File {
        val f = File(folderTempRecord, "tempRecord$VOICE_FORMAT")
        createFile(f.path)
        f.path.logD("$TAG path")

        return f
    }

    suspend fun saveToRecords(file: File, name: String): File? = withContext(ioDispatcher) {
        var destName = name
        val sameNameFile = getRecords().firstOrNull { it.nameWithoutExtension == name }
        if (sameNameFile != null)
            destName += SimpleDateFormat(
                "_yyyy-MM-dd_HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

        val source = File(file.parentFile, "$destName.${file.extension}")
        file.renameTo(source)

        return@withContext copyTo(source, folderRecords.path).also {
            source.delete()
        }
    }

}