package com.issuetalk.auth

import com.issuetalk.auth.config.AppConfigLoader
import com.issuetalk.auth.config.DatabaseFactory
import com.issuetalk.auth.config.RedisFactory
import com.issuetalk.auth.config.RedisResource
import com.issuetalk.auth.plugins.configureExceptionHandling
import com.issuetalk.auth.plugins.configureMonitoring
import com.issuetalk.auth.plugins.configureRouting
import com.issuetalk.auth.plugins.configureSecurity
import com.issuetalk.auth.plugins.configureSerialization
import com.issuetalk.auth.repository.UserRepository
import com.issuetalk.auth.service.AuthService
import com.issuetalk.auth.service.EmailService
import com.issuetalk.auth.service.JwtService
import com.issuetalk.auth.service.PasswordService
import com.issuetalk.auth.service.RedisService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val config = AppConfigLoader.load()

    val dataSource = DatabaseFactory.init(config.database)

    val redisResource: RedisResource = RedisFactory.create(config.redis)
    val redisService = RedisService(redisResource.connection, config.redis)
    val emailService = EmailService(config.mail, appScope)
    val passwordService = PasswordService()
    val userRepository = UserRepository()
    val jwtService = JwtService(config.jwt)
    val authService = AuthService(
        userRepository = userRepository,
        redisService = redisService,
        emailService = emailService,
        passwordService = passwordService,
        jwtService = jwtService
    )

    configureMonitoring()
    configureSerialization()
    configureSecurity(jwtService)
    configureExceptionHandling()
    configureRouting(authService)

    environment.monitor.subscribe(ApplicationStopping) {
        redisResource.close()
        dataSource.close()
        appScope.cancel()
    }
}
