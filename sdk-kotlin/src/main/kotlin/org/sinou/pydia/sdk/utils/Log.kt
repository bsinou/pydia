package org.sinou.pydia.sdk.utils

import java.io.File

private enum class Levels {
    DEBUG, INFO, WARN, ERROR
}

object Log {
    // Temporary tags
    const val TAG_SDK = "CELLS/SDK"
    private var logger: Logger? = null
    fun setLogger(l: Logger?) {
        logger = l
    }

    private const val ANSI_RESET = "\u001B[0m"
    private const val FONT_BLACK = "\u001B[30m"
    private const val FONT_WHITE = "\u001B[47m"
    private const val BG_DANGER = "\u001B[41m"
    private const val BG_WARNING = "\u001B[43m"
    private const val BG_OK = "\u001B[47m"

    private fun getStatusMsg(level: Levels): String {

        var msg = when (level) {
            Levels.DEBUG -> "DEBUG"
            Levels.INFO -> "INFO"
            Levels.WARN -> "WARN"
            Levels.ERROR -> "ERROR"
        }
        if (unixLike()) {
            val (color, bgColor) = when (level) {
                Levels.DEBUG -> FONT_BLACK to BG_OK
                Levels.INFO -> FONT_BLACK to BG_OK
                Levels.WARN -> FONT_BLACK to BG_WARNING
                Levels.ERROR -> FONT_WHITE to BG_DANGER
            }
            msg = "$bgColor $color $msg $ANSI_RESET $ANSI_RESET"

        }
        return msg
    }

    fun e(tag: String, msg: String) {
        logger?.let {
            it.e(tag, msg)
            return
        }
        println("${getStatusMsg(Levels.ERROR)} - $tag\t$msg")
    }

    fun w(tag: String, msg: String) {
        logger?.let {
            it.w(tag, msg)
            return
        }
        println("${getStatusMsg(Levels.WARN)} - tag\t$msg")
    }

    fun i(tag: String, msg: String) {
        val let = logger?.let {
            it.i(tag, msg)
            return
        }
        println("${getStatusMsg(Levels.INFO)} - $tag\t$msg")
    }

    fun d(tag: String, msg: String) {
        logger?.let {
            it.d(tag, msg)
            return
        }
        println("${getStatusMsg(Levels.DEBUG)} - $tag\t$msg")
    }

    fun v(tag: String, msg: String) {
        if (logger != null) {
            logger!!.v(tag, msg)
            return
        }
        println("${getStatusMsg(Levels.DEBUG)} - $tag\t$msg")
    }

    fun paramString(params: Map<String, String>): String {
        val builder = StringBuilder()
        params.forEach { (k, v) -> builder.append(" $k=$v") }
        return if (builder.isEmpty()) {
            ""
        } else {
            builder.substring(1)
        }
    }

    private fun unixLike(): Boolean {
        return File.separator == "/"
    }

    interface Logger {
        fun e(tag: String?, text: String?)
        fun i(tag: String?, text: String?)
        fun v(tag: String?, text: String?)
        fun d(tag: String?, text: String?)
        fun w(tag: String?, text: String?)
    }
}