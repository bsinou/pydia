package org.sinou.android.pydia.services

import com.pydio.cells.api.Store
import com.pydio.cells.transport.auth.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.db.account.RToken

class TokenStore(private val accountService: AccountService) : Store<Token> {

    private var tokenStoreJob = Job()
    private val tokenStoreScope = CoroutineScope(Dispatchers.IO + tokenStoreJob)

    private val dao = accountService.accountDB.tokenDao()

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
        if (dao.getToken(id) == null) {
            dao.insert(rToken)
        } else {
            dao.update(rToken)
        }
    }

    override fun get(id: String): Token? {
        val rToken = dao.getToken(id)
        var token: Token? = null
        if (rToken != null) {
            token = SessionFactory.fromRToken(rToken)
        }
        return token
    }

    override fun remove(id: String) {

        // Rather use Account service
        // dao.forgetToken(id)
        // TODO generalize and factorize tall this
        tokenStoreScope.launch {
            accountService.logoutAccount(id)
        }
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun getAll(): MutableMap<String, Token> {
        val allCreds: MutableMap<String, Token> = HashMap()
        for (token in dao.getAll()) {
            allCreds[token.accountID] = SessionFactory.fromRToken(token)
        }
        return allCreds
    }
}