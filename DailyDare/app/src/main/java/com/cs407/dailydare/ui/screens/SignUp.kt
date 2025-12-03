package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource // <-- Import colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.dailydare.R // <-- Import your R file
import com.cs407.dailydare.auth.EmailResult
import com.cs407.dailydare.auth.PasswordResult
import com.cs407.dailydare.auth.checkEmail
import com.cs407.dailydare.auth.checkPassword
import com.cs407.dailydare.auth.createAccount
import com.cs407.dailydare.auth.signIn
import com.cs407.dailydare.data.UserState

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
) {
    val backgroundColor = colorResource(id = R.color.app_background)
    val buttonColor = colorResource(id = R.color.button_primary)
    val textFieldColor = colorResource(id = R.color.textfield_background)

    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onNavigateToSignIn,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorResource(id = R.color.black)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.black)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_slogan),
                color = colorResource(id = R.color.gray),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Email or Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = textFieldColor,
                    unfocusedContainerColor = textFieldColor,
                    disabledContainerColor = textFieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = buttonColor
                ),
                textStyle = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = textFieldColor,
                    unfocusedContainerColor = textFieldColor,
                    disabledContainerColor = textFieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = buttonColor
                ),
                textStyle = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = textFieldColor,
                    unfocusedContainerColor = textFieldColor,
                    disabledContainerColor = textFieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = buttonColor
                ),
                textStyle = TextStyle(fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Sign Up button, successful sign up takes user back to sign in
            SignUpButton(
                email = username.text,
                password = password.text,
                confirmPassword = confirmPassword.text,
                onAccountCreated = { userState ->
                    if (userState.uid.isNotEmpty()) { // Check if UID is not empty
                        // user signed in, nav to home
                        onNavigateToSignIn()
                    } else {
                        // Login failed, show a message to the user
                        android.widget.Toast.makeText(context, "Login failed. Please check your credentials.", android.widget.Toast.LENGTH_SHORT).show()
                        println("Login failed")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }
    }
}

@Composable
fun SignUpButton(
    email: String,
    password: String,
    confirmPassword: String,
    onAccountCreated: (UserState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var signUpProgress by remember { mutableStateOf(false) }

    Button(
        onClick = {
            var errorString: String? = null

            val emailResult = checkEmail(email)
            if (emailResult == EmailResult.Empty) {
                errorString = context.getString(R.string.empty_email)
            } else if (emailResult == EmailResult.Invalid) {
                errorString = context.getString(R.string.invalid_email)
            }

            val passwordResult = checkPassword(password)
            if (errorString == null) {
                errorString = when (passwordResult) {
                    PasswordResult.Empty -> {
                        context.getString(R.string.empty_password)
                    }

                    PasswordResult.Short -> {
                        context.getString(R.string.short_password)
                    }

                    PasswordResult.Invalid -> {
                        context.getString(R.string.invalid_password)
                    }

                    PasswordResult.Valid -> {
                        null
                    }
                }
            }

            if (errorString == null && password != confirmPassword) {
                errorString = "Passwords do not match."
            }

            if (errorString != null) {
                android.widget.Toast.makeText(context, errorString, android.widget.Toast.LENGTH_SHORT).show()
            } else {
                signUpProgress = true
                createAccount(email, password) { userState ->
                    signUpProgress = false
                    onAccountCreated(userState)
                }
            }
        },
        enabled = !signUpProgress,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.button_primary)),
        shape = RoundedCornerShape(28.dp)
    ) {
        if (signUpProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = stringResource(R.string.sign_up_button),
                color = colorResource(id = R.color.white),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigateToSignIn = {},
    )
}
