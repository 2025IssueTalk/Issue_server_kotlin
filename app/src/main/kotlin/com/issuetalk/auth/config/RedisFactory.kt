package com.issuetalk.auth.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection

data class RedisResource(
    val client: RedisClient,
    val connection: StatefulRedisConnection<String, String>
) {
    fun close() {
        connection.close()
        client.shutdown()
    }
}

object RedisFactory {
    fun create(config: RedisConfig): RedisResource {
        val uriBuilder = RedisURI.Builder.redis(config.host, config.port)
            .withDatabase(config.database)

        config.password?.takeIf { it.isNotBlank() }?.let {
            uriBuilder.withPassword(it.toCharArray())
        }

        val client = RedisClient.create(uriBuilder.build())
        val connection = client.connect()

        return RedisResource(client, connection)
    }
}
