package com.example.aplikasijalanlubang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aplikasijalanlubang.ui.theme.AppBottomNavigationBar
import com.example.aplikasijalanlubang.ui.theme.JalanLubangTheme
import com.google.firebase.FirebaseApp
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val startDestination = if (auth.currentUser != null) Screen.Beranda.route else Screen.Login.route
        setContent {
            JalanLubangTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JalanLubangApp(startRoute = startDestination)
                }
            }
        }
    }
}

@Composable
fun JalanLubangApp(startRoute: String) { // Terima parameter
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom Bar tidak ditampilkan di halaman Login dan SignUp
    val showBottomBar = currentRoute !in listOf(Screen.Login.route, Screen.SignUp.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startRoute, // Gunakan startRoute dinamis
            modifier = Modifier.padding(paddingValues)
        ) {
            // Halaman Login
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Beranda.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }

            // Halaman Daftar (Sign Up)
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        // Setelah daftar sukses, masuk ke beranda
                        navController.navigate(Screen.Beranda.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack() // Kembali ke login
                    }
                )
            }

            composable(Screen.Beranda.route) {
                MainDashboardScreen(navController = navController)
            }
            composable(Screen.Laporan.route) {
                MyReportScreen(navController = navController)
            }
            composable(Screen.Profil.route) {
                ProfileScreen(onLogout = {
                    navController.navigate(Screen.Login.route) {
                        // Menghapus semua tumpukan navigasi sebelumnya agar tombol back keluar dari app
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
            composable(Screen.BuatLaporan.route) {
                CreateReportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onReportSubmitted = {
                        // Kembali ke beranda atau daftar laporan setelah kirim
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
