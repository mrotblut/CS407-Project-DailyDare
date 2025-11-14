package com.cs407.dailydare.auth

import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.createDbUser
import com.cs407.dailydare.data.getUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


enum class EmailResult {
    Valid,
    Empty,
    Invalid,
}

fun checkEmail(email: String): EmailResult {
    if (email.isEmpty()){
        return EmailResult.Empty
    }
    val pattern = Regex("^[\\w.]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
    val isMatch = email.matches(pattern)
    if (isMatch){
        return EmailResult.Valid
    }
    return EmailResult.Invalid

}


enum class PasswordResult {
    Valid,
    Empty,
    Short,
    Invalid
}

fun checkPassword(password: String):PasswordResult {

    if (password.isEmpty()) {
        return PasswordResult.Empty
    }
    if (password.length < 5) {
        return PasswordResult.Short
    }
    if (Regex("\\d+").containsMatchIn(password) &&
        Regex("[a-z]+").containsMatchIn(password) &&
        Regex("[A-Z]+").containsMatchIn(password)
    ) {
        return PasswordResult.Valid
    }
    return PasswordResult.Invalid
}

enum class SignInResult {
    Success,
    Error,
}

fun signIn(
    email: String,
    password: String,
    onSignedIn: (UserState) -> Unit,
):SignInResult {
    var result = SignInResult.Success
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                result = SignInResult.Error
            }

            val user = auth.currentUser ?: task.result?.user
            if (user == null) {
                onSignedIn(UserState())
                result = SignInResult.Error

            } else{
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch{getUserData(uid = user.uid,onSignedIn)}
            }

        }
    return result
}

enum class SignUpResult {
    Success,
    Error,
}

fun createAccount(
    email: String,
    password: String,
    onSignedIn: (UserState) -> Unit,
    //any other callback function or parameters if you want
) : SignUpResult{
    var result = SignUpResult.Success
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onSignedIn(UserState())
                result = SignUpResult.Error
                return@addOnCompleteListener
            }

            val user = auth.currentUser ?: task.result?.user
            if (user == null) {
                onSignedIn(UserState())
                result = SignUpResult.Error
                return@addOnCompleteListener
            }
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch{createDbUser(uid = user.uid,onSignedIn)}
        }
    return result
}


