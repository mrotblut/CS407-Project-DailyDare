package com.cs407.dailydare.data

import java.util.Date

data class Challenge(
    val id: Int = 0,
    val title: String = "",
    val date: Date = Date(),
    val imageLink: String = "",
    val description: String = ""
)

data class Post(
    val uid: String,
    val postId: String,
    val title: String,
    val caption: String,
    val date: Date,
    val contentUri: String,
    val likes: Int,
    var isLiked: Boolean,
    val userName: String,
    val userHandle: String,
    val profilePicture: String,
    )

data class userFriend(
    val uid: String,
    val userName: String,
    val userHandle: String,
    val profilePicture: String
)