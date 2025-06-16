package com.example.aunthenticator.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.aunthenticator.AuthViewModel
import com.example.aunthenticator.RetrofitClient
import com.example.aunthenticator.ShowAccount
import com.example.aunthenticator.User
import com.example.aunthenticator.barsColor
import com.example.aunthenticator.connected2FA
import com.example.aunthenticator.ui.AddButton
import com.example.aunthenticator.ui.SettingsButton
import com.example.aunthenticator.user

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController)
{
    val userState = remember { mutableStateOf(User()) }
    val viewModel: AuthViewModel = viewModel()

    //pobranie danych z serwera i ustawienie ich w data class User
    LaunchedEffect(Unit) {
        try {
            val email = RetrofitClient.instance.getEmail().email
            val qrConnCode = RetrofitClient.instance.getQRConnCode().connectionQRCode
            val connCode = RetrofitClient.instance.getConnCode().connectionCode

            user = User(email, qrConnCode, connCode)
            userState.value = user

            Log.d("API", "User: $user")
        } catch (e: Exception) {
            Log.e("API", "Błąd: ${e.message}")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(barsColor)),
                title = {
                    Text(
                        text = "Authenticator",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                    )
                },
                scrollBehavior = pinnedScrollBehavior()
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(barsColor)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SettingsButton(navController)
                    AddButton(navController)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)) {
            if(connected2FA) {
/*                Log.d("API_SHOW", "Email: ${user.email}")
                Log.d("API_SHOW", "ConnectionQRCode: ${user.connectionQRCode}")
                Log.d("API_SHOW", "ConnectionCode: ${user.connectionCode}")
                Log.d("API_SHOW", "NumberCode: ${user.code}")
                Log.d("API_SHOW", "QRCode: ${user.qrCode}")*/
                ShowAccount(navController, viewModel)
            }
        }
    }
}