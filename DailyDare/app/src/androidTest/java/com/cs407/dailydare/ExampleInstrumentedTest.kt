package com.cs407.dailydare

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cs407.dailydare.data.Challenge
import com.cs407.dailydare.data.UserState
import com.cs407.dailydare.data.firestoreUser
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.text.SimpleDateFormat

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.cs407.dailydare", appContext.packageName)
    }

    @Test
    fun addChallenges(){


        // Sample user
        val format = SimpleDateFormat("yyyy-MM-dd")

        val c1 = Challenge(20251031, "Do 10 jumping jacks in a funny place", format.parse("2025-10-31")!!, R.drawable.wireframe,"Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?")
        val c2 = Challenge(20251030, "Recreate a famous movie scene", format.parse("2025-10-30")!!, R.drawable.wireframe, "Pick an iconic moment from any film and bring it to life your own way. Props, costumes, or pure creativity—anything goes.")
        val c3 = Challenge(20251029, "Build a pillow fort", format.parse("2025-10-29")!!, R.drawable.wireframe,"Use pillows, blankets, and whatever else you can find to engineer the coziest fort possible. Snap a photo of your masterpiece.")
        val c4 = Challenge(20251115, "Try a new hobby for 1 hour",format.parse("2025-11-15")!!, R.drawable.wireframe,"“Choose something you’ve never tried before and dive in for a full hour. Capture a moment that shows what you explored and learned.”")

        val db = Firebase.firestore

        val tasks = listOf(
            db.collection("Challenge").document(c1.id.toString()).set(c1),
            db.collection("Challenge").document(c2.id.toString()).set(c2),
            db.collection("Challenge").document(c3.id.toString()).set(c3),
            db.collection("Challenge").document(c4.id.toString()).set(c4)
        )

        // This blocks the test thread until all writes complete or throw
        Tasks.whenAllComplete(tasks).let { allTask ->
            Tasks.await(allTask) // Will throw if any write failed
        }
        assertEquals(20251031,c1.id)
    }

    @Test
    fun addUsers(){

        val sampleUser = firestoreUser(
            uid = "Sample2",
            userName = "Mr Beast",
            userHandle = "@mrbeast",
            streakCount = 7,
            completedCount = 1,
            completedChallengeRef = listOf("Challenge/20251031"),
            profilePicture = ""
        )
        // Sample user

        val db = Firebase.firestore

        val tasks = listOf(
            db.collection("users").document(sampleUser.uid).set(sampleUser)
        )

        // This blocks the test thread until all writes complete or throw
        Tasks.whenAllComplete(tasks).let { allTask ->
            Tasks.await(allTask) // Will throw if any write failed
        }
        assertEquals("Sample2",sampleUser.uid)
    }

    @Test
    fun TestRef(){
        val db = Firebase.firestore
        val snapshot = Tasks.await(db.document("users/SAMPLEUSER").get())
        val chal = snapshot.toString()
        assertTrue(chal,chal.contains("20251031"))

    }
}