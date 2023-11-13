package org.sinou.pydia.sdk.api

import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

/**
 * Wraps an URL to add convenience methods to ease implementation in a Pydio Context.
 */
interface ServerURL {

    val id: String
        /**
         * Returns a standard id that has been cleaned and normalized.
         */
        get() = url.protocol + "://" + url.authority + url.path

    /**
     * Returns the underlying URL Object to perform various checks and/or access to its properties.
     * The returned object should not be used for server request to avoid concurrency issues.
     * Rather see ServerURL.withPath()
     */
    val url: URL

    /**
     * Returns a new instance of the current Server URL object that points to a given path
     * and that is ready to use to connect to the distant server.
     * If any query was already present, it is conserved.
     *
     *
     * Warning: if our server entry point is under a sub path of the domain (typically for P8),
     * the passed path is appended.
     */
    @Throws(MalformedURLException::class)
    fun withPath(path: String): ServerURL

    /**
     * Returns a new instance of the current Server URL object with the passed query.
     *
     *
     * Note that the path is preserved (if any) but this overrides any query
     * that might have been previously set.
     */
    @Throws(MalformedURLException::class)
    fun withQuery(query: String): ServerURL

    /**
     * Returns a new instance of the current Server URL object augmented with Path and Query
     * passed via the spec String.
     *
     *
     * Warning: if our server entry point is under a sub path of the domain (typically for P8),
     * the passed path is appended.
     */
    @Throws(MalformedURLException::class)
    fun withSpec(spec: String): ServerURL

    @Throws(IOException::class)
    fun openConnection(): HttpURLConnection

    @Throws(IOException::class, SDKException::class)
    fun ping()

    val certificateChain: Array<ByteArray>?
    val sSLContext: SSLContext?

    fun getSslSocketFactory() : SSLSocketFactory?

    fun skipVerify(): Boolean
    fun toJson(): String?
}