package com.issuetalk.auth.service

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordService {
    private val hasher = BCrypt.withDefaults()
    private val verifier = BCrypt.verifyer()

    fun hash(password: String): String =
        hasher.hashToString(12, password.toCharArray())

    fun verify(password: String, hashed: String): Boolean =
        verifier.verify(password.toCharArray(), hashed).verified
}
