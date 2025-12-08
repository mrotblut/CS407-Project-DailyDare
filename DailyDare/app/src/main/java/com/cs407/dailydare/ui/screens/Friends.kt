package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.userFriend
import com.cs407.dailydare.ui.components.BottomNavigationBar
import com.cs407.dailydare.ui.components.TopNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigationToProfile: () -> Unit,
    userViewModel: UserViewModel,
    userState: UserState
) {
    LaunchedEffect(Unit){
        userViewModel.updateFriends()
    }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<userFriend>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var sentRequests by remember { mutableStateOf<Set<String>>(emptySet()) }

    val focusManager = LocalFocusManager.current

    val friendsList = userState.friendsUserStates
    val friendRequests = userState.friendRequest

    fun performSearch() {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            hasSearched = true
            userViewModel.searchFriends(searchQuery) { results ->
                val filtered = results.filter { user ->
                    user.uid != userState.uid &&
                            user.uid !in userState.friendsUID
                }
                searchResults = filtered
                isSearching = false
            }
        } else {
            searchResults = emptyList()
            hasSearched = false
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(title = "Friends")
        },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorResource(id = R.color.app_background))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        if (it.isBlank()) {
                            searchResults = emptyList()
                            hasSearched = false
                        }
                    },
                    onSearch = {
                        performSearch()
                        focusManager.clearFocus()
                    }
                )
            }

            if (hasSearched || isSearching) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Search Results",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorResource(id = R.color.button_primary)
                            )
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    item {
                        Text(
                            text = "No users found",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(searchResults, key = { it.uid }) { user ->
                        SearchResultItem(
                            friend = user,
                            isSent = sentRequests.contains(user.uid),
                            onSendRequest = {
                                userViewModel.friendRequest(user.uid)
                                sentRequests = sentRequests + user.uid
                            }
                        )
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray
                    )
                }
            }

            if (friendRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Friend Requests (${friendRequests.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(friendRequests, key = { it.uid }) { request ->
                    FriendRequestItem(
                        friend = request,
                        onAccept = {
                            userViewModel.acceptFriendRequest(request.uid)
                        },
                        onDecline = {
                            userViewModel.declineFriendRequest(request.uid)
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray
                    )
                }
            }

            item {
                Text(
                    text = "My Friends (${friendsList.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (friendsList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No friends yet",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search for users above to add friends!",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(friendsList, key = { it.uid }) { friend ->
                    FriendItem(friend = friend, {uid -> userViewModel.removeFriend(uid)})
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("username or handle...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.button_primary),
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSearch,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.button_primary)
            )
        ) {
            Text("Search")
        }
    }
}

@Composable
fun FriendItem(friend: userFriend, onRemove: (String) -> Unit) {
    val defaultUser = painterResource(id = R.drawable.default_user)
    val profileImage: Painter = if (friend.profilePicture.isNotEmpty()) {
        rememberAsyncImagePainter(model = friend.profilePicture)
    } else {
        defaultUser
    }
    val expand = remember{ mutableStateOf(false)}
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {expand.value = !expand.value},

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = profileImage,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "@${friend.userHandle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        if (expand.value) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { onRemove(friend.uid) },
                    colors = ButtonColors(Color(0xFFE53935),Color.White,Color(0xFFE53935),Color.White)
                ) {
                    Text(
                        text = "Remove Friend"
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    friend: userFriend,
    isSent: Boolean,
    onSendRequest: () -> Unit
) {
    val defaultUser = painterResource(id = R.drawable.default_user)
    val profileImage: Painter = if (friend.profilePicture.isNotEmpty()) {
        rememberAsyncImagePainter(model = friend.profilePicture)
    } else {
        defaultUser
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                painter = profileImage,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "@${friend.userHandle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onSendRequest,
                enabled = !isSent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSent) Color.Gray else colorResource(id = R.color.button_primary),
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = if (isSent) Icons.Default.Check else Icons.Default.PersonAdd,
                    contentDescription = if (isSent) "Sent" else "Add Friend",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSent) "Sent" else "Add",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    friend: userFriend,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val defaultUser = painterResource(id = R.drawable.default_user)
    val profileImage: Painter = if (friend.profilePicture.isNotEmpty()) {
        rememberAsyncImagePainter(model = friend.profilePicture)
    } else {
        defaultUser
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                painter = profileImage,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, colorResource(id = R.color.button_primary), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "@${friend.userHandle}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Row {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFE53935),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
