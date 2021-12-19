package org.sinou.android.pydia.services

import com.pydio.cells.api.Server
import com.pydio.cells.api.Store
import com.pydio.cells.api.Transport
import com.pydio.cells.client.CellsClient
import com.pydio.cells.client.ClientFactory
import com.pydio.cells.transport.CellsTransport
import com.pydio.cells.transport.auth.CredentialService
import com.pydio.cells.transport.auth.Token
import org.sinou.android.pydia.room.account.AccountDatabase
import org.sinou.android.pydia.room.account.LegacyCredentials
import org.sinou.android.pydia.room.account.RToken

class SessionFactory(
    accountDB: AccountDatabase,
    serverStore: Store<Server?>,
    transportStore: Store<Transport?>
) : ClientFactory(credService(accountDB), serverStore, transportStore) {

    companion object {
        fun credService(accountDB: AccountDatabase): CredentialService {
            return CredentialService(TokenStore(accountDB), PasswordStore(accountDB))
        }
    }

    override fun getCellsClient(transport: CellsTransport?): CellsClient? {
        return CellsClient(transport, S3Client(transport))
    }


    class PasswordStore(private val accountDB: AccountDatabase) : Store<String> {

        override fun put(id: String, password: String) {
            val cred = LegacyCredentials(accountID = id, password = password)
            accountDB.legacyCredentialsDao().insert(cred)
        }

        override fun get(id: String): String? {
            return accountDB.legacyCredentialsDao().getCredential(id)?.password
        }

        override fun remove(id: String) {
            // TODO
        }

        override fun clear() {
            TODO("Not yet implemented")
        }

        override fun getAll(): MutableMap<String, String> {
            throw RuntimeException("foridden action: cannot list all password")
        }

    }

    class TokenStore(private val accountDB: AccountDatabase) : Store<Token> {

        override fun put(id: String, token: Token) {

            val rToken = RToken(
                accountID = id,
                idToken = token.idToken,
                subject = token.subject,
                value = token.value,
                expiresIn = token.expiresIn,
                expirationTime = token.expirationTime,
                scope = token.scope,
                refreshToken = token.refreshToken,
                tokenType = token.tokenType
            )
            accountDB.tokenDao().insert(rToken)
        }

        override fun get(id: String): Token? {
            val rToken = accountDB.tokenDao().getToken(id)
            var token: Token? = null
            if (rToken != null) {
                token = Token()
                token.value = rToken.value

            }
            return token
        }

        override fun remove(id: String) {
            accountDB.tokenDao().delete(id)
        }

        override fun clear() {
            TODO("Not yet implemented")
        }

        override fun getAll(): MutableMap<String, Token> {
            throw RuntimeException("forbidden action: cannot list all tokens")
        }
    }

}
