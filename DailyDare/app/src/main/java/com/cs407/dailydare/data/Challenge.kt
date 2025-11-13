package com.cs407.dailydare.data

import java.util.Date

data class Challenge(
    val id: Int,
    val title: String,
    val date: Date,
    val imageRes: String
)

data class Post(
    val uid: String,
    val postId: String,
    val title: String,
    val caption: String,
    val date: Date,
    val contentUri: String,
    val likes: Int,
    var isLiked: Boolean
    )