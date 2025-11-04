package com.issuetalk.auth.routing

import com.issuetalk.auth.dto.ApiMessage
import com.issuetalk.auth.dto.LoginRequest
import com.issuetalk.auth.dto.RegisterRequest
import com.issuetalk.auth.dto.SendEmailRequest
import com.issuetalk.auth.dto.UserResponse
import com.issuetalk.auth.dto.VerifyEmailRequest
import com.issuetalk.auth.exception.UnauthorizedException
import com.issuetalk.auth.service.AuthService
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.authRoutes(
    authService: AuthService
) {
    route("/auth") {
        post("/email") {
            val request = call.receive<SendEmailRequest>()
            authService.sendVerification(request)
            call.respond(ApiMessage("Verification code sent"))
        }

        post("/verify") {
            val request = call.receive<VerifyEmailRequest>()
            authService.verifyEmail(request)
            call.respond(ApiMessage("Email verified"))
        }

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val user = authService.register(request)
            call.respond(user.toResponse())
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            call.respond(authService.login(request))
        }
    }

    authenticate("auth-jwt") {
        get("/users/me") {
            val principal = call.principal<JWTPrincipal>()
                ?: throw UnauthorizedException("Missing authentication principal")

            val userId = principal.uuidClaim("userId")
                ?: throw UnauthorizedException("Invalid token payload")

            val user = authService.getUserProfile(userId)
                ?: throw UnauthorizedException("User not found")

            call.respond(user.toResponse())
        }
    }
}

private fun com.issuetalk.auth.model.User.toResponse(): UserResponse =
    UserResponse(
        id = this.id,
        email = this.email,
        createdAt = this.createdAt.toString()
    )

private fun JWTPrincipal.uuidClaim(name: String): UUID? =
    payload.getClaim(name)
        ?.asString()
        ?.let run@{
            return@run runCatching { UUID.fromString(it) }.getOrNull()
        }
