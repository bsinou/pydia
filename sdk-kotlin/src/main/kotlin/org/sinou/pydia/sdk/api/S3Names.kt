package org.sinou.pydia.sdk.api

interface S3Names {
    companion object {
        const val S3_TOKEN_KEY = "pydio_jwt"
        const val PYDIO_S3_THUMBSTORE_PREFIX = "pydio-thumbstore"
        const val RESPONSE_CONTENT_TYPE = "response-content-type"
        const val S3_CONTENT_TYPE_OCTET_STREAM = "application/octet-stream"
        const val S3_CONTENT_TYPE_MP4 = "video/mp4"
        const val S3_CONTENT_TYPE_MP3 = "audio/mp3"
    }
}