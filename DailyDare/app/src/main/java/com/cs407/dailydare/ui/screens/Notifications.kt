package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.dailydare.R
import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.data.Post
import com.cs407.dailydare.ui.components.BottomNavigationBar
import com.cs407.dailydare.ui.components.TopNavigationBar

data class Notification(
    val id: Int,
    val message: String,
    val time: String,
    val icon: Int
)
@Composable
fun NotificationsScreen(onNavigateToHome: () -> Unit, onNavigateToChallenge:() -> Unit, onNavigateToFriends:() -> Unit, onNavigateToNotifications:() -> Unit, onNavigationToProfile:() -> Unit, userViewModel: UserViewModel){

    // Sample data for the preview and initial state
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    LaunchedEffect(Unit) {
        userViewModel.getNotifications { notif -> notifications = notif }
    }


    Scaffold(
        topBar = {
            TopNavigationBar(title = "Notifications")
        },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToFriends = onNavigateToFriends,
                onNavigateToChallenge = onNavigateToChallenge,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigationToProfile,
                currentScreen = "Notifications"
            )
        },
        containerColor = colorResource(id = R.color.app_background)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.app_background))
                .padding(horizontal = 16.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications to display",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(notifications) { notification ->
                    NotificationItem(
                        message = notification.message,
                        time = notification.time,
                        profilePicture = painterResource(id = notification.icon)
                    )
                    HorizontalDivider(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun NotificationItem(
    message: String,
    time: String,
    profilePicture: Painter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = profilePicture ?: painterResource(id = R.drawable.default_user),
            contentDescription = "Notification Icon",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Message and time
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

