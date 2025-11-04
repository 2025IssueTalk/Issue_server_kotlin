package com.issuetalk.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.issuetalk.auth.config.JwtConfig
import com.issuetalk.auth.model.User
import java.time.Instant
import java.util.Date

class JwtService(
    private val config: JwtConfig
) {
    private val algorithm: Algorithm = Algorithm.HMAC256(config.secret)
    private val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    val realm: String get() = config.realm
    val issuer: String get() = config.issuer
    val audience: String get() = config.audience
    val accessTokenValiditySeconds: Long get() = config.accessTokenValidityMs / 1000

    fun verifier(): JWTVerifier = verifier

    fun generateToken(user: User): String {
        val expiresAt = Instant.now().plusMillis(config.accessTokenValidityMs)
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(user.id.toString())
            .withClaim("userId", user.id.toString())
            .withClaim("email", user.email)
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }
}
