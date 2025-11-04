package com.issuetalk.auth.exception

import io.ktor.http.HttpStatusCode

open class ApiException(
    val status: HttpStatusCode,
    override val message: String
) : RuntimeException(message)

class ConflictException(message: String) : ApiException(HttpStatusCode.Conflict, message)
class UnauthorizedException(message: String) : ApiException(HttpStatusCode.Unauthorized, message)
class BadRequestException(message: String) : ApiException(HttpStatusCode.BadRequest, message)
