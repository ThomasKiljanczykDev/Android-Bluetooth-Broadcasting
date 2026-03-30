package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.navigation.AppNavHost
import dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.theme.BluetoothBroadcastingTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothBroadcastingTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
