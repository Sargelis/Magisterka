package com.example.aunthenticator.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.aunthenticator.CameraPreviewView
import com.example.aunthenticator.CodeRequest
import com.example.aunthenticator.RequestCameraPermission
import com.example.aunthenticator.RetrofitClient
import com.example.aunthenticator.barsColor
import com.example.aunthenticator.ui.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRVerifi(navController: NavHostController) {
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }
    val scannedQRCode = remember { mutableStateOf("") }
    val navigated = remember { mutableStateOf(false) }
    val backendQR = remember { mutableStateOf("") }

    //pobranie kodu z serwera odpowiedzialnego za weryfikację logowania
    LaunchedEffect(Unit) {
        try {
            backendQR.value = RetrofitClient.instance.getQRCode().qrCode
        } catch (e: Exception) {
            Log.e("API", "Błąd: ${e.message}")
        }
    }
    //weryfikacja kody skanowanego i z serwera oraz ustawienie pozytywnej weryfikacji użytkownika
    LaunchedEffect(scannedQRCode.value, backendQR.value) {
        if(!navigated.value && scannedQRCode.value == backendQR.value && backendQR.value.isNotEmpty()) {
            Log.d("API_TEST", "SUCESS")
                try {
                    RetrofitClient.instance.setQRVerified(CodeRequest("true"))
                    navigated.value = true
                    navController.navigate("Main") { popUpTo("Main") {inclusive = true} }
                } catch (e: Exception) {
                    Log.e("API", "Błąd: ${e.message}")
                }
            }
        }
    //pozwolenie na uzycie kamery
    RequestCameraPermission {
        permissionGranted = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(barsColor)),
                title = {
                    Text(
                        text = "Zeskanuj kod QR",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BackButton(navController)
                }
            }
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            if(permissionGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    CameraPreviewView { result ->
                        scannedCode = result
                        scannedQRCode.value = result.trim('"')
                    }
                    Log.d("API_TEST", "scanned QR Code: ${scannedQRCode.value}")
                    Log.d("API_TEST", "backend Code: ${backendQR.value}")
                }
            }
        }
    }
}