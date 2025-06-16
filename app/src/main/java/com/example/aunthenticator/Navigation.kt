package com.example.aunthenticator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aunthenticator.screens.AddScreen
import com.example.aunthenticator.screens.CodeAddScreen
import com.example.aunthenticator.screens.MainScreen
import com.example.aunthenticator.screens.QRAddScreen
import com.example.aunthenticator.screens.QRVerifi
import com.example.aunthenticator.screens.SettingScreen

//navigacja pomiędzy ekranami
@Composable
fun Nav(settingDataStore: SettingsDataStore)
{
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Main")
    {
        composable(route = "Settings") {
            SettingScreen(navController, settingDataStore)
        }
        composable(route = "Main") {
            MainScreen(navController)
        }
        composable(route = "Add") {
            AddScreen(navController)
        }
        composable(route = "QRAdd") {
            QRAddScreen(navController)
        }
        composable(route = "CodeAdd") {
            CodeAddScreen(navController)
        }
        composable(route = "QRVerifi") {
            QRVerifi(navController)
        }
    }
}