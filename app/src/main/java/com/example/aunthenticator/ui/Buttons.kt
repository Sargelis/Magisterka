package com.example.aunthenticator.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.aunthenticator.CodeRequest
import com.example.aunthenticator.RetrofitClient
import com.example.aunthenticator.SettingsDataStore
import com.example.aunthenticator.buttonColor
import com.example.aunthenticator.connected2FA
import com.example.aunthenticator.user
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsButton(navController: NavHostController) {
    FloatingActionButton(
        onClick =  { navController.navigate("Settings") },
        containerColor = Color(buttonColor),
        contentColor = Color.White
    ) {
        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
    }
}

@Composable
fun AddButton(navController: NavHostController) {
    FloatingActionButton(
        onClick =  { navController.navigate("Add") },
        containerColor = Color(buttonColor),
        contentColor = Color.White
    ) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
    }
}

@Composable
fun BackButton(navController: NavHostController) {
    FloatingActionButton(
        onClick =  { navController.popBackStack() },
        containerColor = Color(buttonColor),
        contentColor = Color.White
    ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}

//ustawienie flagi w DataStore na blokowanie aplikacji
@Composable
fun LockAppSwith(settingsDataStore: SettingsDataStore) {
    val scope = rememberCoroutineScope()
    val checked by settingsDataStore.switchState.collectAsState(initial = false)

    Switch(
        checked = checked,
        onCheckedChange = {
            scope.launch {
                settingsDataStore.saveSwitchState(it)
            }
        }
    )
}

//dodanie użytkownika na aplikacji poprzez Kod
@Composable
fun CodeAddButton(navController: NavHostController) {
    OutlinedButton(
        onClick = { navController.navigate("CodeAdd") },
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
            .height(60.dp),
        colors = ButtonColors(
            containerColor = Color(0xFFDBDBDB),
            contentColor = Color.Black,
            disabledContainerColor = Color.Green,
            disabledContentColor = Color.White),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(
            text = "Dodaj poprzez kod",
            fontSize = 20.sp,
        )
    }
}

//dodanie użytkownika na aplikacji poprzez kod QR
@Composable
fun QRAddButton(navController: NavHostController) {
    OutlinedButton(
        onClick = { navController.navigate("QRAdd") },
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
            .height(60.dp),
        colors = ButtonColors(
            containerColor = Color(0xFFDBDBDB),
            contentColor = Color.Black,
            disabledContainerColor = Color.Green,
            disabledContentColor = Color.White),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(
            text = "Dodaj poprzez kod QR",
            fontSize = 20.sp,
        )
    }
}

//przycisk potwierdzający dodanie użytkownika poprzez kod
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AddCodeButton(userInput: String, navController: NavHostController) {
    var isUserData by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = {
            Log.d("CODE", "USER: " + userInput)
            Log.d("CODE", "BACKEND: " + user.connectionCode.toString())
            if (user.connectionCode == userInput) {
                isUserData = true
            }
        },
        modifier = Modifier.padding(10.dp)
            .height(60.dp),
        colors = ButtonColors(
            containerColor = Color(0xFFDBDBDB),
            contentColor = Color.Black,
            disabledContainerColor = Color.Green,
            disabledContentColor = Color.White
        ),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(
            text = "Dodaj konto",
            fontSize = 20.sp,
        )
    }

    //jak poprawnie to ustaw na serwerze że użytkwonik ma apliakcję 2FA oraz pobierz 6-cyfrowy kod
    if (isUserData) {
        isUserData = false
        navController.navigate("Main") { popUpTo("Main") { inclusive = true } }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.instance.set2FAApp(CodeRequest("true"))
                user.code = RetrofitClient.instance.getNumberCode().code
                connected2FA = true
            } catch (e: Exception) {
                Log.e("API", "Błąd: ${e.message}")
            }
        }
    }
}