package com.issuetalk.auth.repository

import com.issuetalk.auth.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

object UsersTable : UUIDTable("users") {
    val email = varchar("email", length = 255).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 255)
    val createdAt = timestamp("created_at")
}

class UserRepository {
    suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable.select { UsersTable.email eq email }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun findById(id: UUID): User? = dbQuery {
        UsersTable.select { UsersTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    suspend fun create(email: String, passwordHash: String): User = dbQuery {
        val now = Instant.now()

        val insertedId = UsersTable.insertAndGetId {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.createdAt] = now
        }

        User(
            id = insertedId.value,
            email = email,
            passwordHash = passwordHash,
            createdAt = now
        )
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id].value,
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        createdAt = this[UsersTable.createdAt]
    )
}
