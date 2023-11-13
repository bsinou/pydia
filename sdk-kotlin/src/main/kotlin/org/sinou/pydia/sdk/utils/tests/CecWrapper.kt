package org.sinou.pydia.sdk.utils.tests

import org.sinou.pydia.sdk.utils.Log
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

/**
 * In order to simplify dependency management, we do not provide an implementation of the S3 client
 * in this layer. It is thus not possible to upload or download files directly in this layer.
 *
 *
 * To workaround this issue and enable integration tests we provide a simple wrapper around the
 * Cells Client (cec), a go binary that provides the packaged client a single binary.
 *
 *
 * It is for test only and might prove buggy, so use at your own risk.
 */
class CecWrapper {
    private val logTag = "CecWrapper"
    private var basePath: String? = null
    private var cecCmd: String? = null

    @Throws(IOException::class)
    fun setUpCec(prepareCmd: String?, workingDir: String, propsFile: String?) {
        try {
            val builder = ProcessBuilder(prepareCmd, TestUtils.getOS(), workingDir, propsFile)
            builder.redirectErrorStream(true)
            Log.i(logTag, "... Launching prepare-cec script")
            val process = builder.start()
            val returnValue = process.waitFor()
            Log.i(logTag, "## After running prepare-cec script. Exit code: $returnValue")
            displayOutput(process)
            if (returnValue != 0) {
                throw RuntimeException("Execution of Setup failed with code $returnValue")
            }
            basePath = workingDir
            cecCmd = workingDir + File.separator + "cells-client"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    fun callCommand(vararg args: String): Int {
        if (cecCmd == null) {
            throw RuntimeException("You must call setup command before trying to use the cells client")
        }
        val builder = ProcessBuilder(*buildArgs(*args))
        builder.redirectErrorStream(true)
        val process = builder.start()
        val returnValue = process.waitFor()
        displayOutput(process)
        return if (returnValue != 0) {
            throw RuntimeException("Execution of command " + args[0] + " failed with code " + returnValue)
        } else 0
    }

    private fun buildArgs(vararg args: String): Array<String?> {
        val args2 = arrayOfNulls<String>(args.size + 1)
        args2[0] = cecCmd
        var i = 1
        for (elem in args) {
            args2[i] = elem
            i++
        }
        return args2
    }

    @Throws(Exception::class)
    private fun displayOutput(process: Process) {
        process.inputStream.use { out ->
            val reader = BufferedReader(InputStreamReader(out))
            var line: String
            while (reader.readLine().also { line = it } != null) {
                println("Stdout: $line")
            }
        }
    }
}