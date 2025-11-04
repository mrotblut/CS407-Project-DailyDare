package com.cs407.dailydare.data

import java.util.Date

data class firestoreUser(
    val uid: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val streakCount: Int = 0,
    val completedCount: Int = 0,
    val profilePicture: String = ""
)

data class firestoreFriends(
    val UID: List<String>
)

data class firestoreFriendRequest(
    val from: String,
    val to: String
)

data class firestoreUserChallenges(
    val ChallengeID: String,
    val UserUID: String,
    val completed: Boolean,
    val date: Date
)

data class firestoreChallenge(
    val id: String,
    val imageRes: String,
    val title: String
)