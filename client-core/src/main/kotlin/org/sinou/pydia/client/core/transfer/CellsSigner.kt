/*
 * Copyright 2013-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sinou.pydia.client.core.transfer

import com.amazonaws.AmazonClientException
import com.amazonaws.Request
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AwsChunkedEncodingInputStream
import com.amazonaws.services.s3.Headers
import com.amazonaws.util.BinaryUtils
import java.io.IOException

/**
 * AWS4 signer implementation for AWS S3 forked and adapted to work with Cells v4.1
 *
 * We don't use double-url-encode path elements; S3 expects path elements to be  encoded only once in the canonical URI.
 */
class CellsSigner : AWS4Signer(false) {

    /**
     * If necessary, creates a chunk-encoding wrapper on the request payload.
     */
    override fun processRequestPayload(
        request: Request<*>,
        headerSigningResult: HeaderSigningResult
    ) {
        if (useChunkEncoding(request)) {
            val payloadStream = request.content
            val dateTime = headerSigningResult.dateTime
            val keyPath = headerSigningResult.scope
            val kSigning = headerSigningResult.kSigning
            val signature = BinaryUtils.toHex(headerSigningResult.signature)
            val chunkEncodedStream = AwsChunkedEncodingInputStream(
                payloadStream, kSigning, dateTime, keyPath, signature, this
            )
            request.content = chunkEncodedStream
        }
    }

    override fun calculateContentHashPresign(request: Request<*>?): String {
        return "UNSIGNED-PAYLOAD"
    }

    /**
     * Returns the pre-defined header value and set other necessary headers if
     * the request needs to be chunk-encoded. Otherwise calls the superclass
     * method which calculates the hash of the whole content for signing.
     */
    override fun calculateContentHash(request: Request<*>): String {
        // To be consistent with other service clients using sig-v4,
        // we just set the header as "required", and AWS4Signer.sign() will be
        // notified to pick up the header value returned by this method.
        request.addHeader("x-amz-content-sha256", "required")
        if (useChunkEncoding(request)) {
            val contentLength = request.headers[Headers.CONTENT_LENGTH]
            val originalContentLength= contentLength?.toLong()
                ?:
                        /**
                         * "Content-Length" header could be missing if the caller is
                         * uploading a stream without setting Content-Length in
                         * ObjectMetadata. Before using sigv4, we rely on HttpClient to
                         * add this header by using BufferedHttpEntity when creating the
                         * HttpRequest object. But now, we need this information
                         * immediately for the signing process, so we have to cache the
                         * stream here.
                         */
                        try {
                            getContentLength(request)
                        } catch (e: IOException) {
                            throw AmazonClientException(
                                "Cannot get the content lenght of the request content.",
                                e
                            )
                        }
            //            request.addHeader("x-amz-decoded-content-length",
//                    Long.toString(originalContentLength));

            // Make sure "Content-Length" header is not empty so that HttpClient
            // won't cache the stream again to recover Content-Length
            request.addHeader(
                Headers.CONTENT_LENGTH,
                AwsChunkedEncodingInputStream
                    .calculateStreamContentLength(originalContentLength).toString()
            )
            return CONTENT_SHA_256
        }
        return super.calculateContentHash(request)
    }

    companion object {
        private const val CONTENT_SHA_256 = "AWS4-HMAC-SHA256"
        private const val DEFAULT_BYTE_LENGTH = 4096
        const val CELLS_SIGNER_ID = "CellsSigner"

        /**
         * Determine whether to use aws-chunked for signing
         * => we rather never use chunk encoded
         */
        private fun useChunkEncoding(request: Request<*>): Boolean {
            // Whether to use chunked encoding for signing the request
//        boolean chunkedEncodingEnabled = false;
//        if (request.getOriginalRequest() instanceof PutObjectRequest
//                || request.getOriginalRequest() instanceof UploadPartRequest) {
//            chunkedEncodingEnabled = true;
//        }
//        return chunkedEncodingEnabled;
            return false
        }

        /**
         * Read the content of the request to get the length of the stream. This
         * method will wrap the stream by RepeatableInputStream if it is not
         * mark-supported.
         */
        @Throws(IOException::class)
        fun getContentLength(request: Request<*>): Long {
            val content = request.content
            if (!content.markSupported()) {
                throw AmazonClientException("Failed to get content length")
            }
            var contentLength: Long = 0
            val tmp = ByteArray(DEFAULT_BYTE_LENGTH)
            var read: Int
            content.mark(-1)
            while (content.read(tmp).also { read = it } != -1) {
                contentLength += read.toLong()
            }
            content.reset()
            return contentLength
        }
    }
}
