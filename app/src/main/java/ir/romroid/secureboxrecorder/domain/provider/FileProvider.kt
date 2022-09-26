package ir.romroid.secureboxrecorder.domain.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.ext.logE
import ir.romroid.secureboxrecorder.ext.logI
import ir.romroid.secureboxrecorder.utils.*
import ir.romroid.secureboxrecorder.utils.CryptoUtils.encodeBase64Replaced
import ir.romroid.secureboxrecorder.utils.CryptoUtils.fromBase64Replaced
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject
import kotlin.random.Random

class FileProvider @Inject constructor(val context: Context) {

    companion object {
        private const val TAG = "FileProvider"
    }

    private val folderSave by lazy {
        val f = File(context.getExternalFilesDir(null), FILES_SAVED_FOLDER_NAME)
        f.mkdirs()

        f
    }

    private val folderTemp by lazy {
        val f = File(context.cacheDir, FILES_TEMP_SHARE_FOLDER_NAME)
        f.mkdirs()

        f
    }

    private val folderExport by lazy {
        val f = File(context.getExternalFilesDir(null), FILES_EXPORT_FOLDER_NAME)
        f.mkdirs()

        f
    }

    suspend fun copyToTemp(contentUri: Uri) =
        copyTo(contentUri, folderTemp.path)

    private suspend fun copyTo(contentUri: Uri, destinationFolderPath: String): File? {
        Log.e(TAG, "saveToPath: contentUri= ${contentUri}, dest= $destinationFolderPath")

        val parentFolder = File(destinationFolderPath)
        val fileName = getFileNameWithExtension(context, contentUri)
        val filePath = parentFolder.path + "/" + fileName
        // Creating Temp file
        val tempFile = createFile(filePath)

        Log.e(TAG, "fileFromContentUri: path= ${tempFile.path}")

        return try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            val fileData = FileUtils.readFile(inputStream!!)

            FileUtils.saveFile(fileData, oStream)

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }
    }

    private fun getFileNameWithExtension(context: Context, uri: Uri): String {
        var fileName: String? = ""
        if (uri.scheme == "file") {
            fileName = uri.pathSegments.last() ?: ""
        } else {
            fileName = getFileNameFromCursor(uri)
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

    fun getFileNameFromCursor(uri: Uri): String? {
        val fileCursor: Cursor? = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        var fileName: String? = null
        if (fileCursor != null && fileCursor.moveToFirst()) {
            val cIndex: Int = fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cIndex != -1) {
                fileName = fileCursor.getString(cIndex)
            }
        }
        fileCursor?.close()
        return fileName
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }


    fun unzipToSaveFolder(
        file: File,
        listener: FileProviderListener? = null
    ): Boolean = unzip(file, folderSave.path, listener)

    /**
     * files set same name as zip file!
     */
    private fun unzip(
        file: File,
        destinationPath: String? = null,
        listener: FileProviderListener? = null
    ): Boolean {
        return try {
            listener?.onProgress()
            val inputStream = FileInputStream(file.path)
            val zipStream = ZipInputStream(inputStream)
            var zEntry: ZipEntry? = null
            val zipName = file.name.substringBeforeLast(".")

            val destination = destinationPath
                ?: file.parent?.also { "$it/$zipName" }
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

            listener?.onSuccess(destination)

            true
        } catch (e: Exception) {
            "Unzipping failed : $e".logD(TAG)
            listener?.onError(e)
            false
        }
    }

    suspend fun zipFilesToExportFolder(
        listener: FileProviderListener
    ): Boolean =
        zip(
            file = folderSave,
            destinationPath = File(
                folderExport,
                FILE_EXPORT_NAME_date.format(
                    SimpleDateFormat(
                        "yyyy-MM-dd_HH:mm",
                        Locale.getDefault()
                    ).format(Date())
                )
            ).path,
            listener = listener
        )

    /**
     * @param file must be a directory with at least 1 file
     * @param destinationPath should be a *.zip file
     */
    private suspend fun zip(
        file: File,
        destinationPath: String,
        listener: FileProviderListener? = null
    ): Boolean {
        (listener ?: "null").logE("$TAG zip listener")
        if (file.isDirectory.not() || file.listFiles().isNullOrEmpty()) {
            listener?.onError(Exception("path is empty!"))
            return false
        }

        val destFile = File(destinationPath)
        if (destFile.extension != "zip") {
            listener?.onError(Exception("should be a *.zip file!"))
            return false
        }
        if (destFile.exists())
            destFile.delete()
        createFile(destinationPath)

        listener?.onProgress()

        val BUFFER = 1024

        return try {
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

            listener?.onSuccess(destinationPath)

            out.close()
            true
        } catch (e: Exception) {
            delete(destinationPath.toUri())
            "zip error: ${e.message}".logI(TAG)
            listener?.onError(e)
            false
        }
    }

    private fun createFolder(path: String): File? = try {
        val file = File(path)
        if (file.isDirectory.not()) {
            file.mkdirs()
        }

        file
    } catch (e: Exception) {
        "create file Error: $e".logD(TAG)

        null
    }

    private fun createFile(path: String): File {
        val tempFile = File(path)
        tempFile.parentFile?.mkdirs()
        tempFile.logE("$TAG createFile")
        tempFile.createNewFile()
        return tempFile
    }

    private fun encryptFileName(sKey: SecretKey, name: String): String =
        CryptoUtils.encrypt(sKey, name).encodeBase64Replaced()

    private fun decryptFileName(sKey: SecretKey, encryptedName: String): String =
        CryptoUtils.decryptToString(sKey, encryptedName.fromBase64Replaced())

    fun getEncryptedFiles(): List<File> = folderSave.listFiles()?.toList() ?: emptyList()

    /**
     * @return
     * [Pair.first] decrypted file name
     * [Pair.second] encrypted file
     */
    suspend fun getFiles(key: String): List<Pair<String, File>> = getEncryptedFiles().map {
        Pair(
            decryptFileName(CryptoUtils.convertToKey(key), it.name),
            it
        )
    }

    suspend fun saveToBox(key: String, contentUri: Uri): Boolean {
        "saveToBox: contentUri= $contentUri".logD(TAG)

        val fileName = getFileNameWithExtension(context, contentUri)

        val sKey = CryptoUtils.convertToKey(key)
        var nameEncrypted = encryptFileName(sKey, fileName)

        getEncryptedFiles().firstOrNull { it.name == nameEncrypted }?.let {
            nameEncrypted = encryptFileName(sKey, addRandomToName(fileName))
        }

        val filePath = folderSave.path + "/" + nameEncrypted
        val tempFile = createFile(filePath)

        "fileFromContentUri: path= ${tempFile.path}".logD(TAG)

        return try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            if (inputStream!!.available() > MAX_FILE_SIZE) {
                return false
            }

            val fileData = FileUtils.readFile(inputStream)
            val encryptedFileData = CryptoUtils.encrypt(sKey, fileData)
            FileUtils.saveFile(encryptedFileData, oStream)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            tempFile.delete()

            false
        }
    }

    suspend fun restoreFromBox(key: String, uri: Uri): File? = try {
        "restoreFromBox: uri= $uri".logD(TAG)

        val sk = CryptoUtils.convertToKey(key)
        val decryptedFileData = CryptoUtils.decrypt(sk, FileUtils.readFile(uri))
        val fName = decryptFileName(sk, uri.toFile().name)
        val resultFile = File(folderTemp, fName)
        createFile(resultFile.path)

        FileUtils.saveFile(decryptedFileData, resultFile.path)

        resultFile
    } catch (e: Exception) {
        e.printStackTrace()
        folderTemp.delete()

        null
    }

    private fun addRandomToName(name: String): String {
        var result = name.substringBeforeLast('.', name)
        result += Random.nextInt()
        if (name.contains('.'))
            result += "." + name.substringAfterLast(".")

        return result
    }

    fun delete(uri: Uri) =
        uri.toFile().delete()

    fun clearTemp() = folderTemp.delete()

}

interface FileProviderListener {
    fun onProgress()
    fun onSuccess(filePath: String)
    fun onError(e: Exception)
}