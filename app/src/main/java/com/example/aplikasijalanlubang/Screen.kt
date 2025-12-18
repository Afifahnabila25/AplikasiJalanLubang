package com.example.aplikasijalanlubang

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Beranda : Screen("beranda")
    object Laporan : Screen("laporan")
    object Profil : Screen("profil")
    object BuatLaporan : Screen("buat_laporan")
}
