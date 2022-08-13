package ir.romroid.secureboxrecorder.utils

import android.util.Base64
import ir.romroid.secureboxrecorder.ext.logD
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {

    private const val TAG = "Crypto"

    fun convertToKey(encodedKey: String): SecretKey {
        val decodedKey: ByteArray = Base64.decode(encodedKey, Base64.NO_WRAP)
        val str = String(decodedKey, Charsets.US_ASCII)
        str.logD("$TAG convertToKey decodedKey")
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    @Throws(NoSuchAlgorithmException::class)
    fun convertKey(secretKey: SecretKey): String {
        val rawData = secretKey.encoded
        return Base64.encodeToString(rawData, Base64.NO_WRAP)
    }


    fun ByteArray.encodeBase64Replaced(): String =
        Base64.encodeToString(this, Base64.NO_WRAP).replace("/", "_")

    fun String.encodeBase64(): String =
        Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)

    fun ByteArray.toStringASCII(): String =
        String(this, Charsets.US_ASCII)

    fun String.fromBase64(): ByteArray =
        Base64.decode(this.replace("_", "/"), Base64.NO_WRAP)


    @Throws(Exception::class)
    fun encrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = yourKey.encoded
        val sKeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    @Throws(Exception::class)
    fun decrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val decrypted: ByteArray
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.DECRYPT_MODE, yourKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        decrypted = cipher.doFinal(fileData)
        return decrypted
    }

    /*fun decryptFile(
        key: String,
        targetFilePath: String,
        destinationFolderPath: String
    ) {
        val passBase64 = key.encodeBase64()
        val sk = convertToKey(passBase64)
        val decryptedFileData = decrypt(sk, readFile(targetFilePath))
        val fName =
            decrypt(sk, targetFilePath.toUri().pathSegments.last().fromBase64()).toStringASCII()
        val f = File("$destinationFolderPath/$fName")
        if (f.exists().not()) {
            if (f.parentFile.exists().not())
                f.parentFile.mkdirs()
            saveFile(decryptedFileData, f.path)
        }
    }*/
}