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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.getUserData
import com.cs407.dailydare.data.postPost
import com.cs407.dailydare.data.updateUserData
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
import java.text.SimpleDateFormat

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
        getUserData("SAMPLEUSER",{user -> userViewModel.setUser(user)})
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
                userState = userState
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
                challenge = userState.currentChallenges
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
                onNavigateToNotifications = { navController.navigate("Notifications") }
            )
        }
        composable("Post") {
            PostScreen(
                onNavigateToHome = { navController.navigate("Feed") },
                onNavigateToFriends = { navController.navigate("Friends") },
                onNavigateToChallenge = { navController.navigate("Challenge") },
                onNavigationToProfile = { navController.navigate("Profile") },
                onNavigateToNotifications = { navController.navigate("Notifications") },
                challenge = userState.currentChallenges,
                onPost = { caption, imageUrl ->
                    // TODO: Save to Firestore
                    // Create firestoreUserChallenges object with:
                    // - postPicture = imageUrl (the ImgBB URL string)
                    // - other challenge fields
                    // Then save to Firestore
                    postPost(userState,userState.currentChallenges,imageUrl,caption, {userState -> userViewModel.setUser(userState)})
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
                    onEditProfile = {},
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
                    onNavigateToSignUp = { navController.navigate("SignUp") },
                    onNavigateToHome = { navController.navigate("Feed") }

                )
            }
            composable("SignUp") {
                SignUpScreen(
                    onNavigateToSignIn = { navController.navigate("SignIn") }
                )
            }
        }
    }
