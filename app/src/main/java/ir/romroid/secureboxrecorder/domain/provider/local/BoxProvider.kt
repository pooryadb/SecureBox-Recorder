package ir.romroid.secureboxrecorder.domain.provider.local

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import ir.romroid.secureboxrecorder.domain.model.Result
import ir.romroid.secureboxrecorder.ext.logD
import ir.romroid.secureboxrecorder.utils.CryptoUtils
import ir.romroid.secureboxrecorder.utils.CryptoUtils.encodeBase64Replaced
import ir.romroid.secureboxrecorder.utils.CryptoUtils.fromBase64Replaced
import ir.romroid.secureboxrecorder.utils.FILE_EXPORT_NAME_date
import ir.romroid.secureboxrecorder.utils.FileUtils
import ir.romroid.secureboxrecorder.utils.MAX_FILE_SIZE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.SecretKey
import javax.inject.Inject

class BoxProvider @Inject constructor(
    context: Context,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FileProvider(context, ioDispatcher) {

    suspend fun copyToTemp(contentUri: Uri) =
        copyTo(contentUri, folderTemp.path)

    suspend fun unzipToSaveFolder(
        file: File,
        listener: ((Result<String>) -> Unit)? = null
    ): Boolean = unzip(file, folderBox.path, listener)

    suspend fun zipFilesToExportFolder(
        listener: ((Result<String>) -> Unit)
    ): Boolean =
        zip(
            file = folderBox,
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

    private fun encryptFileName(sKey: SecretKey, name: String): String =
        CryptoUtils.encrypt(sKey, name).encodeBase64Replaced()

    private fun decryptFileName(sKey: SecretKey, encryptedName: String): String =
        CryptoUtils.decryptToString(sKey, encryptedName.fromBase64Replaced())

    fun getEncryptedFiles(): List<File> = folderBox.listFiles()?.toList() ?: emptyList()

    /**
     * @return
     * [Pair.first] decrypted file name
     * [Pair.second] encrypted file
     */
    suspend fun getFiles(key: String): List<Pair<String, File>> = withContext(ioDispatcher) {
        return@withContext getEncryptedFiles().map {
            Pair(
                decryptFileName(CryptoUtils.convertToKey(key), it.name),
                it
            )
        }
    }

    suspend fun saveToBox(key: String, contentUri: Uri): Boolean =
        withContext(ioDispatcher) {
            "saveToBox: contentUri= $contentUri".logD(TAG)

            val fileName = getFileNameWithExtension(context, contentUri)

            val sKey = CryptoUtils.convertToKey(key)
            var nameEncrypted = encryptFileName(sKey, fileName)

            getEncryptedFiles().firstOrNull { it.name == nameEncrypted }?.let {
                nameEncrypted = encryptFileName(sKey, addRandomToName(fileName))
            }

            val filePath = folderBox.path + "/" + nameEncrypted
            val tempFile = createFile(filePath)

            "fileFromContentUri: path= ${tempFile.path}".logD(TAG)

            return@withContext try {
                val oStream = FileOutputStream(tempFile)
                val inputStream = context.contentResolver.openInputStream(contentUri)

                if (inputStream!!.available() > MAX_FILE_SIZE) {
                    return@withContext false
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

    suspend fun restoreFromBox(key: String, uri: Uri): File? =
        withContext(ioDispatcher) {
            try {
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
        }

    fun clearTemp() = folderTemp.delete()

}