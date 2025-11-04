package com.issuetalk.auth.util

private val EMAIL_REGEX =
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

fun String.normalizedEmail(): String = trim().lowercase()

fun String.isValidEmail(): Boolean = EMAIL_REGEX.matches(this)
