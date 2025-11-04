package com.issuetalk.auth.plugins

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Route

fun Route.swaggerDocs() {
    swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
}
