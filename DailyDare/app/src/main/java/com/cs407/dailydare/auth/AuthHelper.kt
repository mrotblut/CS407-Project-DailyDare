package com.cs407.dailydare.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    onSignedIn: (UserState) -> Unit,
) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        getUserData(uid = user.uid) { userState ->
                            onSignedIn(userState)
                        }
                    }
                } else {
                    onSignedIn(UserState())
                }
            } else {
                // if the task is not successful, sign in failed.
                onSignedIn(UserState())
            }
        }
}

enum class SignUpResult {
    Error,
}

fun createAccount(
    email: String,
    password: String,
    onAccountCreated: (UserState) -> Unit,
){
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    // successfully created user, now create their database entry
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        createDbUser(uid = user.uid) { userState ->
                            onAccountCreated(userState)
                        }
                    }
                } else {
                    onAccountCreated(UserState())
                }
            } else {
                // If account creation fails (e.g., email already in use),
                onAccountCreated(UserState())
            }
        }
}


