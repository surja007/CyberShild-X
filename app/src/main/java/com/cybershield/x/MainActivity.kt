package com.cybershield.x

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cybershield.x.ui.CyberShieldNavigation
import com.cybershield.x.ui.theme.CyberShieldXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContent {
                CyberShieldXTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CyberShieldNavigation()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
}
