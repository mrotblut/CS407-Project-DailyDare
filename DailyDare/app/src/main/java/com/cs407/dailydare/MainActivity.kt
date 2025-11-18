package com.cs407.dailydare

import androidx.compose.ui.res.painterResource
import com.cs407.dailydare.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.ui.screens.ChallengeScreen
import com.cs407.dailydare.ui.screens.FeedScreen
import com.cs407.dailydare.ui.screens.FriendsScreen
import com.cs407.dailydare.ui.screens.NotificationsScreen
import com.cs407.dailydare.ui.screens.PostScreen
import com.cs407.dailydare.ui.screens.ProfileScreen
import com.cs407.dailydare.ui.screens.SignInScreen
import com.cs407.dailydare.ui.screens.SignUpScreen
import com.cs407.dailydare.ui.theme.DailyDareTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyDareTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // get viewmodel and collect userState
    val userViewModel: UserViewModel = viewModel()
    val userState by userViewModel.userState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "SignIn" //Replace with below when implemented
        // TODO If signed in go to feed else Sign in; add when local storage is added to check if signined
        //startDestination = if (){ 
        //     "SignIn"
        //}else{
        //     "Feed" 
        //}

    ) {
        composable("Feed") {
            FeedScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") }
            )
        }
        composable("Challenge") {
            ChallengeScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") },
                onNavigateToPost = { navController.navigate("Post") }
            )
        }
        composable("Friends") {
            FriendsScreen (
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") }
            )
        }
        composable("Notifications") {
            NotificationsScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") }
            )
        }
        composable("Post") {
            PostScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") }
            )
        }
        composable("Profile") {
            ProfileScreen(
                userState = userState,
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigateToNotifications = { navController.navigate("Notifications") },
                onNavigateToProfile = { navController.navigate("Profile") },
                onEditProfile = {}, // TODO, edit profile
                onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("SignIn") {
                        // reset nav graph
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("SignIn") {
            SignInScreen(
                onNavigateToSignUp = {navController.navigate("SignUp")},
                onNavigateToHome = {navController.navigate("Feed")}

            )
        }
        composable("SignUp") {
            SignUpScreen(
                onNavigateToSignIn = {navController.navigate("SignIn")}
            )
        }
    }
}