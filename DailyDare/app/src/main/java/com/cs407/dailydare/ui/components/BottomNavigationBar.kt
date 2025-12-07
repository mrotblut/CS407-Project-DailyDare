package com.cs407.dailydare.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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

    val challengeScale by animateFloatAsState(
        targetValue = if (isChallengeSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "challengeScale"
    )

    val challengeBorderColor by animateColorAsState(
        targetValue = if (isChallengeSelected) buttonColor else Color.LightGray,
        label = "challengeBorder"
    )

    Column {
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFE8E8E8))
        )

        NavigationBar(
            containerColor = Color.White,
            contentColor = Color.Gray,
            modifier = Modifier
                .height(80.dp)
                .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen == "Home") Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(28.dp)
                    )
                },
                selected = currentScreen == "Home",
                onClick = onNavigateToHome,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = buttonColor.copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen == "Friends") Icons.Filled.People else Icons.Outlined.People,
                        contentDescription = "Friends",
                        modifier = Modifier.size(28.dp)
                    )
                },
                selected = currentScreen == "Friends",
                onClick = onNavigateToFriends,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = buttonColor.copy(alpha = 0.1f)
                )
            )

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .scale(challengeScale)
                            .size(56.dp)
                            .shadow(
                                elevation = if (isChallengeSelected) 8.dp else 4.dp,
                                shape = CircleShape
                            )
                            .background(
                                color = if (isChallengeSelected) buttonColor else Color.White,
                                shape = CircleShape
                            )
                            .border(2.dp, challengeBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Challenge",
                            modifier = Modifier.size(28.dp),
                            tint = if (isChallengeSelected) Color.White else buttonColor
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
                        if (currentScreen == "Notifications") Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(28.dp)
                    )
                },
                selected = currentScreen == "Notifications",
                onClick = onNavigateToNotifications,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = buttonColor.copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        if (currentScreen == "Profile") Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(28.dp)
                    )
                },
                selected = currentScreen == "Profile",
                onClick = onNavigateToProfile,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = buttonColor,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = buttonColor.copy(alpha = 0.1f)
                )
            )
        }
    }
}