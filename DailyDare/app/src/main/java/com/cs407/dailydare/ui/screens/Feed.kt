package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
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
import com.cs407.dailydare.data.Post
import com.cs407.dailydare.ui.components.BottomNavigationBar
import com.cs407.dailydare.ui.components.TopNavigationBar
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.getFeedPosts
import com.cs407.dailydare.data.firestoreUserChallenges
import com.cs407.dailydare.data.getUserData
import com.cs407.dailydare.utils.PhotoUploadManager.fetchPainter
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigationToProfile: () -> Unit,
    userState: UserState = UserState()
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshPosts() {
        coroutineScope.launch {
            isRefreshing = true
            getFeedPosts(userState, {newList -> posts = newList})
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        getFeedPosts(userState, {newList -> posts = newList})
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopNavigationBar(title = "Home")
        },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToFriends = onNavigateToFriends,
                onNavigateToChallenge = onNavigateToChallenge,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigationToProfile,
                currentScreen = "Home"
            )
        },
        containerColor = colorResource(id = R.color.app_background)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorResource(id = R.color.button_primary)
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        refreshPosts()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            CurrentChallengeCard(
                                challengeTitle = userState.currentChallenges.title,
                                onChallengeClick = onNavigateToChallenge
                            )
                        }

                        if (posts.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No posts yet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Add friends to see their posts!",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            items(posts) { post ->
                                PostCard(
                                    post = post,
                                    onLikeClick = { postId ->
                                        posts = posts.map {
                                            if (it.postId == postId) {
                                                it.copy(
                                                    isLiked = !it.isLiked,
                                                    likes = if (it.isLiked) it.likes - 1 else it.likes + 1
                                                )
                                            } else it
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentChallengeCard(
    challengeTitle: String,
    onChallengeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onChallengeClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.button_primary)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODAY'S CHALLENGE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = challengeTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap to complete the challenge!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    onLikeClick: (String) -> Unit
) {
    val defaultUser = painterResource(id = R.drawable.default_user)
    var userImage: Painter = defaultUser
    if (post.profilePicture.isNotEmpty()) {
        fetchPainter(post.profilePicture, {img -> userImage = img?:defaultUser})
    }

    var postImg: Painter? = null
    if (post.profilePicture.isNotEmpty()) {
        fetchPainter(post.contentUri, {img -> postImg = img})
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = userImage,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "@${post.userHandle}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        colorResource(id = R.color.button_primary).copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "Challenge: ${post.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.button_primary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (postImg != null) {
                Image(
                    painter = postImg!!,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (post.caption.isNotEmpty()) {
                Text(
                    text = post.caption,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = Color.LightGray, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { onLikeClick(post.postId) }
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = post.likes.toString(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}




@Preview
@Composable
fun feedPreview(){
    val format = SimpleDateFormat("yyyy-MM-dd")
    val sampleCompletedChallenges = listOf(
        Challenge(1, "Do 10 jumping jacks in a funny place", format.parse("2025-10-31")!!, R.drawable.wireframe,""),
        Challenge(2, "Recreate a famous movie scene", format.parse("2025-10-30")!!, R.drawable.wireframe,""),
        Challenge(3, "Build a pillow fort", format.parse("2025-10-29")!!, R.drawable.wireframe,"")
    )

    val sampleCurrentChallenge = Challenge(4, "Try a new hobby for 1 hour",
        format.parse("2025-11-15")!!, R.drawable.wireframe,"Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?")
    val user = UserState(
        uid = "SAMPLEUSER",
        userName = "IShowSpeed",
        userHandle = "@IShowSpeed",
        streakCount = 7,
        completedCount = sampleCompletedChallenges.size,
        friendsCount = 12,
        completedChallenges = sampleCompletedChallenges,
        currentChallenges = sampleCurrentChallenge,
        profilePicture = painterResource(id = R.drawable.default_user)
    )
    FeedScreen({},{},{},{},{},user)
}
