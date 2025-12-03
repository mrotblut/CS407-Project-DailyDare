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

        val c1 = Challenge(
            20251202,
            "Do 10 jumping jacks in a funny place",
            format.parse("2025-12-02")!!,
            "https://i.ibb.co/Hpn6Q27v/jump.jpg",
            "Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?"
        )
        val c2 = Challenge(
            20251203,
            "Recreate a famous movie scene",
            format.parse("2025-12-03")!!,
            "https://i.ibb.co/Hpn6Q27v/jump.jpg",
            "Pick an iconic moment from any film and bring it to life your own way. Props, costumes, or pure creativity—anything goes."
        )
        val c3 = Challenge(
            20251204,
            "Build a pillow fort",
            format.parse("2025-12-04")!!,
            "https://spcdn.shortpixel.ai/spio/ret_img,q_cdnize/www.drewandjonathan.com/wp-content/uploads/2018/11/pillow-fort-living-room.jpg",
            "Use pillows, blankets, and whatever else you can find to engineer the coziest fort possible. Snap a photo of your masterpiece."
        )
        val c4 = Challenge(
            20251205,
            "Try a new hobby for 1 hour",
            format.parse("2025-12-05")!!,
            "https://centralca.cdn-anvilcms.net/media/images/2021/11/24/images/Ideal_Hobbies_pix_11-24-21.max-2400x1350.jpg",
            "“Choose something you’ve never tried before and dive in for a full hour. Capture a moment that shows what you explored and learned.”"
        )
        val c11 = Challenge(
            20251206,
            "Do 10 jumping jacks in a funny place",
            format.parse("2025-12-06")!!,
            "https://i.ibb.co/Hpn6Q27v/jump.jpg",
            "Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?"
        )
        val c12 = Challenge(
            20251207,
            "Recreate a famous movie scene",
            format.parse("2025-12-07")!!,
            "https://i.ibb.co/Hpn6Q27v/jump.jpg",
            "Pick an iconic moment from any film and bring it to life your own way. Props, costumes, or pure creativity—anything goes."
        )
        val c13 = Challenge(
            20251208,
            "Build a pillow fort",
            format.parse("2025-12-08")!!,
            "https://spcdn.shortpixel.ai/spio/ret_img,q_cdnize/www.drewandjonathan.com/wp-content/uploads/2018/11/pillow-fort-living-room.jpg",
            "Use pillows, blankets, and whatever else you can find to engineer the coziest fort possible. Snap a photo of your masterpiece."
        )
        val c14 = Challenge(
            20251209,
            "Try a new hobby for 1 hour",
            format.parse("2025-12-09")!!,
            "https://centralca.cdn-anvilcms.net/media/images/2021/11/24/images/Ideal_Hobbies_pix_11-24-21.max-2400x1350.jpg",
            "“Choose something you’ve never tried before and dive in for a full hour. Capture a moment that shows what you explored and learned.”"
        )
        val c21 = Challenge(
            20251210,
            "Do 10 jumping jacks in a funny place",
            format.parse("2025-12-10")!!,
            "https://i.ibb.co/Hpn6Q27v/jump.jpg",
            "Let's get moving! Show us your best jumping jacks form. How many can you do in 30 seconds?"
        )
        val c22 = Challenge(
            20251211,
            "Recreate a famous movie scene",
            format.parse("2025-12-11")!!,
            "https://i.ibb.co/Hpn6Q27v/jump.jpg",
            "Pick an iconic moment from any film and bring it to life your own way. Props, costumes, or pure creativity—anything goes."
        )
        val c23 = Challenge(
            20251212,
            "Build a pillow fort",
            format.parse("2025-12-12")!!,
            "https://spcdn.shortpixel.ai/spio/ret_img,q_cdnize/www.drewandjonathan.com/wp-content/uploads/2018/11/pillow-fort-living-room.jpg",
            "Use pillows, blankets, and whatever else you can find to engineer the coziest fort possible. Snap a photo of your masterpiece."
        )
        val c24 = Challenge(
            20251213,
            "Try a new hobby for 1 hour",
            format.parse("2025-12-13")!!,
            "https://centralca.cdn-anvilcms.net/media/images/2021/11/24/images/Ideal_Hobbies_pix_11-24-21.max-2400x1350.jpg",
            "“Choose something you’ve never tried before and dive in for a full hour. Capture a moment that shows what you explored and learned.”"
        )

        val db = Firebase.firestore

        val tasks = listOf(
            db.collection("Challenge").document(c1.id.toString()).set(c1),
            db.collection("Challenge").document(c2.id.toString()).set(c2),
            db.collection("Challenge").document(c3.id.toString()).set(c3),
            db.collection("Challenge").document(c4.id.toString()).set(c4),
            db.collection("Challenge").document(c11.id.toString()).set(c11),
            db.collection("Challenge").document(c12.id.toString()).set(c12),
            db.collection("Challenge").document(c13.id.toString()).set(c13),
            db.collection("Challenge").document(c14.id.toString()).set(c14),
            db.collection("Challenge").document(c21.id.toString()).set(c21),
            db.collection("Challenge").document(c22.id.toString()).set(c22),
            db.collection("Challenge").document(c23.id.toString()).set(c23),
            db.collection("Challenge").document(c24.id.toString()).set(c24)
        )

        // This blocks the test thread until all writes complete or throw
        Tasks.whenAllComplete(tasks).let { allTask ->
            Tasks.await(allTask) // Will throw if any write failed
        }
        assertEquals(20251202,c1.id)
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