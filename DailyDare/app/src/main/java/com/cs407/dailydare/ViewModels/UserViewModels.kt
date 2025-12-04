package com.cs407.dailydare.ViewModels

import android.R.attr.query
import android.util.Log
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import com.cs407.dailydare.R
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.data.Post
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.firestoreFriendRequest
import com.cs407.dailydare.data.firestoreFriends
import com.cs407.dailydare.data.firestoreNotification
import com.cs407.dailydare.data.firestorePost
import com.cs407.dailydare.data.firestoreUser
import com.cs407.dailydare.data.userFriend
import com.cs407.dailydare.ui.screens.Notification
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import okhttp3.internal.wait
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.time.Duration

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
        val format = DateTimeFormatter.ofPattern("yyyyMMdd")
        val yesterday = LocalDate.now().minusDays(1).format(format)
        val today = LocalDate.now().format(format)
        val streakCount =
            if ("Challenge/$yesterday" in userInfo.completedChallengeRef) {
                userInfo.streakCount
            } else if ("Challenge/$today" in userInfo.completedChallengeRef){
                1
            } else {
                0
            }
        val friendRequest = getFriendRequests(uid)
        val friendsUserStates = getFriendStates(friends)


        var curChal = Challenge()
        getTodayChallenge({c -> curChal = c})

        setUser(UserState(
            uid = userInfo.uid,
            userName = userInfo.userName,
            userHandle = userInfo.userHandle,
            streakCount = streakCount,
            completedCount = userInfo.completedCount,
            friendsCount = friends.size,
            completedChallenges = completedChallenges,
            currentChallenge = curChal,
            friendsUID = friends,
            profilePicUrl = userInfo.profilePicture,
            completedChallengesUri = userInfo.completedChallengeRef,
            friendsUserStates =  friendsUserStates,
            friendRequest = friendRequest
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
            completedChallengesUri = emptyList(),
            friendsUserStates =  emptyList(),
            friendRequest = emptyList()
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
        Thread.sleep(2000)
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

    fun getFriendRequests(uid:String): List<userFriend>{
        val uids = mutableListOf<String>()
        getFriendRequestsHelper(uid,{uid -> uids.addAll(uid)})
        val ret = mutableListOf<userFriend>()
        for (i in uids){
            getFriendStatesHelper(i, {friend -> ret.add(friend)})
        }
        return ret.toList()
    }
    fun getFriendRequestsHelper(uid:String, callback: (List<String>) -> Unit){
        val db = Firebase.firestore
        val uids = mutableListOf<String>()
        db.collection("friendRequest").whereEqualTo("to",uid).get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val docs = task.result
                for (i in docs){
                    val fr = i.toObject<firestoreFriendRequest>()
                    uids.add(fr.from)
                }
                callback(uids)
            }

        }
    }

    fun getFriendStates(friends: List<String>): List<userFriend>{
        val ret = mutableListOf<userFriend>()
        for (i in friends){
            getFriendStatesHelper(i, {friend -> ret.add(friend)})
        }
        return ret.toList()
    }
    fun getFriendStatesHelper(uid: String, callback: (userFriend) -> Unit){
        val db = Firebase.firestore
        db.collection("users").document(uid).get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                val user = task.result.toObject<userFriend>()
                callback(user!!)
            }
        }
    }

    suspend fun getTodayChallenge(callback: (Challenge) -> Unit){
        val format = DateTimeFormatter.ofPattern("yyyyMMdd")
        val today = LocalDate.now().format(format)
        val db = Firebase.firestore
        val task = db.collection("Challenge").document(today)
            .get().await()
        callback(task.toObject<Challenge>()!!)
    }

    fun getFeedPosts(setFeed: (List<Post>) -> Unit){
        if(userState.value.friendsCount==0){setFeed(emptyList()); return}
        val friends = userState.value.friendsUID + userState.value.uid
        Log.w("gFP",friends.toString())
        val db = Firebase.firestore
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
            posts.sortByDescending { it.date }
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
                    createNotification(post.uid,"NEWLIKE","${userState.value.userName} liked your post ${post.title}.")
                }
                post = firestorePost(post.uid,post.title,post.caption,post.date,post.contentUri,likes,post.postId, post.userName,post.userHandle,post.userProfile)
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
                date = Date(),
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
            streakCount = userState.value.streakCount +1 ,
            completedCount = userState.value.completedCount+1,
            friendsCount = userState.value.friendsCount,
            completedChallenges = userState.value.completedChallenges + challenge,
            currentChallenge = userState.value.currentChallenge,
            friendsUID = userState.value.friendsUID,
            profilePicUrl = userState.value.profilePicUrl,
            completedChallengesUri = userState.value.completedChallengesUri + challengeId,
            friendsUserStates =  userState.value.friendsUserStates,
            friendRequest = userState.value.friendRequest
        )

        setUser(newUserState)
        updateUserData()
        if(userState.value.streakCount % 5 == 0 || userState.value.streakCount == 1){
            createNotification(userState.value.uid,"STREAK","You now have a ${userState.value.streakCount} day streak!")
        }
    }

    fun searchFriends(search: String, callback: (List<userFriend>) -> Unit) {
        val results = mutableListOf<userFriend>()
        val end = search + "\uf8ff"
        val db = Firebase.firestore
        val usersRef = db.collection("users")

        // Query userName
        val q1 = usersRef
            .whereGreaterThanOrEqualTo("userName", search)
            .whereLessThan("userName", end)

        // Query userHandle
        val q2 = usersRef
            .whereGreaterThanOrEqualTo("userHandle", search)
            .whereLessThan("userHandle", end)

        q1.get().addOnSuccessListener { snap1 ->
            for (i in snap1){
                results.add(i.toObject<userFriend>())
            }

            q2.get().addOnSuccessListener { snap2 ->
                for (i in snap2){
                    results.add(i.toObject<userFriend>())
                }

                // Remove duplicates if the same user matched both fields
                val unique = results.distinctBy { it.uid }

                callback(unique)
            }
        }
    }

    fun friendRequest(friendUID: String){
        val db = Firebase.firestore
        val docRef = db.collection("friendRequests")
        val request = firestoreFriendRequest(from = userState.value.uid,to = friendUID)
        docRef.document("${userState.value.uid}--${friendUID}").set(request)
        createNotification(friendUID,"FRIENDREQUEST","New friend request from ${userState.value.userName}.")
    }

    fun acceptFriendRequest(newFriendUID: String){
        val db = Firebase.firestore
        val docRefRequest = db.collection("friendRequests")
        val docRefFriend = db.collection("friends")
        docRefFriend.add(firestoreFriends(listOf(newFriendUID, userState.value.uid)))
        docRefRequest.document("${newFriendUID}--${userState.value.uid}").delete()
        val friend = userState.value.friendRequest.find({ it.uid == newFriendUID })
        val newUserState = UserState(
            userState.value.uid,
            userState.value.userName,
            userState.value.userHandle,
            userState.value.streakCount,
            userState.value.completedCount,
            userState.value.friendsCount+1,
            userState.value.completedChallenges,
            userState.value.currentChallenge,
            userState.value.friendsUID+newFriendUID,
            userState.value.profilePicUrl,
            userState.value.completedChallengesUri,
            friendsUserStates =  userState.value.friendsUserStates + friend!!,
            friendRequest = userState.value.friendRequest - friend
        )
        setUser(newUserState)
        createNotification(newFriendUID,"NEWFRIEND","${userState.value.userName} has accepted your friend request!")

    }

    fun declineFriendRequest(friendUID: String) {
        val db = Firebase.firestore
        val docRefRequest = db.collection("friendRequests")
        docRefRequest.document("${friendUID}--${userState.value.uid}").delete()
        val friend = userState.value.friendRequest.find { it.uid == friendUID }
        if (friend != null) {
            val newUserState = UserState(
                userState.value.uid,
                userState.value.userName,
                userState.value.userHandle,
                userState.value.streakCount,
                userState.value.completedCount,
                userState.value.friendsCount,
                userState.value.completedChallenges,
                userState.value.currentChallenge,
                userState.value.friendsUID,
                userState.value.profilePicUrl,
                userState.value.completedChallengesUri,
                friendsUserStates = userState.value.friendsUserStates,
                friendRequest = userState.value.friendRequest - friend
            )
            setUser(newUserState)
        }
    }
    fun updateProfile(userName: String, userHandle: String, imageUrl: String){
        val newUserState = UserState(
            userState.value.uid,
            userName,
            userHandle,
            userState.value.streakCount,
            userState.value.completedCount,
            userState.value.friendsCount,
            userState.value.completedChallenges,
            userState.value.currentChallenge,
            userState.value.friendsUID,
            imageUrl,
            userState.value.completedChallengesUri,
            friendsUserStates =  userState.value.friendsUserStates,
            friendRequest = userState.value.friendRequest
        )
        setUser(newUserState)
        updateUserData()
    }

    fun createNotification(uid:String,type:String,message:String){
        val db = Firebase.firestore
        val docRef = db.collection("Notifications")
        val notification = firestoreNotification(message, Date(),type,uid)
        docRef.add(notification)
    }

    fun getNotifications(callback: (List<Notification>) -> Unit){
        val db = Firebase.firestore
        val docRef = db.collection("Notifications").whereEqualTo("uid",userState.value.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val notifications = mutableListOf<Notification>()
            val now = Instant.now()
            Log.w("getNotifications","${documentSnapshot.size()}")
            for (i in documentSnapshot) {
                val fsNotification = i.toObject<firestoreNotification>()
                val time = Duration.between(fsNotification.date.toInstant(), now)
                val days = time.toDays()
                val hours = time.toHours()
                val minutes = time.toMinutes()
                val timeString =
                    if (days >= 1) {
                        "${days}d"
                    } else if (hours > 0) {
                        "${hours}h"
                    } else if (minutes > 10) {
                        "${minutes}mins"
                    }else{
                        "now"
                    }
                val icon =
                    if (fsNotification.type == "NEWLIKE") {
                        R.drawable.heart
                    } else if (fsNotification.type == "FRIENDREQUEST") {
                        R.drawable.default_user
                    }else if (fsNotification.type == "NEWFRIEND") {
                        R.drawable.default_user
                    } else if (fsNotification.type == "STREAK") {
                        R.drawable.flare_icon
                    } else{
                        R.drawable.logo
                    }
                val notification = Notification(
                    id = notifications.size + 1,
                    message = fsNotification.message,
                    time = timeString,
                    icon = icon
                )
                notifications.add(notification)


            }
            callback(notifications.toList())
        }
    }

}