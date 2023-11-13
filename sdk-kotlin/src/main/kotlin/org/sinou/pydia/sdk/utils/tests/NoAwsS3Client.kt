package org.sinou.pydia.sdk.utils.tests

import org.sinou.pydia.sdk.api.S3Client
import java.net.URL

/**
 * Temporary workaround the issue of AWS dependencies to provide
 * a reasonable default when working with a legacy setup.
 */
class NoAwsS3Client : S3Client {

    override fun getUploadPreSignedURL(ws: String, folder: String, name: String): URL {
        throw RuntimeException("Unsupported call")
    }

    override fun getDownloadPreSignedURL(ws: String, file: String): URL {
        throw RuntimeException("Unsupported call")
    }

    override fun getStreamingPreSignedURL(slug: String, file: String, contentType: String): URL {
        throw RuntimeException("Unsupported call")
    }
}