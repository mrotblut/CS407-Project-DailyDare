package com.cs407.dailydare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.dailydare.R
import com.cs407.dailydare.auth.EmailResult
import com.cs407.dailydare.auth.PasswordResult
import com.cs407.dailydare.auth.checkEmail
import com.cs407.dailydare.auth.checkPassword
import com.cs407.dailydare.auth.signIn
import com.cs407.dailydare.data.UserState
import com.google.firebase.auth.FirebaseUser

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val backgroundColor = colorResource(id = R.color.app_background)
    val buttonColor = colorResource(id = R.color.button_primary)
    val textFieldColor = colorResource(id = R.color.textfield_background)
    val secondaryButtonBgColor = colorResource(id = R.color.button_secondary_background)

    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            LogInButton(
                email = username.text,
                password = password.text,
                onSignedIn = { userState ->
                    if (userState.uid.isNotEmpty()) { // Check if UID is not empty
                        // user signed in, nav to home
                        onNavigateToHome()
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

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    color = colorResource(id = R.color.light_gray), // Using resource
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "    or    ",
                    color = colorResource(id = R.color.gray), // Using resource
                    fontSize = 14.sp
                )
                Divider(
                    color = colorResource(id = R.color.light_gray), // Using resource
                    thickness = 1.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = onNavigateToSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBgColor),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_account_button),
                    color = buttonColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LogInButton(
    email: String,
    password: String,
    onSignedIn: (UserState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var loginInProgress by remember { mutableStateOf(false) }

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

            if (errorString != null) {
                android.widget.Toast.makeText(context, errorString, android.widget.Toast.LENGTH_SHORT).show()
            } else {
                loginInProgress = true
                signIn(email, password) { userState ->
                    loginInProgress = false
                    onSignedIn(userState)
                }
            }
        },
        enabled = !loginInProgress,
        modifier = modifier, // Apply the modifier passed in
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.button_primary)),
        shape = RoundedCornerShape(28.dp)
    ) {
        if (loginInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = stringResource(R.string.login_button),
                color = colorResource(id = R.color.white),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen(
        onNavigateToSignUp = {},
        onNavigateToHome = {}
    )
}
