package com.upriseus.remindme.features

import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object Hash {
    fun hash(password: String): String{
        val random = SecureRandom()
        val iterations = 65536
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, 128)
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return "$iterations:" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash)
    }

    fun validatePassword(originalPassword: String, storedPassword: String): Boolean {
        val parts = storedPassword.split(":").toTypedArray()
        val iterations = parts[0].toInt()
        val salt: ByteArray = Base64.getDecoder().decode(parts[1])
        val hash: ByteArray = Base64.getDecoder().decode(parts[2])
        val spec = PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.size * 8)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val testHash = skf.generateSecret(spec).encoded
        return hash.contentEquals(testHash)
    }
}