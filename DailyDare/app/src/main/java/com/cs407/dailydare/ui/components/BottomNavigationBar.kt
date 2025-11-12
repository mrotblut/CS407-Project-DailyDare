package com.cs407.dailydare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.cs407.dailydare.R

@Composable
fun BottomNavigationBar(
    onNavigateToHome: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    currentScreen: String
) {
    val buttonColor = colorResource(id = R.color.button_primary)
    val isChallengeSelected = currentScreen == "Challenge"

    Column {
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color.LightGray)
        )

        NavigationBar(
            containerColor = Color.White,
            contentColor = Color.Gray,
            modifier = Modifier.height(108.dp)
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = currentScreen == "Home",
                onClick = onNavigateToHome,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.People,
                        contentDescription = "Friends",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = currentScreen == "Friends",
                onClick = onNavigateToFriends,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
            val isSelected = currentScreen == "Challenge"


            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = if (isSelected) buttonColor.copy(alpha = 0.15f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(2.dp, Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Challenge",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                selected = isChallengeSelected,
                onClick = onNavigateToChallenge,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = currentScreen == "Notifications",
                onClick = onNavigateToNotifications,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp)
                    )
                },
                selected = currentScreen == "Profile",
                onClick = onNavigateToProfile,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}