package com.example.aplikasijalanlubang.ui.theme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aplikasijalanlubang.Screen

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    // Definisi item navigasi
    val items = listOf(
        Triple(Screen.Beranda, Icons.Default.Home, "Beranda"),
        Triple(Screen.Laporan, Icons.AutoMirrored.Filled.List, "Laporan"),
        Triple(Screen.Profil, Icons.Default.Person, "Profil")
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        // Item 1: Beranda
        BottomNavItem(navController, items[0].first.route, items[0].second, items[0].third)

        FloatingActionButton(
            onClick = { navigateTo(navController, Screen.BuatLaporan.route) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Buat Laporan")
        }

        // Item 2: Laporan
        BottomNavItem(navController, items[1].first.route, items[1].second, items[1].third)

        // Item 3: Profil
        BottomNavItem(navController, items[2].first.route, items[2].second, items[2].third)
    }
}

private fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun RowScope.BottomNavItem(
    navController: NavController,
    route: String,
    icon: ImageVector,
    label: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isSelected = currentRoute == route

    NavigationBarItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        selected = isSelected,
        onClick = { navigateTo(navController, route) },
    )
}

@Preview
@Composable
private fun PreviewBottomNav() {
    AppBottomNavigationBar(navController = rememberNavController())
}
