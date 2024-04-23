package app.app

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.util.Base64

object EncryptionUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val SECRET_KEY = "YourSecretKeyHerYourSecretKeyHer" // 16 characters for AES-128, 24 characters for AES-192, or 32 characters for AES-256
    private const val IV = "YourInitializati" // 16 bytes IV for AES

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = generateSecretKey()
        val ivKey = generateIV()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivKey)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(encryptedData: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secretKey = generateSecretKey()
        val ivKey = generateIV()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivKey)
        val decodedBytes = Base64.getDecoder().decode(encryptedData)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }

    private fun generateSecretKey(): SecretKey {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
        return keySpec
    }

    private fun generateIV(): IvParameterSpec{
        val ivSpec = IvParameterSpec(IV.toByteArray())
        return ivSpec
    }
}