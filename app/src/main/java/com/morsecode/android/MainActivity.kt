package com.morsecode.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morsecode.android.ui.MainScreen
import com.morsecode.android.ui.MorseViewModel
import com.morsecode.android.ui.theme.MorseCodeTimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MorseCodeTimeTheme {
                val viewModel: MorseViewModel = viewModel()
                MainScreen(viewModel)
            }
        }
    }
}
