package org.sinou.pydia.sdk.utils

import org.sinou.pydia.sdk.api.CustomEncoder
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

class KotlinCustomEncoder : CustomEncoder {
    override fun base64Encode(inValue: ByteArray): ByteArray {
        Base64.getEncoder().encode(inValue)
        return inValue
    }

    override fun base64Encode(inValue: String): String {
        val asBytes = inValue.toByteArray(Charsets.UTF_8)
        val encoded = base64Encode(asBytes)
        return encoded.toString(Charsets.UTF_8)
    }

    override fun base64Decode(inValue: ByteArray): ByteArray {
        return Base64.getDecoder().decode(inValue)
    }

    override fun base64Decode(inValue: String): String {
        val asBytes = inValue.toByteArray(Charsets.UTF_8)
        val decoded = base64Decode(asBytes)
        return decoded.toString(Charsets.UTF_8)
    }

    override fun utf8Encode(value: String): String {
        // TODO this method throws an exception in android context but not in the sdk-java, why ?
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Unexpected encoding issue", e)
        }
    }

    override fun utf8Decode(value: String): String {
        // TODO this method throws an exception in android context but not in the sdk-java, why ?
        return try {
            URLDecoder.decode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Unexpected decoding issue", e)
        }
        //return super.utf8Encode(value);
    }

    override fun getUTF8Bytes(str: String): ByteArray {
        return str.toByteArray(StandardCharsets.UTF_8)
    }
}