package com.ridermesh.app.encryption

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ridermesh.app.data.MeshMessage
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CryptoManager(private val context: android.content.Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun generateRideKey(): RideKeyBundle {
        val random = SecureRandom()
        val keyBytes = ByteArray(32).also(random::nextBytes)
        val joinCode = RideKeyEncoder.toJoinCode(keyBytes)
        val rideId = RideKeyEncoder.rideIdFromKey(keyBytes)
        saveKey(rideId, keyBytes)
        return RideKeyBundle(rideId = rideId, joinCode = joinCode, rawKey = keyBytes)
    }

    fun encrypt(message: MeshMessage, key: ByteArray): EncryptedPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(12).also(SecureRandom()::nextBytes)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key.toSecretKey(), spec)
        cipher.updateAAD(message.senderId.toByteArray())
        val cipherBytes = cipher.doFinal(json.encodeToString(message.payload).toByteArray())
        return EncryptedPayload(iv = iv, ciphertext = cipherBytes)
    }

    fun decrypt(payload: EncryptedPayload, message: MeshMessage, key: ByteArray): Map<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, payload.iv)
        cipher.init(Cipher.DECRYPT_MODE, key.toSecretKey(), spec)
        cipher.updateAAD(message.senderId.toByteArray())
        val plainBytes = cipher.doFinal(payload.ciphertext)
        return json.decodeFromString<Map<String, String>>(plainBytes.toString(Charsets.UTF_8))
    }

    fun getKey(rideId: String): ByteArray? = prefs().getString(rideId, null)?.decode()

    private fun saveKey(rideId: String, key: ByteArray) {
        prefs().edit().putString(rideId, key.encode()).apply()
    }

    private fun prefs(): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "ride_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun ByteArray.encode(): String = android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
    private fun String.decode(): ByteArray = android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
    private fun ByteArray.toSecretKey(): SecretKey = javax.crypto.spec.SecretKeySpec(this, "AES")

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

data class RideKeyBundle(val rideId: String, val joinCode: String, val rawKey: ByteArray)

data class EncryptedPayload(val iv: ByteArray, val ciphertext: ByteArray)

object RideKeyEncoder {
    fun toJoinCode(bytes: ByteArray): String = bytes.take(5).joinToString("") { String.format("%02X", it) }
    fun rideIdFromKey(bytes: ByteArray): String = bytes.takeLast(8).joinToString("") { String.format("%02X", it) }
}
