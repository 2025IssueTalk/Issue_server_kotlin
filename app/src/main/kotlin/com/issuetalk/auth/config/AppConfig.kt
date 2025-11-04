package com.issuetalk.auth.config

data class AppConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val jwt: JwtConfig,
    val mail: MailConfig
)

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val driver: String = "org.postgresql.Driver"
)

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String?,
    val database: Int,
    val verificationTtlSeconds: Long = 300
) {
    val verificationTtl = java.time.Duration.ofSeconds(verificationTtlSeconds)
}

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenValidityMs: Long
)

data class MailConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromAddress: String,
    val smtpAuth: Boolean,
    val startTls: Boolean,
    val timeoutMs: Int
)

object AppConfigLoader {
    fun load(): AppConfig {
        val database = DatabaseConfig(
            url = env("DB_URL", "jdbc:postgresql://localhost:5432/issuetalk"),
            user = env("DB_USER", "issuetalk"),
            password = env("DB_PASSWORD", "issuetalk")
        )

        val redis = RedisConfig(
            host = env("REDIS_HOST", "localhost"),
            port = env("REDIS_PORT", "6379").toInt(),
            password = optionalEnv("REDIS_PASSWORD"),
            database = env("REDIS_DATABASE", "0").toInt(),
            verificationTtlSeconds = env("EMAIL_CODE_TTL_SECONDS", "300").toLong()
        )

        val jwt = JwtConfig(
            secret = env("JWT_SECRET", required = true),
            issuer = env("JWT_ISSUER", "issuetalk-auth-service"),
            audience = env("JWT_AUDIENCE", "issuetalk-clients"),
            realm = env("JWT_REALM", "issuetalk-auth-realm"),
            accessTokenValidityMs = env("JWT_ACCESS_TOKEN_VALIDITY_MS", "900000").toLong()
        )

        val mailUsername = env("MAIL_USERNAME", required = true)

        val mail = MailConfig(
            host = env("MAIL_HOST", "smtp.gmail.com"),
            port = env("MAIL_PORT", "587").toInt(),
            username = mailUsername,
            password = env("MAIL_PASSWORD", required = true),
            fromAddress = env("MAIL_FROM", mailUsername),
            smtpAuth = env("MAIL_SMTP_AUTH", "true").toBoolean(),
            startTls = env("MAIL_SMTP_STARTTLS_ENABLE", "true").toBoolean(),
            timeoutMs = env("MAIL_SMTP_TIMEOUT_MS", "5000").toInt()
        )

        return AppConfig(
            database = database,
            redis = redis,
            jwt = jwt,
            mail = mail
        )
    }

    private fun env(name: String, default: String? = null, required: Boolean = false): String {
        val value = System.getenv(name) ?: default
        if (required && value.isNullOrBlank()) {
            error("Missing required environment variable: $name")
        }
        return value ?: ""
    }

    private fun optionalEnv(name: String): String? = System.getenv(name)
}
