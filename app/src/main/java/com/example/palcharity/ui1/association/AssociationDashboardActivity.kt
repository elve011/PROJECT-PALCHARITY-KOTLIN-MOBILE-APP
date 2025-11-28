package com.example.palcharity.ui1.association

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.tasks.await
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.palcharity.utils.Association
import com.example.palcharity.utils.FirebaseHelper
import com.example.palcharity.utils.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssociationDashboardActivity : ComponentActivity() {

    private val firebaseHelper = FirebaseHelper()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PalestineModernTheme {
                AssociationDashboardScreen()
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

    // --------------------- ÉCRAN PRINCIPAL ASSOCIATION ---------------------
    @Composable
    fun AssociationDashboardScreen() {
        var association by remember { mutableStateOf<Association?>(null) }
        var showAddProjectForm by remember { mutableStateOf(false) }
        var showEditProfile by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }

        val uid = auth.currentUser?.uid ?: ""
        val context = LocalContext.current

        // DEBUG: Log de démarrage
        println("🔄 [DASHBOARD] Début chargement - UID: $uid")

        // Charger les données de l'association
        LaunchedEffect(uid) {
            if (uid.isNotEmpty()) {
                isLoading = true
                try {
                    println("🔍 [DASHBOARD] Vérification existence association...")
                    val associationExists = firebaseHelper.associationExists(uid)

                    if (associationExists) {
                        println("✅ [DASHBOARD] Association existe, récupération...")
                        val loadedAssociation = firebaseHelper.getAssociationById(uid)
                        association = loadedAssociation
                        println("🎯 [DASHBOARD] Association récupérée:")
                        println("   - Nom: '${loadedAssociation.name}'")
                        println("   - Email: '${loadedAssociation.email}'")
                        println("   - UID: '${loadedAssociation.uid}'")
                    } else {
                        println("❌ [DASHBOARD] Association n'existe pas dans Firebase")
                        Toast.makeText(context, "Erreur: Association non trouvée", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    println("💥 [DASHBOARD] Erreur lors du chargement: ${e.message}")
                    Toast.makeText(context, "Erreur de chargement du profil", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                    println("🏁 [DASHBOARD] Fin chargement - isLoading: $isLoading")
                }
            } else {
                isLoading = false
                println("❌ [DASHBOARD] UID vide")
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
                            "🏢 Espace Association",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Solidarité Palestine 🇵🇸",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (isLoading) {
                            Toast.makeText(context, "Chargement du profil en cours...", Toast.LENGTH_SHORT).show()
                        } else if (association?.name.isNullOrEmpty()) {
                            Toast.makeText(context, "Profil non chargé", Toast.LENGTH_SHORT).show()
                        } else {
                            showAddProjectForm = true
                        }
                    },
                    containerColor = colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(75.dp)
                        .shadow(12.dp, shape = CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Nouveau projet",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(colorScheme.background)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Chargement de votre profil...",
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    ProfileCardModern(
                        association = association,
                        onEditClick = { showEditProfile = true },
                        onHistoryClick = {
                            val intent = Intent(context, ProjectsAffectedActivity::class.java)
                            context.startActivity(intent)
                        },
                        colorScheme = colorScheme
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    WelcomeSectionModern(
                        association = association,
                        colorScheme = colorScheme
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    QuickActionsSectionModern(
                        onAddProject = {
                            if (association?.name.isNullOrEmpty()) {
                                Toast.makeText(context, "Profil non chargé", Toast.LENGTH_SHORT).show()
                            } else {
                                showAddProjectForm = true
                            }
                        },
                        onHistoryClick = {
                            val intent = Intent(context, ProjectsAffectedActivity::class.java)
                            context.startActivity(intent)
                        },
                        association = association,
                        colorScheme = colorScheme
                    )
                }
            }

            if (showEditProfile) {
                EditProfileFormModern(
                    association = association,
                    onClose = {
                        showEditProfile = false
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val updatedAssociation = firebaseHelper.getAssociationById(uid)
                                withContext(Dispatchers.Main) {
                                    association = updatedAssociation
                                    println("🔄 [DASHBOARD] Profil rechargé - Nom: '${updatedAssociation.name}'")
                                }
                            } catch (e: Exception) {
                                println("❌ [DASHBOARD] Erreur rechargement: ${e.message}")
                            }
                        }
                    }
                )
            }

            if (showAddProjectForm && association != null && !association!!.name.isNullOrEmpty()) {
                AddProjectFormModern(
                    associationUid = association!!.uid,
                    onClose = { showAddProjectForm = false }
                )
            }
        }
    }

    // --------------------- COMPOSANTS MODERNES ---------------------

    @Composable
    fun ProfileCardModern(
        association: Association?,
        onEditClick: () -> Unit,
        onHistoryClick: () -> Unit,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
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
                            contentDescription = "Association",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            association?.name ?: "Nom non disponible",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            association?.email ?: "Email non disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "ID: ${association?.uid?.take(8)}...",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Modifier le profil",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = onHistoryClick,
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = colorScheme.tertiaryContainer,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "Historique des projets",
                                tint = colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WelcomeSectionModern(
        association: Association?,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        val hasProfile = !association?.name.isNullOrEmpty()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasProfile) {
                    colorScheme.primaryContainer.copy(alpha = 0.2f)
                } else {
                    colorScheme.errorContainer.copy(alpha = 0.2f)
                }
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    if (hasProfile) "🎯 Bienvenue ${association?.name}!" else "👋 Profil non chargé",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (hasProfile) {
                        "Vous êtes prêt à créer des projets impactants pour soutenir la cause palestinienne."
                    } else {
                        "Impossible de charger votre profil. Veuillez réessayer."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }

    @Composable
    fun QuickActionsSectionModern(
        onAddProject: () -> Unit,
        onHistoryClick: () -> Unit,
        association: Association?,
        colorScheme: androidx.compose.material3.ColorScheme
    ) {
        val hasProfile = !association?.name.isNullOrEmpty()

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "⚡ Actions Rapides",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bouton Nouveau Projet
                Card(
                    onClick = onAddProject,
                    enabled = hasProfile,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .shadow(8.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasProfile) {
                            colorScheme.surface
                        } else {
                            colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (hasProfile) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Nouveau Projet",
                                tint = if (hasProfile) colorScheme.primary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nouveau Projet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (hasProfile) "Créer un nouveau projet" else "Profil requis",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Bouton Historique
                Card(
                    onClick = onHistoryClick,
                    enabled = hasProfile,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .shadow(8.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasProfile) {
                            colorScheme.surface
                        } else {
                            colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (hasProfile) colorScheme.tertiaryContainer else colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "Historique",
                                tint = if (hasProfile) colorScheme.tertiary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Historique",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (hasProfile) "Voir les projets" else "Profil requis",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }

    // --------------------- FORMULAIRES MODERNES ---------------------

    @Composable
    fun EditProfileFormModern(association: Association?, onClose: () -> Unit) {
        var name by remember { mutableStateOf(association?.name ?: "") }
        var email by remember { mutableStateOf(association?.email ?: "") }
        var isLoading by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val colorScheme = MaterialTheme.colorScheme
        val uid = auth.currentUser?.uid ?: ""

        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            Toast.makeText(context, "L'email ne peut pas être vide", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Mettre à jour seulement l'email dans Firebase
                                withContext(Dispatchers.IO) {
                                    database.child("associations").child(uid).updateChildren(
                                        mapOf("email" to email)
                                    ).await()
                                }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "✅ Email mis à jour avec succès !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isLoading = false
                                    onClose()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "❌ Erreur lors de la mise à jour: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enregistrer")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onClose,
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.onSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Annuler")
                }
            },
            title = {
                Text(
                    "✏️ Modifier l'Email",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Email de l'association *",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Entrez l'email de l'association") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Nom de l'association: ${association?.name ?: "Non disponible"}",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "* Champ obligatoire",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.padding(24.dp)
        )
    }

    @Composable
    fun AddProjectFormModern(associationUid: String, onClose: () -> Unit) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var amountNeeded by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("money") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var imageFileName by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var associationName by remember { mutableStateOf("") }
        var showTypeDialog by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val colorScheme = MaterialTheme.colorScheme

        val options = listOf(
            "💵 Argent" to "money",
            "🍎 Nourriture" to "food",
            "👕 Vêtements" to "clothes"
        )

        // Récupérer le nom de l'association depuis Firebase
        LaunchedEffect(associationUid) {
            if (associationUid.isNotEmpty()) {
                try {
                    println("🔍 [PROJET] Récupération association pour UID: $associationUid")
                    val association = firebaseHelper.getAssociationById(associationUid)
                    associationName = association.name
                    println("✅ [PROJET] Nom d'association récupéré: '$associationName'")
                } catch (e: Exception) {
                    println("❌ [PROJET] Erreur récupération nom association: ${e.message}")
                    associationName = "Association Inconnue"
                }
            }
        }

        val pickImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                imageFileName = imageUri?.let { uri ->
                    extractFileNameFromUri(uri, context)
                }
            }
        }

        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(
                    onClick = {
                        // Validation améliorée
                        if (title.isBlank() || description.isBlank() || amountNeeded.isBlank()) {
                            Toast.makeText(context, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Validation du montant pour tous les types
                        val amount = amountNeeded.toIntOrNull()
                        if (amount == null || amount <= 0) {
                            Toast.makeText(context, "Le montant doit être un nombre positif", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (associationName.isBlank()) {
                            Toast.makeText(context, "Erreur: Impossible de récupérer le nom de l'association", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                println("🚀 [PROJET] Création projet pour: $associationName")

                                val project = Project(
                                    id = "",
                                    title = title,
                                    description = description,
                                    amountNeeded = amount,
                                    amountRemaining = amount,
                                    associationName = associationName,
                                    type = type,
                                    imageUrl = imageFileName ?: "default_project.jpg"
                                )

                                firebaseHelper.addProject(project)

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "✅ Projet '$title' créé avec succès !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isLoading = false
                                    onClose()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "❌ Erreur création projet: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading && associationName.isNotEmpty() &&
                            title.isNotBlank() && description.isNotBlank() && amountNeeded.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Créer le projet 🇵🇸",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onClose,
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Annuler")
                }
            },
            title = {
                Text(
                    "🆕 Nouveau Projet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    // Header informatif
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Association",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        associationName,
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Création de projet",
                                        color = colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 📝 Section Titre
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Title,
                                    contentDescription = "Titre",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Titre du projet *",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = {
                                    Text(
                                        "Ex: Aide alimentaire d'urgence à Gaza",
                                        color = colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colorScheme.surface,
                                    unfocusedContainerColor = colorScheme.surface,
                                    focusedIndicatorColor = colorScheme.primary,
                                    unfocusedIndicatorColor = colorScheme.outline,
                                    focusedTextColor = colorScheme.onSurface,
                                    unfocusedTextColor = colorScheme.onSurface
                                ),
                                singleLine = true
                            )
                        }
                    }

                    // 📖 Section Description
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Description",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Description du projet *",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        "Décrivez les objectifs, l'impact et les bénéficiaires de votre projet...",
                                        color = colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colorScheme.surface,
                                    unfocusedContainerColor = colorScheme.surface,
                                    focusedIndicatorColor = colorScheme.primary,
                                    unfocusedIndicatorColor = colorScheme.outline
                                ),
                                maxLines = 5
                            )
                        }
                    }

                    // 🎯 Section Type de don
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = "Type de don",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Type de don *",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Bouton de sélection du type
                            OutlinedButton(
                                onClick = { showTypeDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = colorScheme.surface,
                                    contentColor = colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    options.find { it.second == type }?.first ?: "Sélectionner le type",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Choisir le type",
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }

                            // Indication du type sélectionné
                            if (type.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Type sélectionné: ${options.find { it.second == type }?.first}",
                                    color = colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // 💰 Section Montant (TOUJOURS AFFICHÉ)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = "Montant",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Montant à collecter *",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = amountNeeded,
                                onValueChange = {
                                    amountNeeded = it.filter { char -> char.isDigit() }
                                },
                                placeholder = {
                                    Text(
                                        "Entrez le montant en dinars tunisiens",
                                        color = colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colorScheme.surface,
                                    unfocusedContainerColor = colorScheme.surface,
                                    focusedIndicatorColor = colorScheme.primary,
                                    unfocusedIndicatorColor = colorScheme.outline
                                ),
                                singleLine = true,
                                prefix = { Text("DT ") },
                                supportingText = {
                                    Text(
                                        "Montant nécessaire pour réaliser le projet",
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }

                    // 🖼️ Section Image
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Photo,
                                    contentDescription = "Image",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Image du projet",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_PICK)
                                    intent.type = "image/*"
                                    pickImageLauncher.launch(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = colorScheme.surface,
                                    contentColor = colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choisir une image")
                            }
                            if (imageFileName != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "📎 $imageFileName",
                                    color = colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Information sur les champs obligatoires
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.secondaryContainer.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Information",
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "* Champs obligatoires",
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(20.dp)
                .fillMaxHeight(0.9f)
        )

        // Dialog pour sélectionner le type de don
        if (showTypeDialog) {
            AlertDialog(
                onDismissRequest = { showTypeDialog = false },
                title = {
                    Text(
                        "🎯 Choisir le type de don",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        options.forEach { (display, value) ->
                            Card(
                                onClick = {
                                    type = value
                                    showTypeDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (type == value)
                                        colorScheme.primaryContainer
                                    else
                                        colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        display,
                                        modifier = Modifier.weight(1f),
                                        color = if (type == value)
                                            colorScheme.primary
                                        else
                                            colorScheme.onSurface,
                                        fontWeight = if (type == value) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (type == value) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Sélectionné",
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showTypeDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Fermer")
                    }
                }
            )
        }
    }

    // Fonction pour extraire le nom du fichier
    private fun extractFileNameFromUri(uri: Uri, context: android.content.Context): String? {
        return try {
            var fileName: String? = null
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            fileName ?: uri.lastPathSegment?.substringAfterLast("/") ?: "image.jpg"
        } catch (e: Exception) {
            "image.jpg"
        }
    }
}