package org.sinou.pydia.sdk.utils

import org.sinou.pydia.sdk.api.SDKException
import org.sinou.pydia.sdk.api.callbacks.ProgressListener
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection

/**
 * Legacy inherited static helpers to simplify implementation of stream related methods
 * and `Exception` management.
 */
object IoHelpers {
    var bufferSize = 4096

    @Throws(IOException::class)
    fun consume(`in`: InputStream) {
        val buffer = ByteArray(bufferSize)
        var read = 0
        while (read != -1) {
            read = `in`.read(buffer)
        }
    }

    fun closeQuietly(`in`: InputStream?) {
        try {
            `in`?.close()
        } catch (ignored: Exception) {
        }
    }

    fun closeQuietly(out: OutputStream?) {
        try {
            out?.close()
        } catch (ignored: Exception) {
        }
    }

    fun closeQuietly(con: HttpURLConnection?) {
        try {
            con?.disconnect()
        } catch (ignored: Exception) {
        }
    }

    @Throws(IOException::class)
    fun pipeRead(inputStream: InputStream, out: OutputStream): Long {
        val buffer = ByteArray(bufferSize)
        var totalRead: Long = 0
        var read = 0
        while (read > -1) {
            totalRead += read.toLong()
            out.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        return totalRead
    }

    @Throws(IOException::class)
    fun readToString (inputStream: InputStream) : String{
        val reader = BufferedReader(inputStream.reader())
        val content = StringBuilder()
        reader.use { reader ->
            var line = reader.readLine()
            while (line != null) {
                content.append(line)
                line = reader.readLine()
            }
        }
        return content.toString()
    }


    @Throws(IOException::class)
    fun write(bytes: ByteArray?, out: OutputStream): Long {
        val `in`: InputStream = ByteArrayInputStream(bytes)
        val buffer = ByteArray(bufferSize)
        var total_read: Long = 0
        var read: Int
        while (true) {
            read = `in`.read(buffer)
            if (read == -1) break
            total_read += read.toLong()
            out.write(buffer, 0, read)
        }
        closeQuietly(`in`)
        return total_read
    }

    @Throws(IOException::class, SDKException::class)
    fun pipeReadWithIncrementalProgress(
        `in`: InputStream,
        out: OutputStream,
        listener: ProgressListener?
    ): Long {
        if (listener == null) {
            return pipeRead(`in`, out)
        }
        val buffer = ByteArray(bufferSize)
        var total_read: Long = 0
        var read = 0
        while (read > -1) {
            total_read += read.toLong()
            out.write(buffer, 0, read)
            val cancelMsg = listener.onProgress(read.toLong())
            if (Str.notEmpty(cancelMsg)) {
                throw SDKException.cancel(cancelMsg)
            }
            read = `in`.read(buffer)
        }
        return total_read
    }

    @Throws(IOException::class, SDKException::class)
    fun pipeReadWithProgress(
        `in`: InputStream,
        out: OutputStream,
        listener: ProgressListener?
    ): Long {
        if (listener == null) {
            return pipeRead(`in`, out)
        }
        val buffer = ByteArray(bufferSize)
        var total_read: Long = 0
        var read = 0
        while (read > -1) {
            total_read += read.toLong()
            out.write(buffer, 0, read)
            val cancelMsg = listener.onProgress(read.toLong())
            if (Str.notEmpty(cancelMsg)) {
                throw SDKException.cancel(cancelMsg)
            }
            read = `in`.read(buffer)
        }
        return total_read
    }

    @Throws(IOException::class)
    fun readFile(path: String?): ByteArray {
        val out = ByteArrayOutputStream()
        val `in`: InputStream = FileInputStream(path)
        val buffer = ByteArray(bufferSize)
        // long total_read = 0;
        var read: Int
        while (true) {
            read = `in`.read(buffer)
            if (read == -1) break
            //  total_read += read;
            out.write(buffer, 0, read)
        }
        closeQuietly(`in`)
        return out.toByteArray()
    }

    @Throws(IOException::class)
    fun writeFile(bytes: ByteArray?, filepath: String?): Long {
        val out: OutputStream = FileOutputStream(filepath)
        val `in`: InputStream = ByteArrayInputStream(bytes)
        val buffer = ByteArray(bufferSize)
        var total_read: Long = 0
        var read: Int
        while (true) {
            read = `in`.read(buffer)
            if (read == -1) break
            total_read += read.toLong()
            out.write(buffer, 0, read)
        }
        closeQuietly(out)
        return total_read
    }

    @Throws(IOException::class)
    fun writeFile(`in`: InputStream, filepath: String?): Long {
        var total_read: Long = 0
        FileOutputStream(filepath).use { out ->
            val buffer = ByteArray(bufferSize)
            var read: Int
            while (true) {
                read = `in`.read(buffer)
                if (read == -1) break
                total_read += read.toLong()
                out.write(buffer, 0, read)
            }
        }
        return total_read
    }
}