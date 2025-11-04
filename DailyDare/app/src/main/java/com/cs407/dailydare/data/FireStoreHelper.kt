package com.cs407.dailydare.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

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

    val docRef = db.collection("users").document(uid)
        docRef.get().addOnSuccessListener { documentSnapshot  ->
            val userInfo = documentSnapshot.toObject<firestoreUser>()
        }

    val userInfo = firestoreUser()
    val friends = getFriends(userInfo.uid)
    return UserState(
        uid = userInfo.uid,
        userName = userInfo.userName,
        userHandle = userInfo.userHandle,
        streakCount = userInfo.streakCount,
        completedCount = userInfo.completedCount,
        friendsCount = friends.size,
        profilePicture = TODO(),
        completedChallenges = TODO(),
        currentChallenges = TODO(),
        friendsUID = friends
    )
}

fun setUserData(uid: String){
    val db = Firebase.firestore
    val user = hashMapOf(
        "first" to "Ada",
        "last" to "Lovelace",
        "born" to 1815,
    )

// Add a new document with a generated ID
    db.collection("users")
        .add(user)
        .addOnSuccessListener { documentReference ->
            Log.d("SetUserData", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("SetUserData", "Error adding document", e)
        }
}