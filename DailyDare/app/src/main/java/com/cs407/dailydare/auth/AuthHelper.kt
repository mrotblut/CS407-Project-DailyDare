package com.cs407.dailydare.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
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
    Error,
}

fun signIn(
    email: String,
    password: String,
    getUserData: (String, () -> Unit) -> Unit,
    errorCallback: (SignInResult) -> Unit,
    onSignedIn: () -> Unit
) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                errorCallback(SignInResult.Error)
            }

            val user = auth.currentUser ?: task.result?.user
            if (user == null) {
                errorCallback(SignInResult.Error)

            } else{
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch{getUserData(user.uid,onSignedIn)}
            }

        }

}

enum class SignUpResult {
    Error,
}

fun createAccount(
    email: String,
    password: String,
    onSignedIn: () -> Unit,
    createDbUser: (String, () -> Unit) -> Unit,
    errorCallback:(SignUpResult) -> Unit
    //any other callback function or parameters if you want
) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                errorCallback(SignUpResult.Error)
                return@addOnCompleteListener
            }

            val user = auth.currentUser ?: task.result?.user
            if (user == null) {
                errorCallback(SignUpResult.Error)
                return@addOnCompleteListener
            }
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch{createDbUser( user.uid, onSignedIn)}
        }
}


