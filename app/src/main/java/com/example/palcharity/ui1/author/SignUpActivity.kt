package com.example.palcharity.ui1.auth

import com.example.palcharity.ui1.donor.DonorHomeActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palcharity.MainActivity
import com.example.palcharity.R
import com.example.palcharity.ui1.association.AssociationDashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

class SignUpActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Utilisez directement MaterialTheme ou votre thème existant
            MaterialTheme {
                SignUpScreen(
                    onSignUp = { email, password, role, firstName, lastName, associationName ->
                        signUpUser(email, password, role, firstName, lastName, associationName)
                    },
                    onBack = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    private fun signUpUser(
        email: String,
        password: String,
        role: String,
        firstName: String,
        lastName: String,
        associationName: String
    ) {
        if (email.isBlank() || password.isBlank() || (role == "Donor" && (firstName.isBlank() || lastName.isBlank()))
            || (role == "Association" && associationName.isBlank())
        ) {
            Toast.makeText(this, "Veuillez remplir tous les champs requis", Toast.LENGTH_SHORT).show()
            return
        }

        // Crée l'utilisateur avec FirebaseAuth (mot de passe géré par Firebase, ne pas stocker en clair)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userRef = if (role == "Donor") {
                        database.child("donors").child(uid)
                    } else {
                        database.child("associations").child(uid)
                    }

                    // Construire la map sans inclure le mot de passe
                    val userMap = if (role == "Donor") {
                        mapOf(
                            "uid" to uid,
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "email" to email
                        )
                    } else {
                        mapOf(
                            "uid" to uid,
                            "associationName" to associationName,
                            "email" to email
                        )
                    }

                    userRef.setValue(userMap).addOnSuccessListener {
                        Toast.makeText(this, "Inscription réussie ✅", Toast.LENGTH_SHORT).show()
                        // Redirection selon rôle
                        if (role == "Donor") {
                            startActivity(Intent(this, DonorHomeActivity::class.java))
                        } else {
                            startActivity(Intent(this, AssociationDashboardActivity::class.java))
                        }
                        finish()
                    }.addOnFailureListener { ex ->
                        Toast.makeText(this, "Erreur en écrivant en DB : ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Erreur d'inscription : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUp: (email: String, password: String, role: String, firstName: String, lastName: String, associationName: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Donor") }
    val roles = listOf("Donor", "Association")
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var associationName by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Couleurs du thème palestinien
    val palestinianRed = Color(0xFFCE1126)
    val palestinianGreen = Color(0xFF006400)
    val palestinianBlack = Color(0xFF000000)
    val backgroundColor = Color(0xFFF8F9FA)

    // Gestion du loading state
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(2000)
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        Color(0xFFE8F4F8)
                    )
                )
            )
    ) {
        // Header avec bouton retour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palestinianRed.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = palestinianRed
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Carte d'inscription
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = true
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo et titre
                    Image(
                        painter = painterResource(id = R.drawable.flag_palestine),
                        contentDescription = "Drapeau Palestine",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Créer un compte",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = palestinianBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rejoignez la communauté PalCharity",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Sélecteur de rôle
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Type de compte",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = palestinianBlack,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = role,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Rôle",
                                        tint = palestinianGreen
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                roles.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            role = selectionOption
                                            expanded = false
                                        },
                                        text = {
                                            Text(
                                                text = selectionOption,
                                                color = if (selectionOption == role) palestinianRed else palestinianBlack
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Champs dynamiques selon rôle
                    if (role == "Donor") {
                        // Champs pour Donor
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Prénom") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Prénom",
                                    tint = palestinianGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Nom") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Nom",
                                    tint = palestinianGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        // Champ pour Association
                        OutlinedTextField(
                            value = associationName,
                            onValueChange = { associationName = it },
                            label = { Text("Nom de l'association") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Nom association",
                                    tint = palestinianGreen
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Champs communs
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = palestinianGreen
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Mot de passe",
                                tint = palestinianGreen
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Bouton d'inscription
                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank() &&
                                ((role == "Donor" && firstName.isNotBlank() && lastName.isNotBlank()) ||
                                        (role == "Association" && associationName.isNotBlank()))) {
                                isLoading = true
                                onSignUp(email.trim(), password.trim(), role, firstName.trim(), lastName.trim(), associationName.trim())
                            } else {
                                Toast.makeText(context, "Veuillez remplir tous les champs requis", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palestinianRed
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Créer le compte",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lien vers connexion
                    Text(
                        text = "Déjà un compte ? Se connecter",
                        color = palestinianGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            val intent = Intent(context, SignInActivity::class.java)
                            context.startActivity(intent)
                            if (context is ComponentActivity) {
                                (context as ComponentActivity).finish()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "Rejoignez notre mission pour la Palestine",
                fontSize = 12.sp,
                color = Color(0xFF718096),
                textAlign = TextAlign.Center
            )
        }
    }
}