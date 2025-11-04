package com.issuetalk.auth.plugins

import com.issuetalk.auth.dto.ErrorResponse
import com.issuetalk.auth.service.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.time.Instant

fun Application.configureSecurity(jwtService: JwtService) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtService.verifier())
            realm = jwtService.realm

            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val email = credential.payload.getClaim("email").asString()
                if (!userId.isNullOrBlank() && !email.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        status = HttpStatusCode.Unauthorized.value,
                        message = "Invalid or expired token",
                        timestamp = Instant.now().toString()
                    )
                )
            }
        }
    }
}
