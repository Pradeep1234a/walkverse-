package com.liquidglass.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.liquidglass.calculator.ui.screens.CalculatorScreen
import com.liquidglass.calculator.ui.theme.LiquidGlassTheme
import com.liquidglass.calculator.ui.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge layout with transparent status and navigation bars
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]

        setContent {
            LiquidGlassTheme {
                CalculatorScreen(viewModel = viewModel)
            }
        }
    }
}
