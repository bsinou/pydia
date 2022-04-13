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

class TokenStore(private val dao: TokenDao) : Store<Token> {

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
        dao.deleteToken(id)
    }

    override fun clear() {
        dao.deleteAllToken()
    }

    override fun getAll(): MutableMap<String, Token> {
        val allCredentials: MutableMap<String, Token> = HashMap()
        for (rToken in dao.getAll()) {
            allCredentials[rToken.accountID] = rToken.toToken()
        }
        return allCredentials
    }
}
