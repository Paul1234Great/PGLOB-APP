package com.example.util

import java.security.MessageDigest
import java.util.UUID
import kotlin.math.abs

enum class PasswordStrength(val label: String) {
    TOO_SHORT("Min 8 characters (letters + numbers)"),
    WEAK("Weak"),
    GOOD("Good"),
    STRONG("Strong")
}

object SecurityAndNumberCodec {

    fun digitsOnly(raw: String): String = raw.filter { it.isDigit() }

    fun normalizePhoneWithDial(dialCode: String, rawLocal: String): String {
        val cleanDial = if (dialCode.startsWith("+")) dialCode.trim() else "+${dialCode.trim()}"
        val localDigits = digitsOnly(rawLocal).trimStart('0')
        return if (localDigits.isBlank()) "" else "$cleanDial $localDigits"
    }

    fun expectedDigitsRange(dialCode: String): IntRange = when (dialCode.trim()) {
        "+234" -> 10..11
        "+233" -> 9..10
        "+44" -> 10..11
        "+1" -> 10..10
        "+91" -> 10..10
        "+254" -> 9..10
        "+27" -> 9..10
        else -> 7..13
    }

    fun isValidPhoneForCountry(dialCode: String, rawLocal: String): Boolean {
        val normalizedDigits = digitsOnly(rawLocal).trimStart('0')
        return normalizedDigits.length in expectedDigitsRange(dialCode)
    }

    fun validatePassword(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.TOO_SHORT
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) return PasswordStrength.WEAK
        val hasSymbolOrUpper = password.any { !it.isLetterOrDigit() } || password.any { it.isUpperCase() }
        return if (password.length >= 10 && hasSymbolOrUpper) PasswordStrength.STRONG else PasswordStrength.GOOD
    }

    fun isPasswordAcceptable(password: String): Boolean {
        return password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }
    }

    fun sha256Hash(salt: String, password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$salt:$password:pglob_v15".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a unique 10-digit PGLOB Account Number guaranteed DIFFERENT from chattingPhone.
     */
    fun generateDistinctAccountNumber(chattingPhone: String, seed: String = UUID.randomUUID().toString()): String {
        val chatDigits = digitsOnly(chattingPhone)
        val chatTail10 = chatDigits.takeLast(10)
        var attempt = 0
        while (attempt < 25) {
            val hashInput = "$seed:$chatDigits:$attempt"
            val digest = MessageDigest.getInstance("SHA-256").digest(hashInput.toByteArray(Charsets.UTF_8))
            val numLong = abs(
                ((digest[0].toLong() and 0xFF) shl 32) or
                    ((digest[1].toLong() and 0xFF) shl 24) or
                    ((digest[2].toLong() and 0xFF) shl 16) or
                    ((digest[3].toLong() and 0xFF) shl 8) or
                    (digest[4].toLong() and 0xFF)
            )
            // 10 digits starting with 90 / 91 / 80 / 70 style prefix but distinct from phone
            val candidate = (1_000_000_000L + (numLong % 8_999_999_999L)).toString()
            if (candidate != chatTail10 && candidate != chatDigits && !chatDigits.endsWith(candidate)) {
                return candidate
            }
            attempt++
        }
        return "909${(1_000_000..9_999_999).random()}"
    }

    fun maskAccountNumber(accountNumber: String): String {
        val d = digitsOnly(accountNumber)
        if (d.length < 7) return "****"
        return "${d.take(4)}***${d.takeLast(3)}"
    }

    fun looksLikeChattingPhoneInsteadOfAccount(input: String, myChattingPhone: String, otherKnownPhones: List<String> = emptyList()): Boolean {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return false
        if (trimmed.startsWith("+")) return true
        val digits = digitsOnly(trimmed)
        val myDigits = digitsOnly(myChattingPhone)
        if (myDigits.isNotBlank() && (digits == myDigits || digits == myDigits.takeLast(10) || digits == "0${myDigits.takeLast(10)}")) {
            return true
        }
        return otherKnownPhones.any { kp ->
            val kd = digitsOnly(kp)
            kd.isNotBlank() && (digits == kd || digits == kd.takeLast(10))
        }
    }
}
