package com.cs407.dailydare.data

data class UserState(
    val uid: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val streakCount: Int = 0,
    val completedCount: Int = 0,
    val friendsCount: Int = 0,
    val completedChallenges: List<Challenge> = emptyList(),
    val currentChallenge: Challenge = Challenge(),
    val friendsUID: List<String> = emptyList(),
    val profilePicUrl: String = "",
    val completedChallengesUri: List<String> = emptyList()
)