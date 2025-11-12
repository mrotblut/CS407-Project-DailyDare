package com.cs407.dailydare.data

import android.util.Log
import androidx.core.os.registerForAllProfilingResults
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.time.LocalDate

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

fun getUserData(uid: String): UserState{
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

    return UserState(
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
}

fun createDbUser(uid:String) : UserState{
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
    return userState
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
        date = "11/12/2025",
        imageRes = ""
    )
}
