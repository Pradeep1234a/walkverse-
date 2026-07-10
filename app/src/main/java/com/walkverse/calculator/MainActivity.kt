package com.walkverse.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.walkverse.calculator.ui.screens.CalculatorScreen
import com.walkverse.calculator.ui.theme.LiquidGlassTheme
import com.walkverse.calculator.ui.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure edge-to-edge layout styling
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LiquidGlassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val calculatorViewModel: CalculatorViewModel = viewModel()
                    CalculatorScreen(viewModel = calculatorViewModel)
                }
            }
        }
    }
}
