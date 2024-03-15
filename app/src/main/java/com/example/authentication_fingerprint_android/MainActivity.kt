package com.example.authentication_fingerprint_android
import android.Manifest

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


class MainActivity : AppCompatActivity() {
    private var keyStore: KeyStore? = null
    private var cipher: Cipher? = null
    private var errorText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initializing KeyguardManager and FingerprintManager
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val fingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager

        // Initializing our error text
        errorText = findViewById<View>(R.id.textMsg) as TextView

        // Here, we are using various security checks
        // Checking device is inbuilt with fingerprint sensor or not
        if (!fingerprintManager.isHardwareDetected) {

            // Setting error message if device
            // doesn't have fingerprint sensor
            errorText!!.text = "Device does not support fingerprint sensor"
        } else {
            // Checking fingerprint permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FINGERPRINT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                errorText!!.text = "Fingerprint authentication is not enabled"
            } else {
                // Check for at least one registered finger
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    errorText!!.text = "Register at least one finger"
                } else {
                    // Checking for screen lock security
                    if (!keyguardManager.isKeyguardSecure) {
                        errorText!!.text = "Screen lock security not enabled"
                    } else {

                        // if everything is enabled and correct then we will generate
                        // the encryption key which will be stored on the device
                        generateKey()
                        if (cipherInit()) {
                            val cryptoObject = FingerprintManager.CryptoObject(
                                cipher!!
                            )
                            val helper = FingerprintHandler(this)
                            helper.Authentication(fingerprintManager, cryptoObject)
                        }
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val keyGenerator: KeyGenerator
        keyGenerator = try {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("KeyGenerator instance failed", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("KeyGenerator instance failed", e)
        }
        try {
            keyStore!!.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        cipher = try {
            Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Cipher failed", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Cipher failed", e)
        }
        return try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey(
                KEY_NAME,
                null
            ) as SecretKey
            cipher?.init(Cipher.ENCRYPT_MODE, key)
            true
        } catch (e: KeyPermanentlyInvalidatedException) {
            false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Cipher initialization failed", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Cipher initialization failed", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Cipher initialization failed", e)
        } catch (e: IOException) {
            throw RuntimeException("Cipher initialization failed", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Cipher initialization failed", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Cipher initialization failed", e)
        }
    }

    companion object {
        // Defining variable for storing
        // key in android keystore container
        private const val KEY_NAME = "GEEKSFORGEEKS"
    }
}

