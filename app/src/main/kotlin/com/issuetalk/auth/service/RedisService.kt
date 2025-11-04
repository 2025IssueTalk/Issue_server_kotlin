package com.issuetalk.auth.service

import com.issuetalk.auth.config.RedisConfig
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RedisService(
    connection: StatefulRedisConnection<String, String>,
    private val config: RedisConfig,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val verificationTtl = config.verificationTtl

    private val commands = connection.sync()

    suspend fun storeVerificationCode(email: String, code: String) = withIoContext {
        commands.setex(
            verificationKey(email),
            verificationTtl.seconds,
            code
        )
    }

    suspend fun fetchVerificationCode(email: String): String? = withIoContext {
        commands.get(verificationKey(email))
    }

    suspend fun markEmailVerified(email: String) = withIoContext {
        commands.setex(verifiedKey(email), verificationTtl.seconds, VERIFIED_VALUE)
    }

    suspend fun isEmailVerified(email: String): Boolean = withIoContext {
        commands.get(verifiedKey(email)) == VERIFIED_VALUE
    }

    suspend fun clearVerification(email: String) = withIoContext {
        commands.del(verificationKey(email))
        commands.del(verifiedKey(email))
    }

    private fun verificationKey(email: String) = "verification:code:$email"
    private fun verifiedKey(email: String) = "verification:verified:$email"

    private suspend fun <T> withIoContext(block: suspend () -> T): T =
        withContext(dispatcher) { block() }

    private companion object {
        private const val VERIFIED_VALUE = "true"
    }
}
