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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cs407.dailydare.R
import com.cs407.dailydare.ui.components.BottomNavigationBar
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.getFeedPosts
import com.cs407.dailydare.data.firestoreUserChallenges
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FeedPost(
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val profilePicture: String = "",
    val challengeTitle: String = "",
    val challengeDescription: String = "",
    val postImageUrl: String = "",
    val caption: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val timestamp: Long = 0L,
    var isLiked: Boolean = false
)

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
    var posts by remember { mutableStateOf<List<FeedPost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun refreshPosts() {
        coroutineScope.launch {
            isRefreshing = true
            posts = loadFeedPosts(userState)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        posts = loadFeedPosts(userState)
        isLoading = false
    }

    Scaffold(
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
                                FeedPostCard(
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
                                    },
                                    onCommentClick = { },
                                    onShareClick = { }
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
fun FeedPostCard(
    post: FeedPost,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit
) {
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
                    painter = if (post.profilePicture.isNotEmpty()) {
                        rememberAsyncImagePainter(post.profilePicture)
                    } else {
                        painterResource(id = R.drawable.default_user)
                    },
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
                    text = "Challenge: ${post.challengeTitle}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.button_primary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.postImageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(post.postImageUrl),
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { onCommentClick(post.postId) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Comment,
                            contentDescription = "Comment",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = post.comments.toString(),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onShareClick(post.postId) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

suspend fun loadFeedPosts(userState: UserState): List<FeedPost> {
    return try {
        val db = Firebase.firestore
        val posts = mutableListOf<FeedPost>()

        val firestorePosts = getFeedPosts(userState.friendsUID, userState.uid)

        for (postData in firestorePosts) {
            val userDoc = db.collection("users").document(postData.UserUID).get().await()
            val userName = userDoc.getString("userName") ?: "Unknown User"
            val userHandle = userDoc.getString("userHandle") ?: "unknown"
            val profilePicture = userDoc.getString("profilePicture") ?: ""

            posts.add(
                FeedPost(
                    postId = "",
                    userId = postData.UserUID,
                    userName = userName,
                    userHandle = userHandle,
                    profilePicture = profilePicture,
                    challengeTitle = postData.title,
                    challengeDescription = postData.description,
                    postImageUrl = postData.postPicture ?: "",
                    caption = postData.description,
                    likes = (0..50).random(),
                    comments = (0..20).random(),
                    timestamp = postData.date.time,
                    isLiked = false
                )
            )
        }

        posts.sortedByDescending { it.timestamp }
    } catch (e: Exception) {
        emptyList()
    }
}