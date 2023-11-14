package org.sinou.pydia.client.core.db.auth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.sinou.pydia.sdk.transport.StateID
import org.sinou.pydia.sdk.transport.auth.Token

@Entity(tableName = "tokens")
data class RToken(

    @PrimaryKey
    @ColumnInfo(name = "account_id") val accountId: String,

    // value is the real useful token => access_token in OAuth2
    @ColumnInfo(name = "value") val value: String,

    // Set by Cells layers to contain the corresponding encoded accountID
    @ColumnInfo(name = "subject") val subject: String?,

    // idToken contains encoded information about current session, typically the claims
    // It is null when dealing with P8 legacy tokens
    @ColumnInfo(name = "id_token") val idToken: String?,

    @ColumnInfo(name = "scope") val scope: String?,

    @ColumnInfo(name = "token_type") val tokenType: String,

    @ColumnInfo(name = "refresh_token") val refreshToken: String?,

    @ColumnInfo(name = "expires_in") val expiresIn: Long = 0L,

    @ColumnInfo(name = "expiration_time") val expirationTime: Long = 0L,

    @ColumnInfo(name = "refreshing_since_ts") var refreshingSinceTs: Long = 0L,

    // valid, expired, refreshing...
    // @ColumnInfo(name = "status") val status: Int,
) {
    fun toToken(): Token {

        val currToken = Token()
        currToken.tokenType = tokenType
        currToken.value = value
        currToken.subject = subject ?: accountId
        currToken.expiresIn = expiresIn
        currToken.expirationTime = expirationTime
        currToken.idToken = idToken
        currToken.refreshToken = refreshToken
        currToken.scope = scope
        return currToken
    }

    fun getAccountID(): StateID = StateID.safeFromId(accountId)

    companion object {
        fun fromToken(accountId: String, token: Token): RToken {
            return RToken(
                accountId = accountId,
                idToken = token.idToken,
                subject = token.subject,
                value = token.value ?: throw IllegalArgumentException("value must not be null"),
                expiresIn = token.expiresIn,
                expirationTime = token.expirationTime,
                scope = token.scope,
                refreshToken = token.refreshToken,
                tokenType = token.tokenType
                    ?: throw IllegalArgumentException("type must not be null"),
                refreshingSinceTs = token.refreshingSinceTs,
            )
        }
    }
}
