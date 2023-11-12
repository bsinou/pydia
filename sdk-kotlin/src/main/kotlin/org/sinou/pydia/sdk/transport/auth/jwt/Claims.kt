package org.sinou.pydia.sdk.transport.auth.jwt

class Claims {
    var atHash: String? = null
    var aud: Array<String> = arrayOf()
    var authTime = 0
    var exp = 0
    var iat: String? = null
    var iss: String? = null
    var jti: String? = null
    var name: String? = null
    var nonce: String? = null
    var rat = 0
    var sid: String? = null
    var sub: String? = null
}