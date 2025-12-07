package com.cs407.dailydare.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cs407.dailydare.R
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.firestoreUser
import com.cs407.dailydare.ui.components.BottomNavigationBar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FriendItem(
    val uid: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val profilePicture: String = "",
    val streakCount: Int = 0,
    val isFriend: Boolean = false,
    val hasPendingRequest: Boolean = false
)

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromUserName: String = "",
    val fromUserHandle: String = "",
    val fromProfilePicture: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigationToProfile: () -> Unit,
    userState: UserState = UserState()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var friends by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var friendRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        friends = loadFriends(userState.uid, userState.friendsUID)
        friendRequests = loadFriendRequests(userState.uid)
        isLoading = false
    }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        coroutineScope.launch {
            isSearching = true
            searchResults = searchUsers(query, userState.uid, userState.friendsUID)
            isSearching = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToFriends = onNavigateToFriends,
                onNavigateToChallenge = onNavigateToChallenge,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigationToProfile,
                currentScreen = "Friends"
            )
        },
        containerColor = colorResource(id = R.color.app_background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.app_background))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Friends",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        performSearch(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search users...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                searchResults = emptyList()
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.button_primary),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        focusManager.clearFocus()
                        performSearch(searchQuery)
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FriendsTabButton(
                            text = "My Friends",
                            isSelected = selectedTab == 0,
                            badgeCount = friends.size,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        FriendsTabButton(
                            text = "Requests",
                            isSelected = selectedTab == 1,
                            badgeCount = friendRequests.size,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.button_primary)
                    )
                }
            } else if (searchQuery.isNotEmpty()) {
                SearchResultsList(
                    results = searchResults,
                    isSearching = isSearching,
                    onAddFriend = { user ->
                        coroutineScope.launch {
                            sendFriendRequest(userState.uid, user.uid)
                            searchResults = searchResults.map {
                                if (it.uid == user.uid) it.copy(hasPendingRequest = true) else it
                            }
                        }
                    }
                )
            } else {
                when (selectedTab) {
                    0 -> FriendsList(
                        friends = friends,
                        onRemoveFriend = { friend ->
                            coroutineScope.launch {
                                removeFriend(userState.uid, friend.uid)
                                friends = friends.filter { it.uid != friend.uid }
                            }
                        }
                    )
                    1 -> FriendRequestsList(
                        requests = friendRequests,
                        onAccept = { request ->
                            coroutineScope.launch {
                                acceptFriendRequest(request.id, userState.uid, request.fromUid)
                                friendRequests = friendRequests.filter { it.id != request.id }
                                friends = loadFriends(userState.uid, userState.friendsUID + request.fromUid)
                            }
                        },
                        onDecline = { request ->
                            coroutineScope.launch {
                                declineFriendRequest(request.id)
                                friendRequests = friendRequests.filter { it.id != request.id }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendsTabButton(
    text: String,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = if (isSelected) colorResource(id = R.color.button_primary) else Color.White
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) colorResource(id = R.color.button_primary) else Color.LightGray

    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        if (badgeCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) else colorResource(id = R.color.button_primary),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.White
                )
            }
        }
    }
}

@Composable
fun FriendsList(
    friends: List<FriendItem>,
    onRemoveFriend: (FriendItem) -> Unit
) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No friends yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search for users to add them as friends",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends) { friend ->
                FriendCard(
                    friend = friend,
                    onRemove = { onRemoveFriend(friend) }
                )
            }
        }
    }
}

@Composable
fun FriendCard(
    friend: FriendItem,
    onRemove: () -> Unit
) {
    var showRemoveConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (friend.profilePicture.isNotEmpty()) {
                    rememberAsyncImagePainter(friend.profilePicture)
                } else {
                    painterResource(id = R.drawable.default_user)
                },
                contentDescription = "Profile",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    if (friend.streakCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.flare_icon),
                            contentDescription = "Streak",
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = friend.streakCount.toString(),
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B35),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = "@${friend.userHandle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = showRemoveConfirm,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    IconButton(
                        onClick = {
                            onRemove()
                            showRemoveConfirm = false
                        }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Confirm",
                            tint = Color.Red
                        )
                    }
                    IconButton(onClick = { showRemoveConfirm = false }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.Gray
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !showRemoveConfirm,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { showRemoveConfirm = true }) {
                    Icon(
                        Icons.Default.PersonRemove,
                        contentDescription = "Remove friend",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestsList(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No pending requests",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Friend requests will appear here",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                FriendRequestCard(
                    request = request,
                    onAccept = { onAccept(request) },
                    onDecline = { onDecline(request) }
                )
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (request.fromProfilePicture.isNotEmpty()) {
                    rememberAsyncImagePainter(request.fromProfilePicture)
                } else {
                    painterResource(id = R.drawable.default_user)
                },
                contentDescription = "Profile",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.fromUserName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "@${request.fromUserHandle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            colorResource(id = R.color.button_primary).copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = colorResource(id = R.color.button_primary)
                    )
                }
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsList(
    results: List<FriendItem>,
    isSearching: Boolean,
    onAddFriend: (FriendItem) -> Unit
) {
    if (isSearching) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorResource(id = R.color.button_primary))
        }
    } else if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No users found",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { user ->
                SearchResultCard(
                    user = user,
                    onAddFriend = { onAddFriend(user) }
                )
            }
        }
    }
}

@Composable
fun SearchResultCard(
    user: FriendItem,
    onAddFriend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (user.profilePicture.isNotEmpty()) {
                    rememberAsyncImagePainter(user.profilePicture)
                } else {
                    painterResource(id = R.drawable.default_user)
                },
                contentDescription = "Profile",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "@${user.userHandle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            when {
                user.isFriend -> {
                    Text(
                        text = "Friends",
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.button_primary),
                        fontWeight = FontWeight.Medium
                    )
                }
                user.hasPendingRequest -> {
                    Text(
                        text = "Pending",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                else -> {
                    IconButton(
                        onClick = onAddFriend,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                colorResource(id = R.color.button_primary),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Add friend",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

suspend fun loadFriends(currentUid: String, friendUIDs: List<String>): List<FriendItem> {
    if (friendUIDs.isEmpty()) return emptyList()

    return try {
        val db = Firebase.firestore
        val friends = mutableListOf<FriendItem>()

        for (uid in friendUIDs) {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    friends.add(
                        FriendItem(
                            uid = uid,
                            userName = doc.getString("userName") ?: "Unknown",
                            userHandle = doc.getString("userHandle") ?: "unknown",
                            profilePicture = doc.getString("profilePicture") ?: "",
                            streakCount = doc.getLong("streakCount")?.toInt() ?: 0,
                            isFriend = true
                        )
                    )
                }
            } catch (e: Exception) {
                continue
            }
        }

        friends.sortedBy { it.userName.lowercase() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun loadFriendRequests(currentUid: String): List<FriendRequest> {
    return try {
        val db = Firebase.firestore
        val requests = mutableListOf<FriendRequest>()

        val querySnapshot = db.collection("friendRequests")
            .whereEqualTo("to", currentUid)
            .get()
            .await()

        for (doc in querySnapshot.documents) {
            val fromUid = doc.getString("from") ?: continue

            try {
                val userDoc = db.collection("users").document(fromUid).get().await()
                requests.add(
                    FriendRequest(
                        id = doc.id,
                        fromUid = fromUid,
                        fromUserName = userDoc.getString("userName") ?: "Unknown",
                        fromUserHandle = userDoc.getString("userHandle") ?: "unknown",
                        fromProfilePicture = userDoc.getString("profilePicture") ?: ""
                    )
                )
            } catch (e: Exception) {
                continue
            }
        }

        requests
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun searchUsers(query: String, currentUid: String, friendUIDs: List<String>): List<FriendItem> {
    return try {
        val db = Firebase.firestore
        val results = mutableListOf<FriendItem>()
        val lowerQuery = query.lowercase()

        val querySnapshot = db.collection("users").get().await()

        val pendingRequests = db.collection("friendRequests")
            .whereEqualTo("from", currentUid)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("to") }
            .toSet()

        for (doc in querySnapshot.documents) {
            val uid = doc.getString("uid") ?: doc.id
            if (uid == currentUid) continue

            val userName = doc.getString("userName") ?: ""
            val userHandle = doc.getString("userHandle") ?: ""

            if (userName.lowercase().contains(lowerQuery) ||
                userHandle.lowercase().contains(lowerQuery)) {
                results.add(
                    FriendItem(
                        uid = uid,
                        userName = userName,
                        userHandle = userHandle,
                        profilePicture = doc.getString("profilePicture") ?: "",
                        streakCount = doc.getLong("streakCount")?.toInt() ?: 0,
                        isFriend = friendUIDs.contains(uid),
                        hasPendingRequest = pendingRequests.contains(uid)
                    )
                )
            }
        }

        results.sortedBy { it.userName.lowercase() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun sendFriendRequest(fromUid: String, toUid: String) {
    try {
        val db = Firebase.firestore
        val request = hashMapOf(
            "from" to fromUid,
            "to" to toUid,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("friendRequests").add(request).await()
    } catch (e: Exception) {
    }
}

suspend fun acceptFriendRequest(requestId: String, currentUid: String, fromUid: String) {
    try {
        val db = Firebase.firestore

        db.collection("friends").add(
            hashMapOf(
                "users" to listOf(currentUid, fromUid),
                "timestamp" to System.currentTimeMillis()
            )
        ).await()

        db.collection("friendRequests").document(requestId).delete().await()
    } catch (e: Exception) {
    }
}

suspend fun declineFriendRequest(requestId: String) {
    try {
        val db = Firebase.firestore
        db.collection("friendRequests").document(requestId).delete().await()
    } catch (e: Exception) {
    }
}

suspend fun removeFriend(currentUid: String, friendUid: String) {
    try {
        val db = Firebase.firestore

        val querySnapshot = db.collection("friends")
            .whereArrayContains("users", currentUid)
            .get()
            .await()

        for (doc in querySnapshot.documents) {
            val users = doc.get("users") as? List<*>
            if (users != null && users.contains(friendUid)) {
                doc.reference.delete().await()
                break
            }
        }
    } catch (e: Exception) {
    }
}
