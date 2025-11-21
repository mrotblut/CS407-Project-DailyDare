package com.cs407.dailydare.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.data.Post
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.firestorePost
import com.cs407.dailydare.data.firestoreUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserViewModel : ViewModel() {
    private val _userState = MutableStateFlow(UserState())
    private val auth: FirebaseAuth = Firebase.auth
    val userState = _userState.asStateFlow()
    init {
        auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                setUser(UserState())
            }
        }
    }
    fun setUser(state: UserState) {
        _userState.update { state }
    }
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



    suspend fun getUserData(uid: String, onSignedIn: () -> Unit){
        val db = Firebase.firestore
        val docRef = db.collection("users").document(uid)
        val task = docRef.get().await()
        val userInfo = task.toObject<firestoreUser>()!!
        val completedChallenges = getChallenges(userInfo.completedChallengeRef)
        val friends = getFriends(uid)
        Log.w("After-getFriends-gUD",friends.toString())


        var curChal = Challenge()
        getTodayChallenge({c -> curChal = c})

        setUser(UserState(
            uid = userInfo.uid,
            userName = userInfo.userName,
            userHandle = userInfo.userHandle,
            streakCount = userInfo.streakCount,
            completedCount = userInfo.completedCount,
            friendsCount = friends.size,
            completedChallenges = completedChallenges,
            currentChallenge = curChal,
            friendsUID = friends,
            profilePicUrl = userInfo.profilePicture,
            completedChallengesUri = userInfo.completedChallengeRef
        )
        )
        onSignedIn()

    }




    suspend fun createDbUser(uid:String, onSignedIn:() -> Unit) {
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
            completedChallenges = emptyList(),
            currentChallenge = curChal,
            friendsUID = emptyList(),
            profilePicUrl = "https://i.ibb.co/Lh2BnV7T/default-user.png",
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
        setUser(userState)
        onSignedIn()
    }

    fun updateUserData(){
        val db = Firebase.firestore
        val fsUser = firestoreUser(
            uid = userState.value.uid,
            userName = userState.value.userName,
            userHandle = userState.value.userHandle,
            streakCount = userState.value.streakCount,
            completedCount = userState.value.completedChallenges.size,
            profilePicture = userState.value.profilePicUrl,
            completedChallengeRef = userState.value.completedChallengesUri
        )
        db.collection("users").document(userState.value.uid).set(fsUser)
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

    fun getFeedPosts(setFeed: (List<Post>) -> Unit){
        if(userState.value.friendsCount==0){setFeed(emptyList()); return}
        val friends = userState.value.friendsUID + userState.value.uid
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
                    isLiked = userState.value.uid in fsPost.likes,
                    userName = fsPost.userName,
                    userHandle = fsPost.userHandle,
                    profilePicture = fsPost.userProfile
                )
                posts.add(post)
            }
            Log.w("gFP",posts.toString())
            setFeed(posts)
            return@addOnSuccessListener
        }
            .addOnFailureListener { e->
                Log.w("gFP-error",e.toString())
            }
    }

    fun changeLikePost(postId:String){
        val db = Firebase.firestore
        val uid = userState.value.uid
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

    fun postPost(imageLink: String, caption:String){
        // Create Post in DB
        val challenge = userState.value.currentChallenge
        val db = Firebase.firestore
        val postId = userState.value.uid+"-"+LocalDate.now()
        val docRef = db.collection("posts").document(postId)
        docRef.set(
            firestorePost(
                uid = userState.value.uid,
                title = challenge.title,
                caption = caption,
                date = challenge.date,
                contentUri = imageLink,
                likes = emptyList(),
                postId = postId,
                userName = userState.value.userName,
                userHandle = userState.value.userHandle,
                userProfile = userState.value.profilePicUrl
            )
        )

        // Add Completed Challenge to US
        val challengeId = "Challenge/" + challenge.id.toString()
        val newUserState = UserState(
            uid = userState.value.uid,
            userName = userState.value.userName,
            userHandle = userState.value.userHandle,
            streakCount = userState.value.streakCount,
            completedCount = userState.value.completedCount+1,
            friendsCount = userState.value.friendsCount,
            completedChallenges = userState.value.completedChallenges + challenge,
            currentChallenge = userState.value.currentChallenge,
            friendsUID = userState.value.friendsUID,
            profilePicUrl = userState.value.profilePicUrl,
            completedChallengesUri = userState.value.completedChallengesUri + challengeId
        )

        setUser(newUserState)
        updateUserData()
    }

    fun friendRequest(friendUID: String){
        TODO()
    }

    fun acceptFriendRequest(newFriendUID: String){
        TODO()

    }
    fun updateProfile(userName: String, userHandle: String, imageUrl: String){
        TODO()
    }

}