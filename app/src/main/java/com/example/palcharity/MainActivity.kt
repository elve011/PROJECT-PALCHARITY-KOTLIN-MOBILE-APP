package com.example.palcharity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palcharity.ui1.auth.SignInActivity
import com.example.palcharity.ui1.auth.SignUpActivity
import com.example.palcharity.ui.theme.PalCharityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PalCharityTheme {
                MainScreen(
                    onSignInClick = {
                        startActivity(Intent(this, SignInActivity::class.java))
                    },
                    onSignUpClick = {
                        startActivity(Intent(this, SignUpActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onSignInClick: () -> Unit, onSignUpClick: () -> Unit) {
    // Couleurs du thème palestinien
    val palestinianBlack = Color(0xFF000000)
    val palestinianWhite = Color(0xFFFFFFFF)
    val palestinianGreen = Color(0xFF006400)
    val palestinianRed = Color(0xFFCE1126)
    val backgroundColor = Color(0xFFF8F9FA)
    val cardColor = Color(0xFFFFFFFF)
    val textColor = Color(0xFF2D3748)

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
            ),
        contentAlignment = Alignment.Center
    ) {
        // Éléments décoratifs
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Carte principale
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = true
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(32.dp)
                ) {
                    // Bannière avec drapeau
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        palestinianBlack,
                                        palestinianGreen,
                                        palestinianWhite,
                                        palestinianRed
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.flag_palestine),
                            contentDescription = "Drapeau de la Palestine",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Titre principal
                    Text(
                        text = "PalCharity",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = palestinianGreen,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sous-titre
                    Text(
                        text = "Solidarité avec la Palestine",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = palestinianBlack,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(
                        text = "Rejoignez notre communauté pour soutenir les projets humanitaires en Palestine. Votre contribution fait la différence.",
                        fontSize = 14.sp,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Bouton principal
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palestinianRed
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Commencer maintenant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Lien d'inscription
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Nouveau ici ? ",
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Créer un compte",
                            color = palestinianGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onSignUpClick() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Ensemble pour la Palestine",
                fontSize = 12.sp,
                color = Color(0xFF718096),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Éléments décoratifs supplémentaires
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.TopEnd
        ) {
            // Pattern décoratif
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 40.dp, y = (-40).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palestinianGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.BottomStart
        ) {
            // Pattern décoratif
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-30).dp, y = 30.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palestinianRed.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
