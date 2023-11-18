package org.sinou.pydia.client.core.util

import android.util.Base64
import org.sinou.pydia.sdk.api.CustomEncoder
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AndroidCustomEncoder : CustomEncoder {
    override fun base64Encode(inValue: ByteArray): ByteArray {
        return Base64.encode(
            inValue,
            Base64.DEFAULT or Base64.NO_WRAP or Base64.NO_PADDING or Base64.NO_CLOSE
        )
    }

    override fun base64Encode(inValue: String): String {
        return String(base64Encode(inValue.toByteArray()))
    }

    override fun base64Decode(inValue: ByteArray): ByteArray {
        return Base64.decode(
            inValue,
            Base64.DEFAULT or Base64.NO_WRAP or Base64.NO_PADDING or Base64.NO_CLOSE
        )
    }

    override fun base64Decode(inValue: String): String {
        val data = inValue.toByteArray(StandardCharsets.UTF_8)
        return String(base64Decode(data), StandardCharsets.UTF_8)
    }

    override fun utf8Encode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Unexpected encoding issue", e)
        }
    }

    override fun utf8Decode(value: String): String {
        return try {
            URLDecoder.decode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Unexpected decoding issue", e)
        }
    }

    override fun getUTF8Bytes(str: String): ByteArray {
        return str.toByteArray(StandardCharsets.UTF_8)
    }
}