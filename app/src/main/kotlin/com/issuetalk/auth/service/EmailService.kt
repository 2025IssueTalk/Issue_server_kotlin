package com.issuetalk.auth.service

import com.issuetalk.auth.config.MailConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

class EmailService(
    private val config: MailConfig,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    fun queueVerificationEmail(email: String, code: String, ttl: Duration) {
        scope.launch(dispatcher) {
            runCatching { sendVerificationEmail(email, code, ttl) }
                .onFailure { logger.warn("Failed to send verification email to {}", email, it) }
        }
    }

    private fun sendVerificationEmail(email: String, code: String, ttl: Duration) {
        val session = Session.getInstance(buildProperties(), object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(config.username, config.password)
        })

        val expiryMinutes = ttl.toMinutes().coerceAtLeast(1)
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(config.fromAddress))
            setRecipient(Message.RecipientType.TO, InternetAddress(email))
            subject = "IssueTalk verification code"
            setText(
                """
                |Hello from IssueTalk,
                |
                |Your verification code is $code.
                |This code expires in $expiryMinutes minute(s).
                |
                |If you did not request this code, you can safely ignore this email.
                """.trimMargin()
            )
        }

        Transport.send(message)
    }

    private fun buildProperties(): Properties = Properties().apply {
        put("mail.smtp.auth", config.smtpAuth.toString())
        put("mail.smtp.starttls.enable", config.startTls.toString())
        put("mail.smtp.starttls.required", config.startTls.toString())
        put("mail.smtp.host", config.host)
        put("mail.smtp.port", config.port.toString())
        put("mail.smtp.connectiontimeout", config.timeoutMs.toString())
        put("mail.smtp.timeout", config.timeoutMs.toString())
        put("mail.smtp.writetimeout", config.timeoutMs.toString())
    }
}
