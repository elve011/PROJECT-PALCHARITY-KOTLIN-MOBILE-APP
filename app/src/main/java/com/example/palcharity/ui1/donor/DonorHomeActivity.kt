package com.example.palcharity.ui1.donor

// ---------------------- Android / OS ----------------------
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.location.Geocoder

// ---------------------- Compose UI ----------------------
import android.os.Environment
import java.io.FileInputStream
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------- Activity / Result APIs ----------------------
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

// ---------------------- Firebase ----------------------
import com.google.firebase.auth.FirebaseAuth

// ---------------------- Google Maps ----------------------
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// ---------------------- Utils / App ----------------------
import com.example.palcharity.R
import com.example.palcharity.utils.*
// OpenStreetMap imports
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager

// ---------------------- Coroutines / Network ----------------------
import kotlinx.coroutines.*
import java.net.URL

class DonorHomeActivity : ComponentActivity() {

    private val firebaseHelper = FirebaseHelper()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var mlHelper: MLHelper
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mlHelper = MLHelper(this)
        locationHelper = LocationHelper(this)

        setContent { PalestineModernTheme { HomeScreen() } }
    }

    // --------------------- THEME PALESTINE MODERNE ---------------------
    @Composable
    fun PalestineModernTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF006633),           // Vert Palestine profond
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE8F5E8),
                onPrimaryContainer = Color(0xFF004D26),

                secondary = Color(0xFFB71C1C),         // Rouge Palestine riche
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFEBEE),
                onSecondaryContainer = Color(0xFF8B0000),

                tertiary = Color(0xFF795548),          // Brun terreux
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFEFEBE9),
                onTertiaryContainer = Color(0xFF5D4037),

                background = Color(0xFFFAFAFA),        // Fond moderne
                onBackground = Color(0xFF1A1A1A),

                surface = Color.White,
                onSurface = Color(0xFF1A1A1A),

                surfaceVariant = Color(0xFFEEEEEE),
                onSurfaceVariant = Color(0xFF444444),

                error = Color(0xFFD32F2F),
                onError = Color.White
            ),
            content = content
        )
    }

    // --------------------- Chargement d'image ---------------------
    @Composable
    fun loadImage(imagePath: String): ImageBitmap? {
        var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
        val context = LocalContext.current

        LaunchedEffect(imagePath) {
            withContext(Dispatchers.IO) {
                try {
                    val fileName = extractFileName(imagePath)

                    val downloadDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    val file = java.io.File(downloadDir, fileName)

                    if (file.exists() && file.canRead()) {
                        try {
                            val inputStream = FileInputStream(file)
                            val loadedBitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream.close()

                            if (loadedBitmap != null) {
                                bitmap = loadedBitmap.asImageBitmap()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return bitmap
    }

    private fun extractFileName(path: String): String {
        return when {
            path.contains("\\") -> path.substringAfterLast("\\")
            path.contains("/") -> path.substringAfterLast("/")
            else -> path
        }
    }

    @Composable
    fun ProjectImage(imageUrl: String, contentDescription: String) {
        val image = loadImage(imageUrl)
        val colorScheme = MaterialTheme.colorScheme

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = true
                ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Image en chargement",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Chargement...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // --------------------- HOME SCREEN MODERNE ---------------------
    @Composable
    fun HomeScreen() {
        var projects by remember { mutableStateOf(listOf<Project>()) }
        var searchType by remember { mutableStateOf("") }
        var filterText by remember { mutableStateOf("") }
        var showDonationForm by remember { mutableStateOf(false) }
        var selectedProject by remember { mutableStateOf<Project?>(null) }

        LaunchedEffect(Unit) {
            projects = firebaseHelper.getAllProjects()
        }

        val colorScheme = MaterialTheme.colorScheme

        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "🤲 PalCharity",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Solidarité avec Gaza 🇵🇸",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                startActivity(Intent(this@DonorHomeActivity, DonationActivity::class.java))
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "Profil",
                                    fontSize = 10.sp,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                startActivity(Intent(this@DonorHomeActivity, HistoryActivity::class.java))
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = colorScheme.secondaryContainer,
                                    shape = CircleShape
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = "Historique",
                                    tint = colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "Historique",
                                    fontSize = 10.sp,
                                    color = colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Barre de recherche moderne
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(25.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchType,
                            onValueChange = { searchType = it },
                            label = { Text("🔍 Rechercher un type de don...") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { filterText = searchType },
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = colorScheme.primary,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Rechercher",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Indicateur de résultats
                if (filterText.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "📋 ${projects.count { it.type.contains(filterText, true) }} projet(s) trouvé(s)",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Liste des projets moderne
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects.filter {
                        it.type.contains(filterText, true) || filterText.isBlank()
                    }) { project ->
                        ProjectCardModern(project = project) {
                            selectedProject = project
                            showDonationForm = true
                        }
                    }
                }
            }

            // Formulaire de don
            if (showDonationForm && selectedProject != null) {
                DonationFormModern(selectedProject!!) { showDonationForm = false }
            }
        }
    }

    @Composable
    fun ProjectCardModern(project: Project, onDonateClick: () -> Unit) {
        val colorScheme = MaterialTheme.colorScheme

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    clip = true
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Image du projet
                ProjectImage(imageUrl = project.imageUrl, contentDescription = project.title)

                Spacer(modifier = Modifier.height(16.dp))

                // Titre et description
                Text(
                    project.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Montant restant avec style moderne
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Montant restant à collecter",
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            "${project.amountRemaining} DT",
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton de don moderne
                Button(
                    onClick = onDonateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Faire un don",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // --------------------- FORMULAIRE DE DON MODERNE ---------------------
    @Composable
    fun DonationFormModern(project: Project, onClose: () -> Unit) {
        var typeDonation by remember { mutableStateOf("money") }
        var amount by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var location by remember { mutableStateOf<LatLng?>(null) }
        var showMap by remember { mutableStateOf(false) }

        val uid = auth.currentUser?.uid ?: ""
        val donorState = remember { mutableStateOf<Donor?>(null) }
        val colorScheme = MaterialTheme.colorScheme

        LaunchedEffect(uid) {
            donorState.value = firebaseHelper.getDonorById(uid)
        }

        val pickImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                imageUri?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    typeDonation = mlHelper.predictImageLabel(bitmap)
                }
            }
        }

        if (showMap) {
            LocationPickerModern(
                onConfirm = { latLng ->
                    location = latLng
                    showMap = false
                },
                onCancel = { showMap = false }
            )
        }

        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                firebaseHelper.addDonation(
                                    donorId = uid,
                                    project = project,
                                    typeDonation = typeDonation,
                                    amount = if (typeDonation == "money") amount.toIntOrNull() ?: 0 else 0,
                                    location = location?.let { Pair(it.latitude, it.longitude) }
                                )
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@DonorHomeActivity,
                                        "🎉 Don ajouté avec succès !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onClose()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Valider le don")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onClose,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Annuler")
                }
            },
            title = {
                Text(
                    "🎁 Faire un don",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // Informations du projet
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Projet: ${project.title}",
                                color = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Type: ${project.type}",
                                color = colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Informations du donneur
                    donorState.value?.let { donor ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "👤 Donneur",
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${donor.firstName} ${donor.lastName}",
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Text(
                                    donor.email,
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Bouton import image
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            pickImageLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer une image du don")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Type de don
                    DropdownMenuDonationTypeModern(typeDonation) { typeDonation = it }

                    if (typeDonation == "money") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("💵 Montant à donner (DT)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bouton localisation
                    OutlinedButton(
                        onClick = { showMap = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📍 Choisir la localisation")
                    }
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }

    // --------------------- MENU TYPE DE DON MODERNE ---------------------
    @Composable
    fun DropdownMenuDonationTypeModern(selected: String, onSelect: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        val colorScheme = MaterialTheme.colorScheme
        val options = listOf(
            "💵 Money" to "money",
            "🍎 Food" to "food",
            "👕 Clothes" to "clothes"
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.onSurface
                )
            ) {
                Text("Type: ${options.find { it.second == selected }?.first ?: selected}")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colorScheme.surface)
            ) {
                options.forEach { (display, value) ->
                    DropdownMenuItem(
                        text = { Text(display) },
                        onClick = {
                            onSelect(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    // --------------------- LOCATION PICKER MODERNE ---------------------
    @Composable
    fun LocationPickerModern(onConfirm: (LatLng) -> Unit, onCancel: () -> Unit) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val colorScheme = MaterialTheme.colorScheme

        var searchQuery by remember { mutableStateOf("") }
        var selectedPosition by remember { mutableStateOf(LatLng(31.9522, 35.2332)) }
        var isMapReady by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0
                controller.setZoom(12.0)
            }
        }

        LaunchedEffect(Unit) {
            try {
                Configuration.getInstance().load(
                    context,
                    PreferenceManager.getDefaultSharedPreferences(context)
                )

                val initialPoint = GeoPoint(selectedPosition.latitude, selectedPosition.longitude)
                mapView.controller.setCenter(initialPoint)

                val marker = Marker(mapView).apply {
                    position = initialPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Position sélectionnée"
                }
                mapView.overlays.clear()
                mapView.overlays.add(marker)

                mapView.setOnClickListener { event ->
                    try {
                        val geoPoint = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt())
                        selectedPosition = LatLng(geoPoint.latitude, geoPoint.longitude)
                        marker.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                        mapView.controller.animateTo(GeoPoint(geoPoint.latitude, geoPoint.longitude), 14.0, 1000L)
                    } catch (e: Exception) {}
                    true
                }

                isMapReady = true
            } catch (e: Exception) {
                isMapReady = true
            }
        }

        fun handleSearch() {
            if (searchQuery.isBlank()) {
                Toast.makeText(context, "Veuillez saisir un lieu", Toast.LENGTH_SHORT).show()
                return
            }

            isLoading = true
            coroutineScope.launch {
                try {
                    val addresses = withContext(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context)
                            if (Geocoder.isPresent()) geocoder.getFromLocationName(searchQuery, 1) else null
                        } catch (e: Exception) { null }
                    }

                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val latLng = LatLng(addr.latitude, addr.longitude)
                        selectedPosition = latLng
                        try {
                            val point = GeoPoint(latLng.latitude, latLng.longitude)
                            mapView.overlays.find { it is Marker }?.let {
                                (it as Marker).position = point
                                mapView.controller.animateTo(point, 14.0, 1000L)
                            }
                            Toast.makeText(context, "📍 Localisation trouvée!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Position mise à jour", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Localisation introuvable", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erreur de recherche", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }

        AlertDialog(
            onDismissRequest = onCancel,
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(selectedPosition)
                        Toast.makeText(context, "✅ Position confirmée!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Annuler")
                }
            },
            title = {
                Text(
                    "🗺️ Choisir votre localisation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // Barre de recherche
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("🔍 Rechercher un lieu") },
                            placeholder = { Text("Ex: Gaza, Palestine") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { handleSearch() },
                            enabled = !isLoading,
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.secondary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Rechercher",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Carte
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMapReady) {
                                AndroidView(
                                    factory = { mapView },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Chargement de la carte...", color = colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Coordonnées
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "📍 Position sélectionnée:",
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Lat: ${String.format("%.6f", selectedPosition.latitude)}",
                                    fontSize = 13.sp,
                                    color = colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Lng: ${String.format("%.6f", selectedPosition.longitude)}",
                                    fontSize = 13.sp,
                                    color = colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "👆 Cliquez sur la carte pour sélectionner une position",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }
}
