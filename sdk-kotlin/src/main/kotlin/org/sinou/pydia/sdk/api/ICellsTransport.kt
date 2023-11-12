package org.sinou.pydia.sdk.api

import java.net.HttpURLConnection

/**
 * Enriches the base Transport interface with new methods
 * that are not meant to ever be supported by the legacy Pydio 8
 */
interface ICellsTransport : Transport {
    @Throws(SDKException::class)
    fun withAuth(con: HttpURLConnection): HttpURLConnection
}