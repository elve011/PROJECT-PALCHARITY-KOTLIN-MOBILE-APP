package com.example.palcharity.ui1.association

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.palcharity.utils.Association
import com.example.palcharity.utils.FirebaseHelper
import com.example.palcharity.utils.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class ProjectsAffectedActivity : ComponentActivity() {

    private val firebaseHelper = FirebaseHelper()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PalestineModernTheme {
                ProjectsAffectedScreen()
            }
        }
    }

    // --------------------- THEME PALESTINE MODERNE ---------------------
    @Composable
    fun PalestineModernTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Color(0xFF006633),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE8F5E8),
                onPrimaryContainer = Color(0xFF004D26),

                secondary = Color(0xFFB71C1C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFEBEE),
                onSecondaryContainer = Color(0xFF8B0000),

                tertiary = Color(0xFF795548),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFEFEBE9),
                onTertiaryContainer = Color(0xFF5D4037),

                background = Color(0xFFFAFAFA),
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

    // --------------------- ÉCRAN PRINCIPAL PROJETS AFFECTÉS ---------------------
    @Composable
    fun ProjectsAffectedScreen() {
        var association by remember { mutableStateOf<Association?>(null) }
        var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var selectedProject by remember { mutableStateOf<Project?>(null) }
        var showDonorsDialog by remember { mutableStateOf(false) }

        val uid = auth.currentUser?.uid ?: ""
        val context = LocalContext.current

        // Charger les données de l'association et ses projets
        LaunchedEffect(uid) {
            if (uid.isNotEmpty()) {
                isLoading = true
                try {
                    println("🔄 [PROJECTS_AFFECTED] Chargement association...")
                    val loadedAssociation = firebaseHelper.getAssociationById(uid)
                    association = loadedAssociation

                    println("🔍 [PROJECTS_AFFECTED] Chargement projets pour: ${loadedAssociation.name}")
                    val loadedProjects = firebaseHelper.getAssociationProjects(loadedAssociation.name)
                    projects = loadedProjects

                    println("✅ [PROJECTS_AFFECTED] ${loadedProjects.size} projet(s) chargé(s)")
                } catch (e: Exception) {
                    println("❌ [PROJECTS_AFFECTED] Erreur: ${e.message}")
                    Toast.makeText(context, "Erreur de chargement des projets", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }

        val colorScheme = MaterialTheme.colorScheme

        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                            "📊 Projets Affectés",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Suivi des dons par projet 🇵🇸",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
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
                // En-tête informatif
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "👋 ${association?.name ?: "Association"}",
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Retrouvez ici tous vos projets et les dons associés. Suivez l'impact de votre travail !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Chargement de vos projets...",
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = "Aucun projet",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Aucun projet créé",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Commencez par créer votre premier projet !",
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Liste des projets
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(projects) { project ->
                            ProjectAffectedCardModern(
                                project = project,
                                onShowDonors = {
                                    selectedProject = project
                                    showDonorsDialog = true
                                },
                                colorScheme = colorScheme
                            )
                        }
                    }
                }
            }

            // Dialog des donateurs
            if (showDonorsDialog && selectedProject != null) {
                DonorsListDialogModern(
                    project = selectedProject!!,
                    onClose = {
                        showDonorsDialog = false
                        selectedProject = null
                    },
                    colorScheme = colorScheme
                )
            }
        }
    }

    // --------------------- CARTE DE PROJET AFFECTÉ MODERNE ---------------------
    @Composable
    fun ProjectAffectedCardModern(
        project: Project,
        onShowDonors: () -> Unit,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        val progress = if (project.amountNeeded > 0) {
            ((project.amountNeeded - project.amountRemaining) * 100 / project.amountNeeded)
        } else {
            0
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // En-tête du projet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        colorScheme.primary,
                                        colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = "Projet",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            project.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            project.type.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Statistiques du projet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Montant total
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "💰 Objectif",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${project.amountNeeded} DT",
                                style = MaterialTheme.typography.titleSmall,
                                color = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Montant restant
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (project.amountRemaining > 0)
                                colorScheme.secondaryContainer
                            else
                                colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (project.amountRemaining > 0) "⏳ Restant" else "✅ Atteint",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${project.amountRemaining} DT",
                                style = MaterialTheme.typography.titleSmall,
                                color = colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Progression
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "📊 Progression",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$progress%",
                                style = MaterialTheme.typography.titleSmall,
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bouton Voir les donateurs
                Button(
                    onClick = onShowDonors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "👥 Voir les donateurs",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // --------------------- DIALOG LISTE DES DONATEURS MODERNE ---------------------
    @Composable
    fun DonorsListDialogModern(
        project: Project,
        onClose: () -> Unit,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        var donations by remember { mutableStateOf<List<DonationWithLocation>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var selectedDonorLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
        var showLocationMap by remember { mutableStateOf(false) }

        val context = LocalContext.current

        // Charger les donations du projet
        LaunchedEffect(project.id) {
            isLoading = true
            try {
                println("🔍 [DONORS] Chargement donations pour projet: ${project.title}")
                donations = getProjectDonationsWithLocation(project.id)
                println("✅ [DONORS] ${donations.size} donation(s) chargée(s)")
            } catch (e: Exception) {
                println("❌ [DONORS] Erreur: ${e.message}")
                Toast.makeText(context, "Erreur de chargement des donateurs", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }

        if (showLocationMap && selectedDonorLocation != null) {
            LocationViewDialogModern(
                location = selectedDonorLocation!!,
                onClose = {
                    showLocationMap = false
                    selectedDonorLocation = null
                },
                colorScheme = colorScheme
            )
        }

        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fermer")
                }
            },
            title = {
                Text(
                    "👥 Donateurs - ${project.title}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                ) {
                    // En-tête informatif
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Information",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${donations.size} donateur(s) pour ce projet",
                                color = colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Chargement des donateurs...",
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (donations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = "Aucun donateur",
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Aucun donateur pour ce projet",
                                    color = colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Les dons apparaîtront ici",
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Liste des donateurs
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(donations) { donation ->
                                DonorCardModern(
                                    donation = donation,
                                    onShowLocation = { location ->
                                        selectedDonorLocation = location
                                        showLocationMap = true
                                    },
                                    colorScheme = colorScheme
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        )
    }

    // --------------------- CARTE DONATEUR MODERNE ---------------------
    @Composable
    fun DonorCardModern(
        donation: DonationWithLocation,
        onShowLocation: (Pair<Double, Double>) -> Unit,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // En-tête du donneur
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorScheme.primaryContainer, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Donateur",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${donation.donorFirstName} ${donation.donorLastName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Donné le ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(donation.timestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Détails du don
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Type de don",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            getDonationTypeIcon(donation.typeDonation) + " " +
                                    donation.typeDonation.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (donation.typeDonation == "money") {
                        Column {
                            Text(
                                "Montant",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${donation.amount} DT",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    val context = LocalContext.current // ← Ajouter cette ligne au début de la fonction
                    // Bouton localisation
                    Button(
                        onClick = {
                            donation.location?.let { location ->
                                onShowLocation(location)
                            } ?: run {
                                // Utiliser le contexte passé en paramètre ou récupéré dans le composable parent
                                Toast.makeText(context, "📍 Localisation non disponible", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = donation.location != null,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (donation.location != null)
                                colorScheme.tertiaryContainer
                            else
                                colorScheme.surfaceVariant,
                            contentColor = if (donation.location != null)
                                colorScheme.tertiary
                            else
                                colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Localisation",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Carte",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // --------------------- DIALOG AFFICHAGE LOCALISATION ---------------------
    @Composable
    fun LocationViewDialogModern(
        location: Pair<Double, Double>,
        onClose: () -> Unit,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        val context = LocalContext.current
        var isMapReady by remember { mutableStateOf(false) }

        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0
            }
        }

        LaunchedEffect(Unit) {
            try {
                Configuration.getInstance().load(
                    context,
                    PreferenceManager.getDefaultSharedPreferences(context)
                )

                val geoPoint = GeoPoint(location.first, location.second)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(geoPoint)

                val marker = Marker(mapView).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Position du donneur"
                }
                mapView.overlays.clear()
                mapView.overlays.add(marker)

                isMapReady = true
            } catch (e: Exception) {
                isMapReady = true
                println("❌ [MAP] Erreur initialisation carte: ${e.message}")
            }
        }

        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Fermer")
                }
            },
            title = {
                Text(
                    "🗺️ Localisation du donneur",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            },
            text = {
                Column {
                    // Carte
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
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
                                    Text(
                                        "Chargement de la carte...",
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Coordonnées
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "📍 Coordonnées GPS:",
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onPrimaryContainer,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Lat: ${String.format("%.6f", location.first)}",
                                    fontSize = 13.sp,
                                    color = colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Lng: ${String.format("%.6f", location.second)}",
                                    fontSize = 13.sp,
                                    color = colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.padding(20.dp)
        )
    }

    // --------------------- FONCTIONS UTILITAIRES ---------------------

    private fun getDonationTypeIcon(type: String): String {
        return when (type) {
            "money" -> "💵"
            "food" -> "🍎"
            "clothes" -> "👕"
            else -> "🎁"
        }
    }

    // Data class pour les donations avec localisation
    data class DonationWithLocation(
        val donorId: String,
        val projectId: String,
        val donorFirstName: String,
        val donorLastName: String,
        val typeDonation: String,
        val amount: Int,
        val timestamp: Long,
        val projectTitle: String,
        val associationName: String,
        val location: Pair<Double, Double>?
    )

    // Fonction corrigée pour récupérer les donations avec localisation
    private suspend fun getProjectDonationsWithLocation(projectId: String): List<DonationWithLocation> {
        return try {
            val snap = database.child("don_projet").get().await()
            val donations = mutableListOf<DonationWithLocation>()

            for (d in snap.children) {
                try {
                    val donationProjectId = d.child("projectId").getValue(String::class.java)

                    if (donationProjectId == projectId) {
                        val locationSnap = d.child("location")
                        val location = if (locationSnap.exists()) {
                            val lat = locationSnap.child("lat").getValue(Double::class.java)
                            val lng = locationSnap.child("lng").getValue(Double::class.java)
                            if (lat != null && lng != null) Pair(lat, lng) else null
                        } else {
                            null
                        }

                        donations.add(
                            DonationWithLocation(
                                donorId = d.child("uid").getValue(String::class.java) ?: "",
                                projectId = donationProjectId ?: "",
                                donorFirstName = d.child("donorFirstName").getValue(String::class.java) ?: "",
                                donorLastName = d.child("donorLastName").getValue(String::class.java) ?: "",
                                typeDonation = d.child("type").getValue(String::class.java) ?: "",
                                amount = d.child("amount").getValue(Int::class.java) ?: 0,
                                timestamp = d.child("timestamp").getValue(Long::class.java) ?: 0L,
                                projectTitle = d.child("projectTitle").getValue(String::class.java) ?: "",
                                associationName = d.child("associationName").getValue(String::class.java) ?: "",
                                location = location
                            )
                        )
                    }
                } catch (e: Exception) {
                    println("❌ [DONATION] Erreur parsing donation: ${e.message}")
                    continue
                }
            }

            // Trier par timestamp (plus récent en premier)
            donations.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            println("❌ [FIREBASE] Erreur récupération donations: ${e.message}")
            emptyList()
        }
    }
}