package com.example.aplikasijalanlubang

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- DATA MODEL ---
data class Report(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val description: String = "",
    val imageUri: String = "",
    val location: String = "",
    val date: String = "",
    val status: String = "Menunggu"
)

// --- 1. LAYAR MEMBUAT LAPORAN (Create) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReportScreen(
    onNavigateBack: () -> Unit,
    onReportSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Lubang Besar", "Jalan Retak", "Aspal Mengelupas", "Banjir/Genangan", "Lainnya")
    var isLoadingLocation by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Camera Logic
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = tempPhotoUri
    }

    // Location Permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation(context) { loc ->
                locationText = loc
                isLoadingLocation = false
            }
        } else {
            isLoadingLocation = false
            Toast.makeText(context, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Laporan Baru") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize().padding(paddingValues).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kategori Dropdown
            Text("Kategori Masalah", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedTextField(
                    value = category, onValueChange = {}, readOnly = true,
                    label = { Text("Pilih Kategori") },
                    trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Filled.ArrowDropDown, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = { category = item; expanded = false })
                    }
                }
            }

            // Foto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .clickable {
                        // ... (kode kamera tetap sama) ...
                        val photoFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "lapor_${System.currentTimeMillis()}.jpg")
                        tempPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(tempPhotoUri!!)
                        } else {
                            Toast.makeText(context, "Berikan izin kamera dulu", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // >>> INI YANG DIPERBAIKI <<<
                    Image(
                        painter = rememberAsyncImagePainter(imageUri), // Pakai 'painter ='
                        contentDescription = "Foto Bukti",             // Wajib ada
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                        Text("Ambil Foto")
                    }
                }
            }

            // Lokasi
            Text("Lokasi", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = locationText, onValueChange = { locationText = it }, placeholder = { Text("Koordinat/Alamat") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    isLoadingLocation = true
                    locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }, enabled = !isLoadingLocation) {
                    if (isLoadingLocation) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White) else Icon(Icons.Filled.LocationOn, null)
                }
            }

            // Deskripsi
            Text("Deskripsi", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth().height(100.dp), maxLines = 5)

            Button(
                onClick = {
                    if (category.isNotEmpty() && locationText.isNotEmpty()) {
                        val user = auth.currentUser
                        if (user != null) {
                            val newId = db.collection("reports").document().id
                            val report = Report(newId, user.uid, category, description, imageUri.toString(), locationText, SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date()))
                            db.collection("reports").document(newId).set(report)
                                .addOnSuccessListener { Toast.makeText(context, "Terkirim!", Toast.LENGTH_SHORT).show(); onReportSubmitted() }
                        }
                    } else Toast.makeText(context, "Lengkapi data!", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Kirim Laporan") }
        }
    }
}

// --- 2. LAYAR LAPORAN SAYA (Full CRUD: Foto, Lokasi, Kategori, Deskripsi) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val currentUserId = auth.currentUser?.uid

    // State Data Utama
    var reports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // State Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<Report?>(null) }

    // --- STATE FORM EDIT (Lengkap) ---
    var editCategory by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editImageUri by remember { mutableStateOf<Uri?>(null) } // Untuk preview foto baru
    var editImageString by remember { mutableStateOf("") }      // String URI lama/baru untuk database

    // State Dropdown & Loading Edit
    var expandedEdit by remember { mutableStateOf(false) }
    val categories = listOf("Lubang Besar", "Jalan Retak", "Aspal Mengelupas", "Banjir/Genangan", "Lainnya")
    var isUpdatingLocation by remember { mutableStateOf(false) }

    // --- LAUNCHER UNTUK FITUR EDIT ---

    // 1. Launcher Kamera (Edit)
    var tempEditPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val editCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempEditPhotoUri != null) {
            editImageUri = tempEditPhotoUri
            editImageString = tempEditPhotoUri.toString() // Siapkan untuk disimpan ke DB
        }
    }

    // 2. Launcher Lokasi (Edit)
    val editLocationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation(context) { loc ->
                editLocation = loc
                isUpdatingLocation = false
            }
        } else {
            isUpdatingLocation = false
            Toast.makeText(context, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    // Load Data Realtime
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("reports")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshots, _ ->
                    if (snapshots != null) {
                        reports = snapshots.toObjects(Report::class.java)
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Saya") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (reports.isEmpty() && !isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text("Belum ada laporan.") }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { report ->
                    ReportItemCard(
                        report = report,
                        onEditClick = {
                            // ISI DATA AWAL SAAT KLIK EDIT
                            selectedReport = report
                            editCategory = report.category
                            editDescription = report.description
                            editLocation = report.location
                            editImageString = report.imageUri

                            // Coba parse URI lama agar muncul di preview
                            editImageUri = if (report.imageUri.isNotEmpty()) Uri.parse(report.imageUri) else null

                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedReport = report
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // --- DIALOG DELETE ---
        if (showDeleteDialog && selectedReport != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Hapus Laporan?") },
                text = { Text("Laporan ini akan dihapus permanen.") },
                confirmButton = {
                    TextButton(onClick = {
                        db.collection("reports").document(selectedReport!!.id).delete()
                            .addOnSuccessListener { Toast.makeText(context, "Laporan dihapus", Toast.LENGTH_SHORT).show() }
                        showDeleteDialog = false
                    }) { Text("Hapus", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
                }
            )
        }

        // --- DIALOG EDIT (FULL FEATURE) ---
        if (showEditDialog && selectedReport != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Laporan Lengkap") },
                text = {
                    // Gunakan ScrollState agar dialog bisa digulir jika konten panjang
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. EDIT FOTO
                        Text("Foto (Ketuk untuk ganti)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .clickable {
                                    // Logika Kamera Edit
                                    val photoFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "edit_${System.currentTimeMillis()}.jpg")
                                    tempEditPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)

                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        editCameraLauncher.launch(tempEditPhotoUri!!)
                                    } else {
                                        Toast.makeText(context, "Berikan izin kamera dulu", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (editImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(editImageUri),
                                    contentDescription = "Preview Edit",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                            }
                        }

                        // 2. EDIT KATEGORI (Dropdown)
                        Text("Kategori", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Box {
                            OutlinedTextField(
                                value = editCategory,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { expandedEdit = true }) { Icon(Icons.Filled.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth().clickable { expandedEdit = true }
                            )
                            DropdownMenu(expanded = expandedEdit, onDismissRequest = { expandedEdit = false }) {
                                categories.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = { editCategory = item; expandedEdit = false }
                                    )
                                }
                            }
                        }

                        // 3. EDIT LOKASI
                        Text("Lokasi", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editLocation,
                                onValueChange = { editLocation = it },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    isUpdatingLocation = true
                                    editLocationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                },
                                enabled = !isUpdatingLocation,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.width(50.dp)
                            ) {
                                if (isUpdatingLocation) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                                else Icon(Icons.Filled.LocationOn, null)
                            }
                        }

                        // 4. EDIT DESKRIPSI
                        Text("Deskripsi", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // UPDATE KE FIREBASE
                        val updateData = mapOf(
                            "category" to editCategory,
                            "description" to editDescription,
                            "location" to editLocation,
                            "imageUri" to editImageString // Update string URI juga
                        )

                        db.collection("reports").document(selectedReport!!.id)
                            .update(updateData)
                            .addOnSuccessListener { Toast.makeText(context, "Laporan diperbarui!", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(context, "Gagal update", Toast.LENGTH_SHORT).show() }

                        showEditDialog = false
                    }) { Text("Simpan Perubahan") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun ReportItemCard(
    report: Report,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail Foto
            Box(
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (report.imageUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(report.imageUri)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(report.category, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(report.location, style = MaterialTheme.typography.bodySmall, maxLines = 1) // Tampilkan lokasi
                Text(report.status, style = MaterialTheme.typography.labelSmall, color = if(report.status=="Selesai") Color.Green else Color(0xFFFF9800))
            }
            // Tombol Aksi
            Row {
                IconButton(onClick = onEditClick) { Icon(Icons.Filled.Edit, null, tint = Color.Blue) }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Filled.Delete, null, tint = Color.Red) }
            }
        }
    }
}

// Helper Lokasi (Pastikan ini ada di file ReportScreens.kt di bagian paling bawah)
private fun getCurrentLocation(context: Context, onLocationReceived: (String) -> Unit) {
    try {
        LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
            onLocationReceived(if (it != null) "${it.latitude}, ${it.longitude}" else "Lokasi tidak ditemukan")
        }
    } catch (e: SecurityException) { onLocationReceived("Izin lokasi diperlukan") }
}