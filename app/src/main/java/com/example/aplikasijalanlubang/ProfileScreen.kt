package com.example.aplikasijalanlubang

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.* // PENTING: Import ini untuk remember, mutableStateOf, LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aplikasijalanlubang.ui.theme.JalanLubangTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // PENTING: Import Firestore

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // State untuk menyimpan data profil
    var userName by remember { mutableStateOf("Memuat...") }
    var userEmail by remember { mutableStateOf("Memuat...") }

    // State untuk statistik profil
    var totalLaporan by remember { mutableIntStateOf(0) }
    var laporanSelesai by remember { mutableIntStateOf(0) }

    // Ambil data saat layar dibuka
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // 1. Ambil Data User
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userName = document.getString("name") ?: "Tanpa Nama"
                        userEmail = document.getString("email") ?: user.email ?: "-"
                    }
                }

            // 2. Hitung Laporan User Ini
            db.collection("reports")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    // Fix: Akses .size sebagai property, bukan function
                    totalLaporan = documents.size()

                    // Fix: Akses .documents untuk mendapatkan List agar bisa di-filter
                    laporanSelesai = documents.documents.filter {
                        it.getString("status") == "Selesai"
                    }.size
                }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tampilkan Data Asli
                Text(text = userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = userEmail, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tampilkan Statistik Asli
                    ProfileInfoRow(label = "Total Laporan", value = totalLaporan.toString(), valueColor = MaterialTheme.colorScheme.primary)

                    // Fix: Divider diganti HorizontalDivider (versi terbaru Material3)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfileInfoRow(label = "Laporan Selesai", value = laporanSelesai.toString(), valueColor = Color(0xFF00C853))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Button(
                onClick = {
                    auth.signOut() // Logout Firebase
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Text("Keluar")
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileScreen() {
    JalanLubangTheme {
        ProfileScreen(onLogout = {})
    }
}