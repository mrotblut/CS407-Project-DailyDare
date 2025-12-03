package com.cs407.dailydare.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.cs407.dailydare.R
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.utils.PhotoUploadManager
import java.text.SimpleDateFormat


@Composable
fun PostScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigationToProfile: () -> Unit,
    challenge: Challenge,
    onPost: (String, String) -> Unit  // Caption and media URI
) {
    val challengeTitle = challenge.title
    val backgroundColor = colorResource(id = R.color.app_background)
    val buttonColor = colorResource(id = R.color.button_primary)
    val textFieldColor = colorResource(id = R.color.textfield_background)

    var caption by remember { mutableStateOf(TextFieldValue("")) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaOptions by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Function to create a new URI for each photo
    fun createPhotoUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DailyDare")
            }
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    // Store the current photo URI
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher - FULL RESOLUTION photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            // Photo was taken successfully, set it as selected media
            selectedMediaUri = currentPhotoUri
        } else if (!success && currentPhotoUri != null) {
            // User cancelled, delete the empty file
            try {
                context.contentResolver.delete(currentPhotoUri!!, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create new URI and launch camera
            currentPhotoUri = createPhotoUri()
            currentPhotoUri?.let { cameraLauncher.launch(it) }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedMediaUri = uri
    }


    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToChallenge) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Challenge title
            Text(
                text = "Challenge: $challengeTitle",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Media preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMediaUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedMediaUri),
                        contentDescription = "Selected media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No media",
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No photo or video selected",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media selection buttons - Stack vertically for better spacing
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Take Photo button
                OutlinedButton(
                    onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                // Permission already granted, create URI and launch camera
                                currentPhotoUri = createPhotoUri()
                                currentPhotoUri?.let { cameraLauncher.launch(it) }
                            }
                            else -> {
                                // Request permission
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = buttonColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }

                // Choose from Gallery button
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = buttonColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose from Gallery")
                }

                // Record Video button
                //OutlinedButton(
                //    onClick = {
                        // TODO: Implement video recording
                //    },
                //    modifier = Modifier
                //        .fillMaxWidth()
                //        .height(50.dp),
                //    shape = RoundedCornerShape(12.dp),
                //    colors = ButtonDefaults.outlinedButtonColors(
                //        contentColor = buttonColor
                //    )
                //) {
                //    Icon(
                //        imageVector = Icons.Default.Videocam,
                //        contentDescription = "Video",
                //        modifier = Modifier.size(20.dp)
                //    )
                //    Spacer(modifier = Modifier.width(8.dp))
                //    Text("Record Video")
                //}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caption input
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text("Caption...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = textFieldColor,
                    unfocusedContainerColor = textFieldColor,
                    disabledContainerColor = textFieldColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = buttonColor
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            if (uploadError != null) {
                Text(
                    text = uploadError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Post button
            Button(
                onClick = {
                    if (selectedMediaUri != null) {
                        isUploading = true
                        uploadError = null

                        PhotoUploadManager.uploadPhoto(context, selectedMediaUri!!) { imageUrl ->
                            isUploading = false

                            if (imageUrl != null) {
                                onPost(caption.text, imageUrl)
                                onNavigateToHome()
                            } else {
                                uploadError = "Upload failed. Check your internet connection."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(28.dp),
                enabled = selectedMediaUri != null && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Post",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F8FF)
@Composable
fun PostScreenPreview() {
    val format = SimpleDateFormat("MM/dd/yyyy")
    PostScreen(
        onNavigateToHome = {},
        onNavigateToChallenge = {},
        onNavigateToFriends = {},
        onNavigateToNotifications = {},
        onNavigationToProfile = {},
        challenge = Challenge(0,"Do 10 Jumping Jacks", format.parse("11/13/2025")!!, imageLink = "https://i.ibb.co/Hpn6Q27v/jump.jpg","Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?"),
        onPost = {s1,s2->}
    )
}