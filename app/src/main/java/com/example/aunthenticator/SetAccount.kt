package com.example.aunthenticator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

//klasa przechowująca dane użytkwonika z serwera
data class User(
    @SerializedName("email")
    val email: String = "",
    @SerializedName("connectionQRCode")
    val connectionQRCode: String = "",
    @SerializedName("connectionCode")
    var connectionCode: String? = "XD",
    @SerializedName("code")
    var code: String = "",
    @SerializedName("qrCode")
    var qrCode: String? = "XD"
)

data class EmailResponse(val email: String)
data class ConnectionQRCodeResponse(val connectionQRCode: String)
data class ConnectionCodeResponse(val connectionCode: String)
data class CodeResponse(val code: String)
data class QRCodeResponse(val qrCode: String)

data class CodeRequest(val code: String)
data class FCMTokenRequest(val token: String)
data class BiometricVerifiedRequest(val biometricVerification: Boolean)

//API - metody do komunikacji z serwerem aby pobierać dane jak i je wysyłać
interface API {
    @GET("getEmail")
    suspend fun getEmail(): EmailResponse
    @GET("getQRConnCode")
    suspend fun getQRConnCode(): ConnectionQRCodeResponse
    @GET("getConnCode")
    suspend fun getConnCode(): ConnectionCodeResponse
    @GET("getNumberCode")
    suspend fun getNumberCode(): CodeResponse
    @GET("getQRCode")
    suspend fun getQRCode(): QRCodeResponse
    @POST("set2FAApp")
    suspend fun set2FAApp(@Body request: CodeRequest)
    @POST("setQRVerified")
    suspend fun setQRVerified(@Body request: CodeRequest)
    @POST("setFCMToken")
    suspend fun setFCMToken(@Body request: FCMTokenRequest)
    @POST("setBiometricVerified")
    suspend fun setBiometricVerified(@Body request: BiometricVerifiedRequest)
}

//klasa odpowiedzialna za pobieranie kodu co dwie minuty i wyświetlanie go
class AuthViewModel : ViewModel() {
    var codeTimer by mutableIntStateOf(120)
    private set
    var code by mutableStateOf(user.code)
    private set

    init { startTimer() }

    private fun updateCode(newCode: String) {
        code = newCode
        codeTimer = 120
        user = user.copy(code = newCode)
    }

    private fun startTimer() {
        viewModelScope.launch {
            while(true) {
                try {
                    val newCode = RetrofitClient.instance.getNumberCode().code
                    updateCode(newCode)
                } catch (e: Exception) {
                    Log.e("API", "Błąd: ${e.message}")
                }

                codeTimer = 120
                while (codeTimer > 0) {
                    delay(1000)
                    codeTimer--
                }
            }
        }
    }
}

//funkcja odpowiedzialna za wyświetlanie emailu kodu oraz pozostałego czasu ważności kodu
@Composable
fun ShowAccount(navController: NavController, viewModel: AuthViewModel) {
    val timer = viewModel.codeTimer
    val code = viewModel.code

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .background(Color(0xFFDBDBDB))
            .clickable { navController.navigate("QRVerifi") }
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(Color(0xFFCCCCCC)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Email:",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(5.dp)
                    .padding(horizontal = 5.dp)
            )
            Text(
                text = "Kod:",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(5.dp)
                    .padding(horizontal = 5.dp)
            )
            Text(
                text = "Czas:",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(5.dp)
                    .padding(horizontal = 5.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(Color(0xFFCCCCCC)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //showing user email
            Text(
                text = user.email,
                fontSize = 17.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(7.dp)
                    .padding(horizontal = 5.dp)
            )
            // code show
            Text(
                text = code,
                color = Color.Black,
                fontSize = 17.sp
            )
            //timer
            Text(
                text = "${timer}s",
                fontSize = 17.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(7.dp)
                    .padding(horizontal = 5.dp),
            )
        }
    }
}