package ir.romroid.secureboxrecorder.domain.provider.local

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import ir.romroid.secureboxrecorder.domain.model.Result
import ir.romroid.secureboxrecorder.ext.getFileNameFromCursor
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.logE
import ir.romroid.secureboxrecorder.ext.logI
import ir.romroid.secureboxrecorder.utils.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.random.Random

open class BaseFileProvider constructor(
    protected val context: Context, protected val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    protected companion object {
        const val TAG = "BaseFileProvider"
    }

    protected val folderBox by lazy {
        val f = File(context.getExternalFilesDir(null), FILES_BOX_FOLDER_NAME)
        f.mkdirs()

        f
    }

    protected val folderTemp by lazy {
        val f = File(context.cacheDir, FILES_TEMP_SHARE_FOLDER_NAME)
        f.mkdirs()

        f
    }

    protected val folderExport by lazy {
        val f = File(context.getExternalFilesDir(null), FILES_EXPORT_FOLDER_NAME)
        f.mkdirs()

        f
    }

    protected val folderRecords by lazy {
        val f = File(context.getExternalFilesDir(null), VOICE_SAVED_FOLDER_NAME)
        f.mkdirs()

        f
    }

    protected val folderTempRecord by lazy {
        val f = File(context.cacheDir.path, VOICE_TEMP_FOLDER_NAME)
        f.mkdirs()

        f
    }

    suspend fun copyTo(contentUri: Uri, destFolderPath: String): File? = withContext(ioDispatcher) {
        "saveTo: contentUri= ${contentUri}, dest= $destFolderPath".logE(TAG)

        val fileName = getFileNameWithExtension(context, contentUri)
        val destFilePath = File(destFolderPath, fileName).path
        val destFile = createFile(destFilePath)

        "fileFromContentUri: path= ${destFile.path}".logE(TAG)

        return@withContext try {
            FileUtils.copy(
                context.contentResolver.openInputStream(contentUri)!!,
                FileOutputStream(destFile)
            )

            destFile
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }
    }

    suspend fun copyTo(file: File, destFolderPath: String): File? = withContext(ioDispatcher) {
        "saveTo: file= ${file.path}, dest= $destFolderPath".logE(TAG)

        val destFilePath = File(destFolderPath, file.name).path
        val destFile = createFile(destFilePath)

        "fileFromContentUri: path= ${destFile.path}".logE(TAG)

        return@withContext try {
            FileUtils.copy(file.inputStream(), FileOutputStream(destFile))

            destFile
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }
    }

    /**
     * files set same name as zip file!
     */
    suspend fun unzip(
        file: File, destinationPath: String? = null
    ): Result<String> = withContext(ioDispatcher) {
        return@withContext try {
            val inputStream = FileInputStream(file.path)
            val zipStream = ZipInputStream(inputStream)
            var zEntry: ZipEntry? = null
            val zipName = file.name.substringBeforeLast(".")

            val destination = destinationPath ?: file.parent?.also { "$it/$zipName" }
            ?: throw Exception("can't create destination")

            createFolder(destination) ?: throw Exception("can't create destination")

            while (zipStream.nextEntry.also { zEntry = it } != null) {
                val extractFileName = zEntry!!.name

                "Unzipping $extractFileName at $destination".logD(TAG)

                if (zEntry!!.isDirectory) {
                    createFolder(extractFileName)
                } else {
                    val fout = FileOutputStream("$destination/$extractFileName")
                    val bufout = BufferedOutputStream(fout)
                    val buffer = ByteArray(1024)
                    var read = 0
                    while (zipStream.read(buffer).also { read = it } != -1) {
                        bufout.write(buffer, 0, read)
                    }
                    zipStream.closeEntry()
                    bufout.close()
                    fout.close()
                }
            }
            zipStream.close()
            "Unzipping complete. path :  $destination".logD(TAG)

            Result.Success(destination)
        } catch (e: Exception) {
            "Unzipping failed : $e".logD(TAG)
            Result.Error(e)
        }

    }

    /**
     * @param file must be a directory with at least 1 file
     * @param destinationPath should be a *.zip file
     */
    suspend fun zip(
        file: File, destinationPath: String
    ): Result<String> = withContext(ioDispatcher) {
        if (file.isDirectory.not() || file.listFiles().isNullOrEmpty()) {
            return@withContext Result.Error(Exception("path is empty!"))
        }

        val destFile = File(destinationPath)
        if (destFile.extension != "zip") {
            return@withContext Result.Error(Exception("should be *.zip file!"))
        }
        if (destFile.exists()) destFile.delete()
        createFile(destinationPath)

        val BUFFER = 1024

        return@withContext try {
            var origin: BufferedInputStream? = null
            val dest = FileOutputStream(destinationPath)
            val out = ZipOutputStream(BufferedOutputStream(dest))
            val data = ByteArray(BUFFER)
            file.listFiles()?.forEach {
                "zip Adding: ${it.path}".logI(TAG)
                val fileInputStream = FileInputStream(it)
                origin = BufferedInputStream(fileInputStream, BUFFER)
                val entry = ZipEntry(it.name)
                out.putNextEntry(entry)

                var count: Int
                while (origin!!.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin!!.close()
            }

            out.close()

            Result.Success(destinationPath)
        } catch (e: Exception) {
            delete(destinationPath.toUri())
            "zip error: ${e.message}".logI(TAG)

            Result.Error(e)
        }
    }

    protected fun addRandomToName(name: String): String {
        var result = name.substringBeforeLast('.', name)
        result += Random.nextInt()
        if (name.contains('.')) result += "." + name.substringAfterLast(".")

        return result
    }

    fun getFileNameWithExtension(context: Context, uri: Uri): String {
        var fileName: String? = ""
        if (uri.scheme == "file") {
            fileName = uri.pathSegments.last() ?: ""
        } else {
            fileName = context.getFileNameFromCursor(uri)
            if (fileName == null) {
                val fileExtension = getFileExtension(context, uri)
                fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""
            } else if (!fileName.contains(".")) {
                val fileExtension = getFileExtension(context, uri)
                fileName = "$fileName.$fileExtension"
            }
        }
        return fileName
    }

    fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    fun createFolder(path: String): File? = try {
        val file = File(path)
        if (file.isDirectory.not()) {
            file.mkdirs()
        }

        file
    } catch (e: Exception) {
        "create file Error: $e".logD(TAG)

        null
    }

    fun createFile(path: String): File {
        val tempFile = File(path)
        tempFile.parentFile?.mkdirs()
        tempFile.logE("$TAG createFile")
        tempFile.createNewFile()
        return tempFile
    }

    fun delete(uri: Uri) = uri.toFile().delete()

}