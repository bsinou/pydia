package org.sinou.android.pydia.services

import com.pydio.cells.api.Store
import com.pydio.cells.transport.auth.Token
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sinou.android.pydia.CellsApp
import org.sinou.android.pydia.db.accounts.RToken
import org.sinou.android.pydia.db.accounts.TokenDao

class TokenStore(
    private val dao: TokenDao
    // private val accountService: AccountService,
) : Store<Token> {

    private var tokenStoreJob = Job()
    private val tokenStoreScope = CoroutineScope(Dispatchers.IO + tokenStoreJob)


    override fun put(id: String, token: Token) {
        val rToken = RToken.fromToken(id, token)
        if (dao.getToken(id) == null) {
            dao.insert(rToken)
        } else {
            dao.update(rToken)
        }
    }

    override fun get(id: String): Token? {
        val rToken = dao.getToken(id)
        if (rToken != null) {
            return rToken.toToken()
        }
        return null
    }

    override fun remove(id: String) {
        tokenStoreScope.launch {
            // TODO("We should avoid to retrieve the service this way: it prevents DI")
            CellsApp.instance.accountService.logoutAccount(id)
        }
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun getAll(): MutableMap<String, Token> {
        val allCredentials: MutableMap<String, Token> = HashMap()
        for (rToken in dao.getAll()) {
            allCredentials[rToken.accountID] = rToken.toToken()
        }
        return allCredentials
    }
}
