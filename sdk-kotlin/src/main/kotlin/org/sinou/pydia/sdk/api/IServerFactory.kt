package org.sinou.pydia.sdk.api

import org.sinou.pydia.sdk.transport.StateID

interface IServerFactory {

    /**
     * Cache the server object to be later retrieved by url
     *
     * @param serverURL
     * @return
     * @throws SDKException
     */
    @Throws(SDKException::class)
    fun registerServer(serverURL: ServerURL): Server

    /**
     * Register an account with the provided credentials.
     * The factory can then provide Transport objects with the corresponding AccountId
     *
     * @param serverURL
     * @param credentials
     * @throws SDKException
     */
    @Throws(SDKException::class)
    fun registerAccountCredentials(serverURL: ServerURL, credentials: Credentials): StateID

    /**
     * Cleanly unregister an account, among others, this deletes the relate tokens from the token store.
     *
     * @param accountID
     * @throws SDKException
     */
    @Throws(SDKException::class)
    fun unregisterAccount(accountID: StateID)

    /**
     * Simply get a well-configured transport for this account.
     * `registerAccount()` must have been called before at least once.
     *
     * @param accountID
     * @return
     * @throws SDKException
     */
    @Throws(SDKException::class)
    fun getTransport(accountID: StateID): Transport?

    /**
     * Until we find a better option, it is the factory responsibility to provide an encoder
     * that fits with the current runtime (typically Android or plain Java).
     *
     * @return
     */
    fun getEncoder(): CustomEncoder
}