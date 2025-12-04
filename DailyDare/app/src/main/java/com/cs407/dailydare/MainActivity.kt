package com.cs407.dailydare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.ui.screens.ChallengeScreen
import com.cs407.dailydare.ui.screens.EditProfileScreen
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
fun AppNavigation(userViewModel: UserViewModel = viewModel()) {
    val navController = rememberNavController()

    val userState by userViewModel.userState.collectAsState()

    /////// Sample user //////////////
    LaunchedEffect(Unit){
    //    userViewModel.getUserData("SAMPLEUSER", {navController.navigate("Feed")})
        val user = Firebase.auth.currentUser
        if (user != null){
            navController.navigate("Feed")
            userViewModel.getUserData(user.uid,{navController.navigate("Feed")})
        }
    }
    /////////////////////////////////

    LaunchedEffect(userState.uid) {
        if (userState.uid.isEmpty() || userState.userName.isEmpty()) {
            navController.navigate("SignIn") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate("Feed") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "SignIn" //Replace with below when implemented

    ) {
        composable("Feed") {
            FeedScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") },
                userState = userState,
                userViewModel = userViewModel
            )
        }
        composable("Challenge") {
            ChallengeScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") },
                onNavigateToPost = { navController.navigate("Post") },
                challenge = userState.currentChallenge,
                userState = userState
            )
        }
        composable("Friends") {
            FriendsScreen(
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
                onNavigateToNotifications = { navController.navigate("Notifications") },
                userViewModel = userViewModel
            )
        }
        composable("Post") {
            PostScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") },
                challenge = userState.currentChallenge,
                onPost = { caption, imageUrl ->
                    userViewModel.postPost(imageUrl,caption)
                    println("Caption: $caption, Image URL: $imageUrl")
                }
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
                onEditProfile = { navController.navigate("EditProfile") },
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
        composable("EditProfile") {
            EditProfileScreen (
                userState = userState,
                onNavigateToProfile = { navController.navigate("Profile") },
                userViewModel = userViewModel
            )
        }
        composable("SignIn") {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate("SignUp") },
                onNavigateToHome = { navController.navigate("Feed") },
                userViewModel = userViewModel

            )
        }
        composable("SignUp") {
            SignUpScreen(
                onNavigateToSignIn = { navController.navigate("SignIn") },
                onNavigateToProfile = { navController.navigate("Profile") },
                userViewModel = userViewModel
            )
        }
    }
}
