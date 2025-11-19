package com.cs407.dailydare.data

import android.util.Log
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.core.os.registerForAllProfilingResults
import com.cs407.dailydare.utils.PhotoUploadManager.fetchPainter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.String


suspend fun getFriends(uid: String) : List<String> {
    val db = Firebase.firestore

    val querySnapshot = db.collection("friends")
        .whereArrayContains("UID", uid).get().await()

    val friends = mutableListOf<String>()

    for (doc in querySnapshot) {
        // Get the array field "UID" as a list
        val friendList = doc.get("UID") as? List<String> ?: emptyList()
        Log.w("getFriends",friendList.toString())

        if (friendList.size == 2) {
            val friend = if (friendList[0] == uid) {
                friendList[1]
            } else {
                friendList[0]
            }
            friends.add(friend)
        }
    }
    Log.w("getFriends",friends.toString())
    return(friends)
}



suspend fun getUserData(uid: String, updateUser: (UserState) -> Unit){
    val db = Firebase.firestore
    val docRef = db.collection("users").document(uid)
    val task = docRef.get().await()
    val userInfo = task.toObject<firestoreUser>()!!
    val completedChallenges = getChallenges(userInfo.completedChallengeRef)
    val friends = getFriends(uid)
    Log.w("After-getFriends-gUD",friends.toString())
    var profilePic: Painter? = null
    if (!userInfo.profilePicture.isBlank())fetchPainter(userInfo.profilePicture,{painter -> profilePic = painter})

    var curChal = Challenge()
    getTodayChallenge({c -> curChal = c})

    updateUser(UserState(
        uid = userInfo.uid,
        userName = userInfo.userName,
        userHandle = userInfo.userHandle,
        streakCount = userInfo.streakCount,
        completedCount = userInfo.completedCount,
        friendsCount = friends.size,
        profilePicture = profilePic,
        completedChallenges = completedChallenges,
        currentChallenges = curChal,
        friendsUID = friends,
        profilePicUrl = userInfo.profilePicture,
        completedChallengesUri = userInfo.completedChallengeRef
    )
    )

}




suspend fun createDbUser(uid:String, updateUser: (UserState) -> Unit) {
    val db = Firebase.firestore
    var curChal = Challenge()
    getTodayChallenge({c -> curChal = c})
    val userState = UserState(
        uid = uid,
        userName = "Anonymous User",
        userHandle = "Anonymous_User_"+uid.filter{it.isDigit()},
        streakCount = 0,
        completedCount = 0,
        friendsCount = 0,
        profilePicture = null,
        completedChallenges = emptyList(),
        currentChallenges = curChal,
        friendsUID = emptyList(),
        profilePicUrl = "",
        completedChallengesUri = emptyList()
    )
    val fsUserState = firestoreUser(
        uid = userState.uid,
        userName = userState.userName,
        userHandle = userState.userHandle,
        streakCount = userState.streakCount,
        completedCount = userState.completedCount,
        profilePicture = userState.profilePicUrl,
        completedChallengeRef = userState.completedChallengesUri
    )
    db.collection("users").document(uid).set(fsUserState)
    updateUser(userState)
}

fun updateUserData(userState:UserState){
    val db = Firebase.firestore
    val fsUser = firestoreUser(
        uid = userState.uid,
        userName = userState.userName,
        userHandle = userState.userHandle,
        streakCount = userState.streakCount,
        completedCount = userState.completedChallenges.size,
        profilePicture = userState.profilePicUrl,
        completedChallengeRef = userState.completedChallengesUri
    )
    db.collection("users").document(userState.uid).set(fsUser)
}

fun getChallenges(challenges: List<String>): List<Challenge>{
    val retChallenge = mutableListOf<Challenge>()
    for (challenge in challenges){
        getChallengesHelper(challenge,{c -> retChallenge.add(c)})
    }
    return retChallenge
}
private fun getChallengesHelper(challenge: String, callback: (Challenge) -> Unit){
    val db = Firebase.firestore
    db.document(challenge).get().addOnCompleteListener { task ->
        if(task.isSuccessful){
            val fbc = task.result.toObject<Challenge>()
            callback(fbc!!)
        }
    }
}

suspend fun getTodayChallenge(callback: (Challenge) -> Unit){
    val format = SimpleDateFormat("MM/dd/YYYY")
    val db = Firebase.firestore
    val task = db.collection("Challenge").document("20251115") // TODO: Implement Date when more challenges are added
        .get().await()
    callback(task.toObject<Challenge>()!!)
}

fun getFeedPosts(userState: UserState, setFeed: (List<Post>) -> Unit){
    if(userState.friendsCount==0){setFeed(emptyList()); return}
    val friends = userState.friendsUID
    Log.w("gFP",friends.toString())
    val db = Firebase.firestore
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val dates = listOf(today,yesterday)
    val docRef = db.collection("posts").whereIn("uid",friends)
    docRef.get().addOnSuccessListener { documentSnapshot  ->
        val posts = mutableListOf<Post>()
        Log.w("gFP",documentSnapshot.size().toString())
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
                isLiked = userState.uid in fsPost.likes,
                userName = "userName", // TODO
                userHandle = "userHandle", // TODO
                profilePicture = "https://i.ibb.co/7xF5rPr7/jump.jpg" //TODO
            )
            posts.add(post)
        }
        Log.w("gFP",posts.toString())
        setFeed(posts)
        return@addOnSuccessListener
    }
        .addOnFailureListener { e->
            Log.w("gFP",e.toString())
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

fun postPost(userState:UserState, challenge: Challenge, imageLink: String, caption:String, updateUserState: (UserState) -> Unit){
    // Create Post in DB
    val db = Firebase.firestore
    val postId = userState.uid+"-"+LocalDate.now()
    val docRef = db.collection("posts").document(postId)
    docRef.set(firestorePost(userState.uid,challenge.title,caption,challenge.date,imageLink,emptyList(),postId))

    // Add Completed Challenge to US
    val challengeId = "Challenge/" + challenge.id.toString()
    val newUserState = UserState(
        uid = userState.uid,
        userName = userState.userName,
        userHandle = userState.userHandle,
        streakCount = userState.streakCount,
        completedCount = userState.completedCount+1,
        friendsCount = userState.friendsCount,
        profilePicture = userState.profilePicture,
        completedChallenges = userState.completedChallenges + challenge,
        currentChallenges = userState.currentChallenges,
        friendsUID = userState.friendsUID,
        profilePicUrl = userState.profilePicUrl,
        completedChallengesUri = userState.completedChallengesUri + challengeId
    )
    updateUserData(newUserState)
    updateUserState(newUserState)
}

fun friendRequest(userState: UserState, friendUID: String){
    TODO()
}

fun acceptFriendRequest(userState: UserState, newFriendUID: String){
    TODO()
    updateUserData(TODO())
}
fun updateProfile(userState: UserState, userName: String, userHandle: String, imageUrl: String){
    TODO()
}
