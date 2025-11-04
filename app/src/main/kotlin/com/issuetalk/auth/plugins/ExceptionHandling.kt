package com.issuetalk.auth.plugins

import com.issuetalk.auth.dto.ErrorResponse
import com.issuetalk.auth.exception.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import java.time.Instant

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                status = cause.status,
                message = ErrorResponse(
                    status = cause.status.value,
                    message = cause.message,
                    timestamp = Instant.now().toString()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    message = "Internal server error",
                    timestamp = Instant.now().toString()
                )
            )
        }
    }
}
