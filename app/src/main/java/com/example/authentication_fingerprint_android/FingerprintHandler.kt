package com.example.authentication_fingerprint_android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class FingerprintHandler // Constructor
    (private val context: Context) : FingerprintManager.AuthenticationCallback() {
    // Fingerprint authentication starts here..
    fun Authentication(
        manager: FingerprintManager,
        cryptoObject: FingerprintManager.CryptoObject?
    ) {
        val cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_FINGERPRINT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    // On authentication failed
    override fun onAuthenticationFailed() {
        update("Authentication Failed!!!", false)
    }

    // On successful authentication
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        update("Successfully Authenticated...", true)
    }

    // This method is used to update the text message
    // depending on the authentication result
    fun update(e: String?, success: Boolean) {
        val textView = (context as Activity).findViewById<View>(R.id.textMsg) as TextView
        textView.text = e
        if (success) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }
}

