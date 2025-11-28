package com.example.palcharity.ui1.donor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palcharity.utils.Donor
import com.example.palcharity.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DonationActivity : ComponentActivity() {

    private val firebaseHelper = FirebaseHelper()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                    background = Color(0xFFFAFAFA),
                    onBackground = Color(0xFF1A1A1A),
                    surface = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF1A1A1A),
                )
            ) {
                ProfileScreen()
            }
        }
    }

    // Gradients Palestine
    val PalestineGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF006633), Color(0xFF004D26))
    )

    // ==================================================
    // COMPOSANTS RÉUTILISABLES
    // ==================================================

    @Composable
    fun ModernCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            content()
        }
    }

    @Composable
    fun ModernTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        placeholder: String = "",
        leadingIcon: @Composable (() -> Unit)? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        enabled: Boolean = true,
        modifier: Modifier = Modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            keyboardOptions = keyboardOptions,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ==================================================
    // COMPOSANTS DU PROFIL
    // ==================================================

    @Composable
    fun ProfileHeader(
        donor: Donor,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(PalestineGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "${donor.firstName} ${donor.lastName}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    donor.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge Donateur
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        "🤲 Donateur",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun ProfileForm(
        donor: Donor,
        onFirstNameChange: (String) -> Unit,
        onLastNameChange: (String) -> Unit,
        onEmailChange: (String) -> Unit,
        isEditing: Boolean,
        modifier: Modifier = Modifier
    ) {
        ModernCard(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "📝 Informations Personnelles",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Prénom
                ModernTextField(
                    value = donor.firstName,
                    onValueChange = onFirstNameChange,
                    label = "Prénom",
                    placeholder = "Votre prénom",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Prénom",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    enabled = isEditing,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Nom
                ModernTextField(
                    value = donor.lastName,
                    onValueChange = onLastNameChange,
                    label = "Nom",
                    placeholder = "Votre nom",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Nom",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    enabled = isEditing,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Email
                ModernTextField(
                    value = donor.email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "votre@email.com",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = isEditing,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }

    @Composable
    fun StatisticsSection(
        donationCount: Int,
        totalAmount: Int,
        modifier: Modifier = Modifier
    ) {
        ModernCard(
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "📊 Mes Statistiques",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total des dons
                    StatCard(
                        title = "Total Donné",
                        value = "$totalAmount DT",
                        icon = Icons.Default.AttachMoney,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    // Nombre de dons
                    StatCard(
                        title = "Dons Effectués",
                        value = donationCount.toString(),
                        icon = Icons.Default.Favorite,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    fun StatCard(
        title: String,
        value: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        color: Color,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun ActionButtons(
        onSave: () -> Unit,
        onCancel: () -> Unit,
        isEditing: Boolean,
        isSaving: Boolean,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isEditing) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Annuler")
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isEditing) "Enregistrer" else "Modifier le profil")
                }
            }
        }
    }

    @Composable
    fun LoadingProfile() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Chargement du profil...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    fun SuccessMessage() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Succès",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Profil mis à jour avec succès !",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // ==================================================
    // ÉCRAN PRINCIPAL DU PROFIL
    // ==================================================

    @Composable
    fun ProfileScreen() {
        var donor by remember { mutableStateOf<Donor?>(null) }
        var editedDonor by remember { mutableStateOf<Donor?>(null) }
        var isEditing by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }
        var donationStats by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var showSuccessMessage by remember { mutableStateOf(false) }

        val currentUser = auth.currentUser
        val coroutineScope = rememberCoroutineScope()

        // Chargement des données du profil
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    currentUser?.uid?.let { uid ->
                        val donorData = firebaseHelper.getDonorById(uid)
                        donor = donorData
                        editedDonor = donorData.copy()

                        // Chargement des statistiques
                        val stats = firebaseHelper.getDonorStats(uid)
                        donationStats = stats
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Affichage du message de succès
        if (showSuccessMessage) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header personnalisé
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(PalestineGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Mon Profil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            when {
                donor == null -> LoadingProfile()
                else -> {
                    val currentDonor = donor!!
                    val currentEditedDonor = editedDonor!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ProfileHeader(donor = if (isEditing) currentEditedDonor else currentDonor)
                        }

                        if (showSuccessMessage) {
                            item {
                                SuccessMessage()
                            }
                        }

                        item {
                            ProfileForm(
                                donor = if (isEditing) currentEditedDonor else currentDonor,
                                onFirstNameChange = {
                                    if (isEditing) editedDonor = currentEditedDonor.copy(firstName = it)
                                },
                                onLastNameChange = {
                                    if (isEditing) editedDonor = currentEditedDonor.copy(lastName = it)
                                },
                                onEmailChange = {
                                    if (isEditing) editedDonor = currentEditedDonor.copy(email = it)
                                },
                                isEditing = isEditing,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item {
                            donationStats?.let { (count, total) ->
                                StatisticsSection(
                                    donationCount = count,
                                    totalAmount = total,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        item {
                            ActionButtons(
                                onSave = {
                                    if (isEditing) {
                                        coroutineScope.launch {
                                            isSaving = true
                                            try {
                                                currentUser?.uid?.let { uid ->
                                                    firebaseHelper.updateDonorProfile(
                                                        uid = uid,
                                                        firstName = currentEditedDonor.firstName,
                                                        lastName = currentEditedDonor.lastName
                                                    )
                                                    donor = currentEditedDonor
                                                    isEditing = false
                                                    showSuccessMessage = true
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                isSaving = false
                                            }
                                        }
                                    } else {
                                        isEditing = true
                                    }
                                },
                                onCancel = {
                                    editedDonor = currentDonor.copy()
                                    isEditing = false
                                },
                                isEditing = isEditing,
                                isSaving = isSaving,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}