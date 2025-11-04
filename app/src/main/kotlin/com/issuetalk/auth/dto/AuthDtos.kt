@file:UseSerializers(UUIDSerializer::class)

package com.issuetalk.auth.dto

import com.issuetalk.auth.util.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class SendEmailRequest(
    val email: String
)

@Serializable
data class VerifyEmailRequest(
    val email: String,
    val code: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

@Serializable
data class UserResponse(
    val id: UUID,
    val email: String,
    val createdAt: String
)

@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: String
)

@Serializable
data class ApiMessage(
    val message: String
)
