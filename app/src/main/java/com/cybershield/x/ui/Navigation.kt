package com.cybershield.x.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cybershield.x.ui.dashboard.DashboardScreen
import com.cybershield.x.ui.scanner.ScannerScreen
import com.cybershield.x.ui.privacy.PrivacyScreen
import com.cybershield.x.ui.locker.LockerScreen
import com.cybershield.x.ui.urlscanner.UrlScannerScreen
import com.cybershield.x.ui.parental.ParentalControlScreen
import com.cybershield.x.ui.remotewipe.RemoteWipeScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
    object Privacy : Screen("privacy")
    object Locker : Screen("locker")
    object UrlScanner : Screen("url_scanner")
    object Parental : Screen("parental")
    object RemoteWipe : Screen("remote_wipe")
}

@Composable
fun CyberShieldNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        
        composable(Screen.Scanner.route) {
            ScannerScreen(navController)
        }
        
        composable(Screen.Privacy.route) {
            PrivacyScreen(navController)
        }
        
        composable(Screen.Locker.route) {
            LockerScreen(navController)
        }
        
        composable(Screen.UrlScanner.route) {
            UrlScannerScreen(navController)
        }
        
        composable(Screen.Parental.route) {
            ParentalControlScreen(navController)
        }
        
        composable(Screen.RemoteWipe.route) {
            RemoteWipeScreen(navController)
        }
    }
}
