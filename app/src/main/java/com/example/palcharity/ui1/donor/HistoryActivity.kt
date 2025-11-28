package com.example.palcharity.ui1.donor

import android.os.Bundle
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palcharity.utils.Donation
import com.example.palcharity.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : ComponentActivity() {

    private val firebaseHelper = FirebaseHelper()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PalestineModernTheme {
                HistoryScreen()
            }
        }
    }

    // ==================================================
    // THÈME ET STYLES
    // ==================================================

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
                tertiary = Color(0xFFD4AF37),
                onTertiary = Color(0xFF000000),
                tertiaryContainer = Color(0xFFFFF8E1),
                onTertiaryContainer = Color(0xFFB8860B),
                background = Color(0xFFFAFAFA),
                onBackground = Color(0xFF1A1A1A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1A1A1A),
                surfaceVariant = Color(0xFFF5F5F5),
                onSurfaceVariant = Color(0xFF444444),
                error = Color(0xFFD32F2F),
                onError = Color.White
            ),
            content = content
        )
    }

    // Gradients Palestine
    val PalestineGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF006633), Color(0xFF004D26))
    )

    val PalestineRedGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFB71C1C), Color(0xFF8B0000))
    )

    val PalestineGoldGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFD4AF37), Color(0xFFB8860B))
    )

    // ==================================================
    // COMPOSANTS RÉUTILISABLES
    // ==================================================

    @Composable
    fun ModernCard(
        modifier: Modifier = Modifier,
        elevation: Int = 4,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
        ) {
            content()
        }
    }

    @Composable
    fun PalestineBadge(
        text: String,
        type: String = "info",
        modifier: Modifier = Modifier
    ) {
        val backgroundColor = when (type) {
            "success" -> MaterialTheme.colorScheme.primaryContainer
            "warning" -> MaterialTheme.colorScheme.tertiaryContainer
            "error" -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        val textColor = when (type) {
            "success" -> MaterialTheme.colorScheme.onPrimaryContainer
            "warning" -> MaterialTheme.colorScheme.onTertiaryContainer
            "error" -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            modifier = modifier,
            color = backgroundColor,
            shape = CircleShape
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }

    @Composable
    fun PalestineButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        variant: String = "primary",
        content: @Composable () -> Unit
    ) {
        val containerColor = when (variant) {
            "primary" -> MaterialTheme.colorScheme.primary
            "secondary" -> MaterialTheme.colorScheme.secondary
            "tertiary" -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }

        val contentColor = when (variant) {
            "primary" -> MaterialTheme.colorScheme.onPrimary
            "secondary" -> MaterialTheme.colorScheme.onSecondary
            "tertiary" -> MaterialTheme.colorScheme.onTertiary
            else -> MaterialTheme.colorScheme.onPrimary
        }

        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            content()
        }
    }

    // ==================================================
    // COMPOSANTS DU DASHBOARD
    // ==================================================

    @Composable
    fun DashboardStats(stats: Map<String, Int>) {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // En-tête du dashboard
                DashboardHeader(stats = stats)

                Spacer(modifier = Modifier.height(24.dp))

                // Carte principale - Total des contributions
                TotalContributionCard(totalAmount = stats["totalAmount"] ?: 0)

                Spacer(modifier = Modifier.height(20.dp))

                // Section des types de dons
                DonationTypesSection(stats = stats)

                Spacer(modifier = Modifier.height(20.dp))

                // Section résumé
                SummarySection(stats = stats)
            }
        }
    }

    @Composable
    private fun DashboardHeader(stats: Map<String, Int>) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "📊 Tableau de Bord",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Vos contributions en un coup d'œil",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            PalestineBadge(
                text = "${stats["donationCount"]} dons",
                type = "success"
            )
        }
    }

    @Composable
    private fun TotalContributionCard(totalAmount: Int) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PalestineGradient)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Total des Contributions",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "$totalAmount DT",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = "Total",
                            tint = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DonationTypesSection(stats: Map<String, Int>) {
        Column {
            Text(
                "🎯 Types de Contributions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DonationTypeCard(
                    title = "Argent",
                    count = stats["moneyDonations"] ?: 0,
                    amount = stats["totalAmount"] ?: 0,
                    icon = Icons.Default.AccountBalance,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f),
                    isMoney = true
                )

                DonationTypeCard(
                    title = "Nourriture",
                    count = stats["foodDonations"] ?: 0,
                    amount = 0,
                    icon = Icons.Default.Restaurant,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f),
                    isMoney = false
                )

                DonationTypeCard(
                    title = "Vêtements",
                    count = stats["clothesDonations"] ?: 0,
                    amount = 0,
                    icon = Icons.Default.ShoppingBag,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    isMoney = false
                )
            }
        }
    }

    @Composable
    private fun DonationTypeCard(
        title: String,
        count: Int,
        amount: Int,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        color: Color,
        modifier: Modifier = Modifier,
        isMoney: Boolean = false
    ) {
        ModernCard(
            modifier = modifier,
            elevation = 2
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "$count",
                    color = color,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                if (isMoney && amount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$amount DT",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    private fun SummarySection(stats: Map<String, Int>) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Projets Soutenus",
                value = "${stats["uniqueProjects"]}",
                icon = Icons.Default.Flag,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Total Dons",
                value = "${stats["donationCount"]}",
                icon = Icons.Default.Favorite,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun SummaryCard(
        title: String,
        value: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        color: Color,
        modifier: Modifier = Modifier
    ) {
        ModernCard(
            modifier = modifier,
            elevation = 2
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

    // ==================================================
    // COMPOSANTS DE L'HISTORIQUE
    // ==================================================

    @Composable
    fun DonationHistoryItem(donation: Donation) {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = 2
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // En-tête avec projet et type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            donation.projectTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            donation.associationName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }

                    PalestineBadge(
                        text = getDonationTypeLabel(donation.typeDonation),
                        type = getDonationType(donation.typeDonation)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Détails du don
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            formatTimestamp(donation.timestamp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (donation.typeDonation == "money") {
                        Text(
                            "${donation.amount} DT",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "Don en nature",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun EmptyHistoryState() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Aucun historique",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Aucun don encore",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Vos contributions apparaîtront ici une fois que vous aurez soutenu un projet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                PalestineButton(
                    onClick = { finish() },
                    modifier = Modifier.width(200.dp),
                    variant = "primary"
                ) {
                    Text(
                        "Explorer les projets",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // ==================================================
    // ÉCRAN PRINCIPAL
    // ==================================================

    @Composable
    fun HistoryScreen() {
        var donations by remember { mutableStateOf<List<Donation>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        val currentUser = auth.currentUser
        val coroutineScope = rememberCoroutineScope()

        // Calcul des statistiques
        val stats = remember(donations) {
            calculateStats(donations)
        }

        // Chargement des données
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val allDonations = firebaseHelper.getDonorDonations(currentUser?.uid ?: "")
                    donations = allDonations.sortedByDescending { it.timestamp }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }

        // Interface utilisateur
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(PalestineGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "📊 Mon Historique",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Vos contributions pour Gaza 🇵🇸",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        ) { paddingValues ->
            when {
                isLoading -> LoadingState()
                donations.isEmpty() -> EmptyHistoryState()
                else -> HistoryContent(donations, stats, paddingValues)
            }
        }
    }

    @Composable
    private fun HistoryContent(
        donations: List<Donation>,
        stats: Map<String, Int>,
        paddingValues: PaddingValues
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardStats(stats = stats)
            }

            item {
                Text(
                    "📋 Historique des Contributions",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            items(donations) { donation ->
                DonationHistoryItem(donation = donation)
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    @Composable
    private fun LoadingState() {
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
                    "Chargement de votre historique...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // ==================================================
    // FONCTIONS UTILITAIRES
    // ==================================================

    private fun calculateStats(donations: List<Donation>): Map<String, Int> {
        val totalAmount = donations.sumOf { it.amount }
        val donationCount = donations.size
        val moneyDonations = donations.count { it.typeDonation == "money" }
        val foodDonations = donations.count { it.typeDonation == "food" }
        val clothesDonations = donations.count { it.typeDonation == "clothes" }
        val uniqueProjects = donations.map { it.projectTitle }.distinct().size

        return mapOf(
            "totalAmount" to totalAmount,
            "donationCount" to donationCount,
            "moneyDonations" to moneyDonations,
            "foodDonations" to foodDonations,
            "clothesDonations" to clothesDonations,
            "uniqueProjects" to uniqueProjects
        )
    }

    private fun getDonationTypeLabel(type: String): String {
        return when (type) {
            "money" -> "💰 Argent"
            "food" -> "🍎 Nourriture"
            "clothes" -> "👕 Vêtements"
            else -> type
        }
    }

    private fun getDonationType(type: String): String {
        return when (type) {
            "money" -> "success"
            "food" -> "warning"
            "clothes" -> "info"
            else -> "info"
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy 'à' HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }
}