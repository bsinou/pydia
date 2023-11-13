package org.sinou.pydia.sdk.api

import java.net.URL

interface S3Client {

    @Throws(SDKException::class)
    fun getUploadPreSignedURL(ws: String, folder: String, name: String): URL

    @Throws(SDKException::class)
    fun getDownloadPreSignedURL(ws: String, file: String): URL

    @Throws(SDKException::class)
    fun getStreamingPreSignedURL(slug: String, file: String, contentType: String): URL

}