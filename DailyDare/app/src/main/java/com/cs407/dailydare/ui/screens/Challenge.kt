package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.dailydare.R
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat

@Composable
fun ChallengeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigationToProfile: () -> Unit,
    onNavigateToPost: () -> Unit = {},
    challenge: Challenge,
) {
    val challengeTitle = challenge.title
    val challengeDescription = challenge.description
    val challengeImageRes = challenge.imageRes
    val backgroundColor = colorResource(id = R.color.app_background)
    val buttonColor = colorResource(id = R.color.button_primary)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToFriends = onNavigateToFriends,
                onNavigateToChallenge = onNavigateToChallenge,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigationToProfile,
                currentScreen = "Challenge"
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Daily Dare",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Today's Challenge",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Challenge Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Challenge Image
                    Image(
                        painter = painterResource(id = challengeImageRes),
                        contentDescription = "Challenge Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )

                    // "Today's Dare" label
                    Text(
                        text = "Today's Dare",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Challenge Title
                    Text(
                        text = challengeTitle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = buttonColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Challenge Description
                    Text(
                        text = challengeDescription,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Submit Proof Button
                    Button(
                        onClick = onNavigateToPost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Proof",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChallengeScreenPreview() {
    val format = SimpleDateFormat("yyyy-MM-dd")
    ChallengeScreen(
        onNavigateToHome = {},
        onNavigateToChallenge = {},
        onNavigateToFriends = {},
        onNavigateToNotifications = {},
        onNavigationToProfile = {},
        onNavigateToPost = {},
        challenge = Challenge(4, "Try a new hobby for 1 hour",
            format.parse("2025-11-15")!!, R.drawable.wireframe,"Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?")
    )
}
