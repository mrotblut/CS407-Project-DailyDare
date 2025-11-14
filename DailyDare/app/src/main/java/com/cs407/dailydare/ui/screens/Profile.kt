package com.cs407.dailydare.ui.screens

import android.util.Log
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
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


@Composable
fun ProfileScreen(
    // : Placeholder data to be passed in from a ViewModel and onClick actions
    userState: UserState,
    // : Navigation actions
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile:() -> Unit,
    onEditProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Completed") }
    val challengesToShow = if (selectedTab == "Completed") {
        userState.completedChallenges
    } else {
        listOf(userState.currentChallenges)
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToFriends = onNavigateToFriends,
                onNavigateToChallenge = onNavigateToChallenge,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigateToProfile,
                currentScreen = "Profile"
            )
        },
        containerColor = colorResource(id = R.color.app_background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.app_background))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                        userName = userState.userName,
                        userHandle = userState.userHandle,
                        profilePicture = userState.profilePicture,
                        onEditClick = onEditProfile,
                        streakCount = userState.streakCount
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Stats
                item {
                    StatsRow(
                        streakCount = userState.streakCount.toString(),
                        completedCount = userState.completedCount.toString(),
                        friendsCount = userState.friendsCount.toString()
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

                if (challengesToShow.isEmpty()) {
                    item {
                        Text("No challenges to display", color = Color.Gray)
                    }
                }
                else {
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
        }
    }
}


@Composable
fun ProfileHeader(
    userName: String,
    userHandle: String,
    profilePicture: Painter?,
    streakCount: Int,
    onEditClick: () -> Unit
) {
    val buttonColor = colorResource(id = R.color.button_primary)
    Box(contentAlignment = Alignment.TopCenter) {
        Image(
            painter = profilePicture ?: painterResource(id = R.drawable.default_user),
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
            painter = painterResource(id = R.drawable.flare_icon),
            contentDescription = "Streak Flame",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Text(text = streakCount.toString(), fontSize = 14.sp, color = Color.Gray)
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
fun ChallengeCard(imageRes: Int, title: String, date: Date) {
    val pattern = "EEE MMM dd yyyy"
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
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
                Text(text = simpleDateFormat.format(date), fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFF8F8FF)
@Composable
fun ProfileScreenPreview() {
    val format = SimpleDateFormat("yyyy-MM-dd")
    val sampleCompletedChallenges = listOf(
        Challenge(1, "Do 10 jumping jacks in a funny place", format.parse("2025-10-31")!!, R.drawable.wireframe,""),
        Challenge(2, "Recreate a famous movie scene", format.parse("2025-10-30")!!, R.drawable.wireframe,""),
        Challenge(3, "Build a pillow fort", format.parse("2025-10-29")!!, R.drawable.wireframe,"")
    )

    val sampleCurrentChallenge = Challenge(4, "Try a new hobby for 1 hour",
        format.parse("2025-11-15")!!, R.drawable.wireframe,"Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?")


    val sampleUser = UserState(
        userName = "IShowSpeed",
        userHandle = "@IShowSpeed",
        streakCount = 7,
        completedCount = sampleCompletedChallenges.size,
        friendsCount = 12,
        completedChallenges = sampleCompletedChallenges,
        currentChallenges = sampleCurrentChallenge,
        profilePicture = painterResource(id = R.drawable.default_user)
    )

    ProfileScreen(
        userState = sampleUser,
        onNavigateToHome = {},
        onNavigateToChallenge = {},
        onNavigateToFriends = {},
        onNavigateToNotifications = {},
        onNavigateToProfile = {},
        onEditProfile = {}
    )
}
