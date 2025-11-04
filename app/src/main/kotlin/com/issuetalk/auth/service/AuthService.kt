package com.issuetalk.auth.service

import com.issuetalk.auth.dto.LoginRequest
import com.issuetalk.auth.dto.RegisterRequest
import com.issuetalk.auth.dto.SendEmailRequest
import com.issuetalk.auth.dto.TokenResponse
import com.issuetalk.auth.dto.VerifyEmailRequest
import com.issuetalk.auth.exception.BadRequestException
import com.issuetalk.auth.exception.ConflictException
import com.issuetalk.auth.exception.UnauthorizedException
import com.issuetalk.auth.model.User
import com.issuetalk.auth.repository.UserRepository
import com.issuetalk.auth.util.isValidEmail
import com.issuetalk.auth.util.normalizedEmail
import java.security.SecureRandom
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val redisService: RedisService,
    private val emailService: EmailService,
    private val passwordService: PasswordService,
    private val jwtService: JwtService
) {

    suspend fun sendVerification(request: SendEmailRequest) {
        val email = request.email.requireValidEmail()
        val code = generateVerificationCode()
        redisService.storeVerificationCode(email, code)
        emailService.queueVerificationEmail(email, code, redisService.verificationTtl)
    }

    suspend fun verifyEmail(request: VerifyEmailRequest) {
        val email = request.email.requireValidEmail()
        val submittedCode = request.code.trim().takeIf { it.isNotEmpty() }
            ?: throw BadRequestException("Verification code is required")
        val storedCode = redisService.fetchVerificationCode(email)
            ?: throw BadRequestException("Verification code expired or not found")

        if (storedCode != submittedCode) {
            throw BadRequestException("Verification code does not match")
        }

        redisService.markEmailVerified(email)
    }

    suspend fun register(request: RegisterRequest): User {
        val email = request.email.requireValidEmail()

        if (!redisService.isEmailVerified(email)) {
            throw BadRequestException("Email verification required before registration")
        }

        userRepository.findByEmail(email)?.let {
            throw ConflictException("Email already exists")
        }

        val hashedPassword = passwordService.hash(request.password.requireValidPassword())
        val user = userRepository.create(email, hashedPassword)
        redisService.clearVerification(email)
        return user
    }

    suspend fun login(request: LoginRequest): TokenResponse {
        val email = request.email.requireValidEmail()
        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException("Invalid credentials")

        val candidatePassword = request.password.ifBlank {
            throw UnauthorizedException("Invalid credentials")
        }
        if (!passwordService.verify(candidatePassword, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }

        val token = jwtService.generateToken(user)
        return TokenResponse(
            accessToken = token,
            expiresIn = jwtService.accessTokenValiditySeconds
        )
    }

    suspend fun getUserProfile(userId: UUID): User? =
        userRepository.findById(userId)

    private fun generateVerificationCode(): String =
        CODE_RANDOM.nextInt(VERIFICATION_CODE_BOUND).toString().padStart(VERIFICATION_CODE_LENGTH, '0')

    private fun String.requireValidEmail(): String =
        normalizedEmail().takeIf { it.isValidEmail() }
            ?: throw BadRequestException("Invalid email address format")

    private fun String.requireValidPassword(): String =
        takeIf { it.length >= MIN_PASSWORD_LENGTH }
            ?: throw BadRequestException("Password must be at least $MIN_PASSWORD_LENGTH characters long")

    private companion object {
        private const val VERIFICATION_CODE_LENGTH = 6
        private const val VERIFICATION_CODE_BOUND = 1_000_000
        private const val MIN_PASSWORD_LENGTH = 8
        private val CODE_RANDOM = SecureRandom()
    }
}
