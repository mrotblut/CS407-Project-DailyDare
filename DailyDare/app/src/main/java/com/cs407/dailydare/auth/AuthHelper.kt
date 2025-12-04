package com.cs407.dailydare.auth

import com.cs407.dailydare.ViewModels.UserViewModel
import com.cs407.dailydare.data.UserState
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

fun signIn(
    email: String,
    password: String,
    onSignedIn: () -> Unit,
    userViewModel: UserViewModel,
    error: () -> Unit
) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        userViewModel.getUserData(uid = user.uid, onSignedIn)
                    }
                } else {
                    error()
                }
            } else {
                // if the task is not successful, sign in failed.
                error()
            }
        }
}


fun createAccount(
    email: String,
    password: String,
    onAccountCreated: () -> Unit,
    uvm: UserViewModel,
    error: () -> Unit
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
                        uvm.createDbUser(uid = user.uid, onAccountCreated)
                    }
                } else {
                    error()
                }
            } else {
                // If account creation fails (e.g., email already in use),
                error()
            }
        }
}


