package com.example.aplikasijalanlubang

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aplikasijalanlubang.ui.theme.JalanLubangTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue          // <--- Ini PENTING buat kata kunci 'by'
import androidx.compose.runtime.mutableIntStateOf // <--- Ini yang bikin error merah
import androidx.compose.runtime.remember          // <--- Ini juga
import androidx.compose.runtime.setValue

@Composable
fun MainDashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    // State untuk menghitung jumlah laporan
    var countMenunggu by remember { mutableIntStateOf(0) }
    var countDikerjakan by remember { mutableIntStateOf(0) }
    var countSelesai by remember { mutableIntStateOf(0) }
    var totalLaporanSaya by remember { mutableIntStateOf(0) }

    // Ambil data statistik real-time
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("reports")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        totalLaporanSaya = snapshots.size()
                        // Hitung berdasarkan status string
                        countMenunggu = snapshots.documents.filter { it.getString("status") == "Menunggu" }.size
                        countDikerjakan = snapshots.documents.filter { it.getString("status") == "Dikerjakan" }.size
                        countSelesai = snapshots.documents.filter { it.getString("status") == "Selesai" }.size
                    }
                }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Laporin Aja!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Laporkan masalah infrastruktur di sekitar Anda", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActionCard(
                    title = "Buat Laporan",
                    subtitle = "Laporkan masalah baru",
                    icon = Icons.Default.Add,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.BuatLaporan.route) }
                )

                ActionCard(
                    title = "Laporan Saya",
                    subtitle = "$totalLaporanSaya laporan aktif", // Angka Asli
                    icon = Icons.Default.List,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Laporan.route) }
                )
            }
        }

        // Kirim data hitungan asli ke kartu statistik
        item {
            ReportStatisticCard(countMenunggu, countDikerjakan, countSelesai)
        }
    }
}

// Update parameter fungsi ini agar menerima angka
@Composable
fun ReportStatisticCard(menunggu: Int, dikerjakan: Int, selesai: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Statistik Laporan Anda", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatisticItem(label = "Menunggu", count = menunggu, color = Color.Gray)
                StatisticItem(label = "Dikerjakan", count = dikerjakan, color = Color(0xFF00C853))
                StatisticItem(label = "Selesai", count = selesai, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ... (ActionCard dan StatisticItem tetap sama) ...
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatisticItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = color,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDashboard() {
    JalanLubangTheme {
        MainDashboardScreen(navController = rememberNavController())
    }
}