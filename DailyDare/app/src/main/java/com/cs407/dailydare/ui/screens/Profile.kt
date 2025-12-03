package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import coil.compose.rememberAsyncImagePainter
import com.cs407.dailydare.R
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
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
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Completed") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val challengesToShow = if (selectedTab == "Completed") {
        userState.completedChallenges
    } else {
        listOf(userState.currentChallenge)
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("No")
                }
            }
        )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spacer to provide room for the logout button at the top
                item { Spacer(modifier = Modifier.height(60.dp)) }

                // Profile Header
                item {
                    ProfileHeader(
                        userName = userState.userName,
                        userHandle = userState.userHandle,
                        profilePicture = if(userState.profilePicUrl.isEmpty()){painterResource(R.drawable.default_user)}else{rememberAsyncImagePainter(model = userState.profilePicUrl)},
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
                        Row {
                            TabButton("Completed", selectedTab == "Completed") {
                                selectedTab = "Completed"
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TabButton("Current", selectedTab == "Current") {
                                selectedTab = "Current"
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (challengesToShow.isEmpty()) {
                    item {
                        Text("No challenges to display", color = Color.Gray)
                    }
                } else {
                    items(challengesToShow) { challenge ->
                        ChallengeCard(
                            imageRes = challenge.imageLink,
                            title = challenge.title,
                            date = challenge.date
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            // Logout button
            FloatingActionButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(52.dp),
                shape = CircleShape,
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Log Out"
                )
            }
        }
    }
}


@Composable
fun ProfileHeader(
    userName: String,
    userHandle: String,
    profilePicture: Painter,
    streakCount: Int,
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
fun ChallengeCard(imageRes: String, title: String, date: Date) {
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
                painter = rememberAsyncImagePainter(model = imageRes),
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
        Challenge(1, "Do 10 jumping jacks in a funny place", format.parse("2025-10-31")!!, "https://i.ibb.co/Hpn6Q27v/jump.jpg",""),
        Challenge(2, "Recreate a famous movie scene", format.parse("2025-10-30")!!, "https://i.ibb.co/Hpn6Q27v/jump.jpg",""),
        Challenge(3, "Build a pillow fort", format.parse("2025-10-29")!!, "https://i.ibb.co/Hpn6Q27v/jump.jpg","")
    )

    val sampleCurrentChallenge = Challenge(4, "Try a new hobby for 1 hour",
        format.parse("2025-11-15")!!, "https://centralca.cdn-anvilcms.net/media/images/2021/11/24/images/Ideal_Hobbies_pix_11-24-21.max-2400x1350.jpg","Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?")


    val sampleUser = UserState(
        userName = "IShowSpeed",
        userHandle = "@IShowSpeed",
        streakCount = 7,
        completedCount = sampleCompletedChallenges.size,
        friendsCount = 12,
        completedChallenges = sampleCompletedChallenges,
        currentChallenge = sampleCurrentChallenge,
    )

    ProfileScreen(
        userState = sampleUser,
        onNavigateToHome = {},
        onNavigateToChallenge = {},
        onNavigateToFriends = {},
        onNavigateToNotifications = {},
        onNavigateToProfile = {},
        onEditProfile = {},
        onLogout = {}
    )
}
