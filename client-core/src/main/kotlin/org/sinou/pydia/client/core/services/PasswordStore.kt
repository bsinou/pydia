package org.sinou.pydia.client.core.services

import org.sinou.pydia.client.core.db.auth.LegacyCredentialsDao
import org.sinou.pydia.client.core.db.auth.RLegacyCredentials
import org.sinou.pydia.sdk.api.Store

class PasswordStore(private val dao: LegacyCredentialsDao) : Store<String> {

    override fun put(id: String, password: String) {
        val cred = RLegacyCredentials(accountID = id, password = password)
        if (dao.getCredential(id) == null) {
            dao.insert(cred)
        } else {
            dao.update(cred)
        }
    }

    override fun get(id: String): String? {
        return dao.getCredential(id)?.password
    }

    override fun remove(id: String) {
        dao.forgetPassword(id)
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun getAll(): MutableMap<String, String> {
        val allCredentials: MutableMap<String, String> = HashMap()
        for (cred in dao.getAll()) {
            allCredentials[cred.accountID] = cred.password
        }
        return allCredentials
    }
}
