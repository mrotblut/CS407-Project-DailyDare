package com.cs407.dailydare.data

import android.util.Log
import androidx.core.os.registerForAllProfilingResults
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

fun getFriends(uid:String): List<String>{
    val db = Firebase.firestore
    var friends = emptyList<String>()
    val docRef = db.collection("friends").whereArrayContains("uid",uid)
    docRef.get().addOnSuccessListener { documentSnapshot  ->
        for (i in documentSnapshot){
            val friendList = i.toObject<List<String>>()
            val friend = if (friendList[0] == uid){friendList[1]}else{friendList[0]}
            friends = friends+friend
            }
        }
    return friends
    }

fun getUserData(uid: String, updateUser: (UserState) -> Unit){
    val db = Firebase.firestore
    var userInfo: firestoreUser? = null
    val docRef = db.collection("users").document(uid)
    docRef.get().addOnCompleteListener  { task  ->
        if (task.isSuccessful) {
            userInfo = task.result.toObject<firestoreUser>()!!

        }

    }

    val completedChallenges = getChallenges(userInfo!!.completedChallengeRef)
    val friends = getFriends(userInfo.uid)

    updateUser(UserState(
        uid = userInfo.uid,
        userName = userInfo.userName,
        userHandle = userInfo.userHandle,
        streakCount = userInfo.streakCount,
        completedCount = userInfo.completedCount,
        friendsCount = friends.size,
        profilePicture = null, //TODO: get image after figure out image storage userInfo.profilePicture,
        completedChallenges = completedChallenges,
        currentChallenges = getTodayChallenge(),
        friendsUID = friends
    )
    )
}

fun createDbUser(uid:String, updateUser: (UserState) -> Unit) {
    val db = Firebase.firestore
    val userState = UserState(
        uid = uid,
        userName = "Anonymous User",
        userHandle = "Anonymous_User_"+uid.filter{it.isDigit()},
        streakCount = 0,
        completedCount = 0,
        friendsCount = 0,
        profilePicture = null,
        completedChallenges = emptyList(),
        currentChallenges = getTodayChallenge(),
        friendsUID = emptyList()
    )
    db.collection("users").document(uid).set(userState)
    updateUser(userState)
}

fun updateUserData(userState:UserState){
    TODO()
}

fun getChallenges(challenges: List<String>):List<Challenge>{
    return emptyList()
}

fun getTodayChallenge(): Challenge{
    return Challenge(
        id = 0,
        title =" Do 10 jumping jacks",
        date = Date(11/12/2025),
        imageRes = ""
    )
}

fun getFeedPosts(userState: UserState, setFeed: (List<Post>) -> Unit){
    if(userState.friendsCount==0){setFeed(emptyList()); return}
    val friends = userState.friendsUID
    val db = Firebase.firestore
    val docRef = db.collection("posts").whereIn("uid",friends)
    docRef.get().addOnSuccessListener { documentSnapshot  ->
        val posts = mutableListOf<Post>()
        for (i in documentSnapshot){
            val fsPost = i.toObject<firestorePost>()
            val post = Post(
                uid = fsPost.uid,
                postId = i.id,
                title = fsPost.title,
                caption = fsPost.caption,
                date = fsPost.date,
                contentUri = fsPost.contentUri,
                likes = fsPost.likes.size,
                isLiked = userState.uid in fsPost.likes
            )
            posts.add(post)
        }
        setFeed(posts)
        return@addOnSuccessListener
    }
}

fun changeLikePost(userState: UserState, postId:String){
    val db = Firebase.firestore
    val uid = userState.uid
    val docRef = db.collection("posts").document(postId)
    docRef.get().addOnCompleteListener  { task  ->
        if (task.isSuccessful) {
            var post = task.result.toObject<firestorePost>()!!
            val likes = post.likes.toMutableList()
            val liked = uid in likes
            if (liked){
                likes.remove(uid)
            }else{
                likes.add(uid)
            }
             post = firestorePost(post.uid,post.title,post.caption,post.date,post.contentUri,likes,post.postId)
            docRef.set(post)

        }

    }
}

fun postPost(userState:UserState, challenge: Challenge, imageLink: String, caption:String){
    val db = Firebase.firestore
    val postId = userState.uid+"-"+LocalDate.now()
    val docRef = db.collection("posts").document(postId)
    docRef.set(firestorePost(userState.uid,challenge.title,caption,challenge.date,imageLink,emptyList(),postId))
}

