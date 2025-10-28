package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.dailydare.R

data class Challenge(
    val id: Int,
    val title: String,
    val date: String,
    val imageRes: Int
)

@Composable
fun ProfileScreen(
    // TODO: Placeholder data to be passed in from a ViewModel and onClick actions
    userName: String,
    userHandle: String,
    streakCount: Int,
    completedCount: Int,
    friendsCount: Int,
    profilePicture: Painter,
    streakIcon: Painter,
    completedChallenges: List<Challenge>,
    currentChallenges: List<Challenge>,

    // TODO: Navigation actions
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onEditProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Completed") }
    val challengesToShow = if (selectedTab == "Completed") completedChallenges else currentChallenges

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.app_background))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        item {
            ProfileHeader(
                userName = userName,
                userHandle = userHandle,
                profilePicture = profilePicture,
                streakIcon = streakIcon,
                onEditClick = onEditProfile
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Stats
        item {
            StatsRow(
                streakCount = streakCount.toString(),
                completedCount = completedCount.toString(),
                friendsCount = friendsCount.toString()
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // My Challenges
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "My Challenges",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Complete and current Buttons
                Row {
                    TabButton("Completed", selectedTab == "Completed") { selectedTab = "Completed" }
                    Spacer(modifier = Modifier.width(8.dp))
                    TabButton("Current", selectedTab == "Current") { selectedTab = "Current" }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Challenges List
        items(challengesToShow) { challenge ->
            ChallengeCard(
                imageRes = challenge.imageRes,
                title = challenge.title,
                date = challenge.date
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userHandle: String,
    profilePicture: Painter,
    streakIcon: Painter,
    onEditClick: () -> Unit
) {
    val buttonColor = colorResource(id = R.color.button_primary)
    Box(contentAlignment = Alignment.TopCenter) {
        Image(
            painter = profilePicture,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        // Edit profile picture button
        FloatingActionButton(
            onClick = onEditClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp, end = 4.dp)
                .size(36.dp),
            containerColor = buttonColor,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = streakIcon,
            contentDescription = "Streak Flame",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Text(text = "<Streak #>", fontSize = 14.sp, color = Color.Gray)
    }

    Text(text = userHandle, fontSize = 16.sp, color = Color.Gray)
}

@Composable
fun StatsRow(streakCount: String, completedCount: String, friendsCount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(number = streakCount, label = "Streak")
        StatCard(number = completedCount, label = "Completed")
        StatCard(number = friendsCount, label = "Friends")
    }
}

@Composable
fun StatCard(number: String, label: String) {
    Card(
        modifier = Modifier.size(width = 90.dp, height = 70.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = number, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val buttonColor = if (isSelected) colorResource(id = R.color.button_primary) else Color(0xFFE8E8E8)
    val textColor = if (isSelected) Color.White else Color.Gray

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ChallengeCard(imageRes: Int, title: String, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = date, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFF8F8FF)
@Composable
fun ProfileScreenPreview() {
    val sampleCompletedChallenges = listOf(
        Challenge(1, "Do 10 jumping jacks in a funny place", "Completed: Oct 26, 2023", R.drawable.wireframe),
        Challenge(2, "Recreate a famous movie scene", "Completed: Oct 25, 2023", R.drawable.wireframe),
        Challenge(3, "Build a pillow fort", "Completed: Oct 24, 2023", R.drawable.wireframe)
    )

    ProfileScreen(
        userName = "IShowSpeed",
        userHandle = "@IShowSpeed",
        streakCount = 1,
        completedCount = 2,
        friendsCount = 3,
        profilePicture = painterResource(id = R.drawable.wireframe),
        streakIcon = painterResource(id = R.drawable.flare_icon),
        completedChallenges = sampleCompletedChallenges,
        currentChallenges = emptyList(),
        onNavigateToHome = {},
        onNavigateToChallenge = {},
        onNavigateToFriends = {},
        onNavigateToNotifications = {},
        onEditProfile = {}
    )
}
