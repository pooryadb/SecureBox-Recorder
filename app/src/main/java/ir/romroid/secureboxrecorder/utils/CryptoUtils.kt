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
    private const val ALGORITHM = "AES"
    private const val ALGORITHM_PROVIDER = "BC"
    private const val PATH_SPECIAL = "/"
    private const val PATH_SPECIAL_REPLACEMENT = "_"

    fun convertToKey(key: String, isBase64: Boolean = false): SecretKey {
        val encodedKey = if (isBase64) key else key.encodeBase64()

        val decodedKey: ByteArray = Base64.decode(encodedKey, Base64.NO_WRAP)
        val str = String(decodedKey, Charsets.US_ASCII)
        str.logD("$TAG convertToKey decodedKey")
        return SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun convertKey(secretKey: SecretKey): String {
        val rawData = secretKey.encoded
        return Base64.encodeToString(rawData, Base64.NO_WRAP)
    }


    fun ByteArray.encodeBase64Replaced(): String =
        Base64.encodeToString(this, Base64.NO_WRAP).replace(PATH_SPECIAL, PATH_SPECIAL_REPLACEMENT)

    fun String.encodeBase64(): String =
        Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)

    fun ByteArray.toStringASCII(): String =
        String(this, Charsets.US_ASCII)

    fun String.fromBase64Replaced(): ByteArray =
        Base64.decode(this.replace(PATH_SPECIAL_REPLACEMENT, PATH_SPECIAL), Base64.NO_WRAP)


    @Throws(Exception::class)
    fun encrypt(yourKey: SecretKey, data: ByteArray): ByteArray {
        val keyEncoded = yourKey.encoded
        val sKeySpec = SecretKeySpec(keyEncoded, 0, keyEncoded.size, ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM, ALGORITHM_PROVIDER)
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(data)
    }

    fun encrypt(yourKey: SecretKey, data: String): ByteArray =
        encrypt(yourKey, data.toByteArray())

    @Throws(Exception::class)
    fun decrypt(yourKey: SecretKey, data: ByteArray): ByteArray {
        val decrypted: ByteArray
        val cipher = Cipher.getInstance(ALGORITHM, ALGORITHM_PROVIDER)
        cipher.init(Cipher.DECRYPT_MODE, yourKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        decrypted = cipher.doFinal(data)
        return decrypted
    }

    fun decryptToString(yourKey: SecretKey, data: ByteArray): String =
        decrypt(yourKey, data).toStringASCII()

    fun decrypt(yourKey: SecretKey, data: String): ByteArray =
        decrypt(yourKey, data.fromBase64Replaced())

    fun decryptToString(yourKey: SecretKey, data: String): String =
        decrypt(yourKey, data.fromBase64Replaced()).toStringASCII()
}