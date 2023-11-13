package org.sinou.pydia.sdk.utils

import org.sinou.pydia.sdk.api.SDKException
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

    private var bufferSize = 4096

    @Throws(IOException::class)
    fun consume(input: InputStream) {
        val buffer = ByteArray(bufferSize)
        var read = 0
        while (read != -1) {
            read = input.read(buffer)
        }
    }

    fun closeQuietly(input: InputStream?) {
        try {
            input?.close()
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
    fun readToString(inputStream: InputStream): String {
        val reader = BufferedReader(inputStream.reader())
        val content = StringBuilder()
        reader.use {
            var line = it.readLine()
            while (line != null) {
                content.append(line)
                line = it.readLine()
            }
        }
        return content.toString()
    }


    @Throws(IOException::class)
    fun write(bytes: ByteArray?, out: OutputStream): Long {
        val input: InputStream = ByteArrayInputStream(bytes)
        val buffer = ByteArray(bufferSize)
        var totalRead: Long = 0
        var read: Int
        while (true) {
            read = input.read(buffer)
            if (read == -1) break
            totalRead += read.toLong()
            out.write(buffer, 0, read)
        }
        closeQuietly(input)
        return totalRead
    }

    @Throws(IOException::class, SDKException::class)
    fun pipeReadWithIncrementalProgress(
        input: InputStream,
        out: OutputStream,
        progress: ((Long) -> String?)?
    ): Long {
        if (progress == null) {
            return pipeRead(input, out)
        }
        val buffer = ByteArray(bufferSize)
        var totalRead: Long = 0
        var read = 0
        while (read > -1) {
            totalRead += read.toLong()
            out.write(buffer, 0, read)
            // val cancelMsg =
            progress(read.toLong())?.let {
                if (it.isNotEmpty())
                    throw SDKException.cancel(it)
            }
            read = input.read(buffer)
        }
        return totalRead
    }

    @Throws(IOException::class, SDKException::class)
    fun pipeReadWithProgress(
        input: InputStream,
        out: OutputStream,
        progress: ((Long) -> String?)?
    ): Long {
        if (progress == null) {
            return pipeRead(input, out)
        }
        val buffer = ByteArray(bufferSize)
        var totalRead: Long = 0
        var read = 0
        while (read > -1) {
            totalRead += read.toLong()
            out.write(buffer, 0, read)
            progress(read.toLong())?.let {
                if (it.isNotEmpty()) throw SDKException.cancel(it)
            }
            read = input.read(buffer)
        }
        return totalRead
    }

    @Throws(IOException::class)
    fun readFile(path: String): ByteArray {
        val out = ByteArrayOutputStream()
        val input: InputStream = FileInputStream(path)
        val buffer = ByteArray(bufferSize)
        // long total_read = 0;
        var read: Int
        while (true) {
            read = input.read(buffer)
            if (read == -1) break
            //  total_read += read;
            out.write(buffer, 0, read)
        }
        closeQuietly(input)
        return out.toByteArray()
    }

    @Throws(IOException::class)
    fun writeFile(bytes: ByteArray, filepath: String): Long {
        val out: OutputStream = FileOutputStream(filepath)
        val input: InputStream = ByteArrayInputStream(bytes)
        val buffer = ByteArray(bufferSize)
        var totalRead: Long = 0
        var read: Int
        while (true) {
            read = input.read(buffer)
            if (read == -1) break
            totalRead += read.toLong()
            out.write(buffer, 0, read)
        }
        closeQuietly(out)
        return totalRead
    }

    @Throws(IOException::class)
    fun writeFile(input: InputStream, filepath: String): Long {
        var totalRead: Long = 0
        FileOutputStream(filepath).use { out ->
            val buffer = ByteArray(bufferSize)
            var read: Int
            while (true) {
                read = input.read(buffer)
                if (read == -1) break
                totalRead += read.toLong()
                out.write(buffer, 0, read)
            }
        }
        return totalRead
    }
}