package ir.romroid.secureboxrecorder.utils

import java.io.*

object FileUtils {
    @Throws(Exception::class)
    fun readFile(inputStream: InputStream): ByteArray {
        val fileContents = inputStream.readBytes()
        val inputBuffer = BufferedInputStream(
            inputStream
        )

        inputBuffer.read(fileContents)
        inputBuffer.close()

        return fileContents
    }

    @Throws(Exception::class)
    fun readFile(filePath: String): ByteArray {
        val file = File(filePath)
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(
            FileInputStream(file)
        )

        inputBuffer.read(fileContents)
        inputBuffer.close()

        return fileContents
    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, outputStream: OutputStream) {
        outputStream.write(fileData)
        outputStream.flush()
        outputStream.close()
    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, path: String) {
        val file = File(path)
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }

}