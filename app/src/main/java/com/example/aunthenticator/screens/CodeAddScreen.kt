package com.example.aunthenticator.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.navigation.NavHostController
import com.example.aunthenticator.RetrofitClient
import com.example.aunthenticator.barsColor
import com.example.aunthenticator.ui.AddCodeButton
import com.example.aunthenticator.ui.BackButton
import com.example.aunthenticator.user

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeAddScreen(navController: NavHostController)
{
    val userInput = remember { mutableStateOf("") }

    //wysłanie prośby o kod połączeniowy do serwera
    LaunchedEffect(Unit) {
        try {
            user.connectionCode = null
            val newConnectionCode = RetrofitClient.instance.getConnCode().connectionCode
            user.connectionCode = newConnectionCode
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
                        text = "Dodaj konto",
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
                    horizontalArrangement = Arrangement.End
                ) {
                    BackButton(navController)
                }
            }
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Wprowadź kod:",
                fontSize = 30.sp
            )
            OutlinedTextField(
                value = userInput.value,
                onValueChange = { userInput.value = it },
                modifier = Modifier.fillMaxWidth()
                    .padding(20.dp)
            )
            AddCodeButton(userInput.value, navController)
        }
    }
}