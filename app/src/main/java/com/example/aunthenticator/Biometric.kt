package com.example.aunthenticator

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

//Biometria odpowiedzialna za odblokwoanie aplikajci
fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)
    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autoryzacja biometryczna")
                .setSubtitle("Uzyj odcisku palca aby odblokować aplikację")
                .setNegativeButtonText("Anuluj")
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Nieprawidłowy odcisk palca")
                    }
                })
            biometricPrompt.authenticate(promptInfo)
        }
        else {
            onError("Autoryzacja niedostępna")
        }
    }
//Biometria odpowiedzialna za autoryzację logowania na stronie
fun biometricAuth(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)
    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autoryzacja biometryczna")
            .setSubtitle("Uzyj odcisku palca aby autoryzować logowanie")
            .setNegativeButtonText("Anuluj")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    //wysłanie potwierdzenia prośby logowania na serwer
                    activity.lifecycleScope.launch {
                        RetrofitClient.instance.setBiometricVerified(BiometricVerifiedRequest(true))
                    }
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Nieprawidłowy odcisk palca")
                }
            })
        biometricPrompt.authenticate(promptInfo)
    }
    else {
        onError("Autoryzacja niedostępna")
    }
}
//ekran blokady aplikacji aby odblokować
@Composable
fun BiometricScreen (
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
  val activity = LocalContext.current as FragmentActivity

  LaunchedEffect(true) {
      showBiometricPrompt(
          activity = activity,
          onSuccess = onSuccess,
          onError = { onError() }
      )
  }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Autoryzacja biometryczna")
    }
}
//ekran autoryzacji logowania poprzez biometrię
@Composable
fun BiometricAuthScreen (
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val activity = LocalContext.current as FragmentActivity

    LaunchedEffect(true) {
        biometricAuth(
            activity = activity,
            onSuccess = onSuccess,
            onError = { onError() }
        )
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Proszę użyć odcisku palca",
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}