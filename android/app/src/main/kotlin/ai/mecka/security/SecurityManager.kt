package ai.mecka.security

import ai.mecka.runtime.StateManager
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class SecurityManager(private val context: Context) {

    private val stateManager: StateManager by lazy { StateManager.getInstance(context) }
    private val keyStore: KeyStore by lazy { KeyStore.getInstance("AndroidKeyStore").apply { load(null) } }

    // Encrypted SharedPreferences for secure storage
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedSharedPreferences: EncryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "mecka_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun initialize(): Boolean {
        Timber.d("SecurityManager initializing")
        return true
    }

    fun generateEncryptionKey(alias: String): Boolean {
        return try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate encryption key")
            false
        }
    }

    fun encryptData(alias: String, data: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = keyStore.getKey(alias, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data)
            // Combine IV and encrypted data
            ByteArray(iv.size + encryptedData.size).apply {
                System.arraycopy(iv, 0, this, 0, iv.size)
                System.arraycopy(encryptedData, 0, this, iv.size, encryptedData.size)
            }
        } catch (e: Exception) {
            Timber.e(e, "Encryption failed")
            null
        }
    }

    fun decryptData(alias: String, encryptedData: ByteArray): ByteArray? {
        return try {
            val ivSize = 12 // GCM IV size
            val iv = encryptedData.copyOfRange(0, ivSize)
            val actualEncryptedData = encryptedData.copyOfRange(ivSize, encryptedData.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = keyStore.getKey(alias, null) as SecretKey
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            cipher.doFinal(actualEncryptedData)
        } catch (e: Exception) {
            Timber.e(e, "Decryption failed")
            null
        }
    }

    fun storeSecureValue(key: String, value: String): Boolean {
        return try {
            encryptedSharedPreferences.edit().putString(key, value).apply()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to store secure value")
            false
        }
    }

    fun retrieveSecureValue(key: String): String? {
        return encryptedSharedPreferences.getString(key, null)
    }

    fun removeSecureValue(key: String): Boolean {
        return try {
            encryptedSharedPreferences.edit().remove(key).apply()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove secure value")
            false
        }
    }

    fun validateEarbudAuthentication(macAddress: String): Boolean {
        // TODO: Implement earbud MAC address validation logic
        // This should check against whitelisted MAC addresses
        return true
    }

    fun deriveKeyFromEarbudMac(macAddress: String): String {
        // TODO: Implement key derivation from earbud MAC address
        return "derived_key_$macAddress"
    }

    companion object {
        private var instance: SecurityManager? = null

        fun getInstance(context: Context): SecurityManager {
            return instance ?: synchronized(this) {
                instance ?: SecurityManager(context).also {
                    instance = it
                }
            }
        }
    }
}