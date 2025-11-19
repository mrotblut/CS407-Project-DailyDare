package com.cs407.dailydare.data

import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class firestoreUser(
    val uid: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val streakCount: Int = 0,
    val completedCount: Int = 0,
    val profilePicture: String = "",
    val completedChallengeRef: List<String> = emptyList()
)

data class firestoreFriends(
    val UID: List<String>
)

data class firestoreFriendRequest(
    val from: String,
    val to: String
)

data class firestorePost(
    val uid: String = "",
    val title: String = "",
    val caption: String = "",
    val date: Date = Date(),
    val contentUri: String = "",
    val likes: List<String> = emptyList(),
    val postId: String = ""
)