package com.example.aunthenticator

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.example.aunthenticator.ui.theme.AunthenticatorTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val barsColor = 0xFF0C00E8
const val buttonColor = 0xFF0B02AD
var user = User()
var connected2FA = false

//ustawienie retrofit do komunikajci z serwerem
object RetrofitClient {
    //sprawdzić jaki adres ipconfig + :port serwera
    //private const val BASE_URL = "http://10.0.2.2:4000"
    private const val BASE_URL = "http://192.168.100.23:4000"

    val instance: API by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(API::class.java)
    }
}
//stworzenie kanału powiadomień ważne dla odbioru powiadomień
private fun createNotificationChannel(context: Context) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "login_channel",
            "Powiadomienia",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

class MainActivity : FragmentActivity() {
    private lateinit var settingsDataStore: SettingsDataStore

    //pozwolenie na użycie notyfikacji
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            return@registerForActivityResult
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsDataStore = SettingsDataStore(applicationContext)
        enableEdgeToEdge()
        createNotificationChannel(this)

        //pozwolenie na użycie notyfikacji
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        lifecycleScope.launch {

            //pobranie tokenu urządzenia oraz wysłanie go do serwera
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }
                    val token = task.result
                    Log.d("FCM", "Token: $token")

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            RetrofitClient.instance.setFCMToken(FCMTokenRequest(token))
                        } catch (e: Exception) {
                            Log.e("FCM", "Błąd: ${e.message}")
                        }
                    }
                }

            //TODO odczyt użytkownika z bazy danych czy ma 2FA?

            runOnUiThread {
                setContent {
                    val unlockApp = settingsDataStore.switchState.collectAsState(initial = false)
                    val biometricAuth = settingsDataStore.shouldShowBiometricFlow.collectAsState(initial = false)
                    var isAuthenticated by remember { mutableStateOf(false) }
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                    Log.d("TEST_bool", "unlockApp: ${unlockApp.value}")
                    Log.d("TEST_bool", "biometricAuth: ${biometricAuth.value}")
                    Log.d("TEST_bool", "isAuthenticated: $isAuthenticated")

                    //ustawienie obserwatora czy aplikacja zminimalizowana do ponownej autoryzacji jak aplikacja zablokowana
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) { isAuthenticated = false }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    AunthenticatorTheme {
                        //warunki do wyświetlenia ekranów
                        when {
                            //ekran do oblokowania aplikacji
                            unlockApp.value && !isAuthenticated -> BiometricScreen(
                                onSuccess = { isAuthenticated = true },
                                onError = { finish() }
                            )
                            //ekran do potwierdzenia prośby o logowanie na stronie
                            biometricAuth.value  -> BiometricAuthScreen(
                                onSuccess = { lifecycleScope.launch { settingsDataStore.setShouldShowBiometric(false) } },
                                onError = { finish() }
                            )
                            //normalne działanie
                            else -> Nav(settingsDataStore)
                        }
                    }
                }
            }
        }
    }
    //funkcja która odpowiada za kliknięcie w powiadomienie/ wyświetlenie powiadomienia
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.extras?.getString("screen")?.let { screen ->
            if (screen == "BiometricLogin") { //nazwa z bacend -> /sendLoginRequest ->const message -> data.screen
                lifecycleScope.launch {
                    settingsDataStore.setShouldShowBiometric(true)
                }
            }
        }
    }
}