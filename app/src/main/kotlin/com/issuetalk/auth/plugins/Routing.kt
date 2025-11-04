package com.issuetalk.auth.plugins

import com.issuetalk.auth.plugins.swaggerDocs
import com.issuetalk.auth.routing.authRoutes
import com.issuetalk.auth.service.AuthService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.Routing

fun Application.configureRouting(
    authService: AuthService
) {
    install(Routing) {
        authRoutes(authService)
        swaggerDocs()
    }
}
