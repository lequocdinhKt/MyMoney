package com.example.mymoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.ui.navigation.AppNavigation
import com.example.mymoney.ui.theme.MyMoneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyMoneyTheme {
                // Tạo NavController và truyền vào navigation graph
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}
