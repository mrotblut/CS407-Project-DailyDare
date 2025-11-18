package com.cs407.dailydare.data

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.cs407.dailydare.R

data class UserState(
    val uid: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val streakCount: Int = 0,
    val completedCount: Int = 0,
    val friendsCount: Int = 0,
    val profilePicture: Painter? = null,
    val completedChallenges: List<Challenge> = emptyList(),
    val currentChallenges: Challenge = Challenge(),
    val friendsUID: List<String> = emptyList(),
    val profilePicUrl: String = "",
    val completedChallengesUri: List<String> = emptyList()
)