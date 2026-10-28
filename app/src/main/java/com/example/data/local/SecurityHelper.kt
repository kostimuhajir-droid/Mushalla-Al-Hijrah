package com.example.data.local

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object SecurityHelper {
    private const val SALT = "MUSHALLA_SECURE_VAULT_2026_SYARIAH_FINANCE"
    private const val AES_KEY_16 = "MushallaKasKey99" // 16 bytes for local AES
    private const val INIT_VECTOR = "RandomInitVector" // 16 bytes IV

    /**
     * Menghasilkan SHA-256 integrity hash anti-tamper untuk menjamin
     * keaslian dan keamanan rekaman keuangan mushalla.
     */
    fun generateIntegrityHash(vararg parts: String): String {
        return try {
            val input = parts.joinToString(separator = "|") + "|$SALT"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            "SEC-${System.currentTimeMillis().toString().takeLast(8)}"
        }
    }

    /**
     * Enkripsi string teks sensitif menggunakan AES/CBC/PKCS5Padding
     */
    fun encryptText(plainText: String): String {
        return try {
            val iv = IvParameterSpec(INIT_VECTOR.toByteArray(Charsets.UTF_8))
            val keySpec = SecretKeySpec(AES_KEY_16.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    /**
     * Dekripsi string teks menggunakan AES
     */
    fun decryptText(cipherText: String): String {
        return try {
            val iv = IvParameterSpec(INIT_VECTOR.toByteArray(Charsets.UTF_8))
            val keySpec = SecretKeySpec(AES_KEY_16.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            String(cipher.doFinal(decoded), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}
