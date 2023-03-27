package com.example.clapback

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ProfilePage : AppCompatActivity(), OnSwipeListener {

    private lateinit var friends: CardView
    private lateinit var chat: CardView
    private lateinit var settings: CardView
    private lateinit var bio: CardView
    private lateinit var requests: CardView
    private lateinit var notifications: CardView
    private lateinit var edit: Button
    private lateinit var detector: GestureDetectorCompat
    private lateinit var image: CircleImageView
    private lateinit var mDbRef: DatabaseReference

    // Username's set parameters on profile page
    private lateinit var userBio: TextView
    private lateinit var username: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile_page)

        // Brief line to remove action bar
        supportActionBar?.hide()

        friends = findViewById(R.id.friends)
        chat = findViewById(R.id.chat)
        settings = findViewById(R.id.settings)
        bio = findViewById(R.id.bio)
        requests = findViewById(R.id.requests)
        notifications = findViewById(R.id.notifications)
        detector = GestureDetectorCompat(this, DiaryGestureListener(this))
        edit = findViewById(R.id.edit)
        image = findViewById(R.id.profile_image)

        // Text fields on user profile page
        userBio = findViewById(R.id.name)
        username = findViewById(R.id.username)

        val profileUid = FirebaseAuth.getInstance().currentUser?.uid
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/$profileUid")


        mDbRef = FirebaseDatabase.getInstance().getReference()



        // Setting the profile picture's username and Bio fields
        if (profileUid != null) {
            mDbRef.child("user").child(profileUid).get().addOnSuccessListener {
                val currentUser = it.getValue(User::class.java)

                if (!currentUser?.bio.equals("")) {
                    userBio.setText(currentUser?.bio + "|" + currentUser?.fmovie + "|" +
                            currentUser?.fmusic + "|" + currentUser?.fbook).toString()
                } else {
                    userBio.setText("Bio").toString()
                }

                username.setText(currentUser?.name).toString()
            }
        }




        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap: Bitmap =
                modifyOrientation(
                    BitmapFactory.decodeFile(pic.absolutePath),
                    pic.absolutePath
                )
            image.setImageBitmap(bitmap)

        }.addOnFailureListener{
            image.setImageResource(R.drawable.pfp)
        }

        settings.setOnClickListener() {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        edit.setOnClickListener() {
            val intent = Intent(this, EditMainProfile::class.java)
            startActivity(intent)
        }

        requests.setOnClickListener {
            val intent = Intent(this, FriendRequest::class.java)
            startActivity(intent)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }


    inner class DiaryGestureListener(private val onSwipeListener: OnSwipeListener) : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent) {
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1.x < e2.x) {
                onSwipeListener.onSwipeRight()
            }
            return true
        }
    }

    override fun onSwipeLeft() {
        TODO("Not yet implemented")
    }


    override fun onSwipeRight() {
        val intent = Intent(this, Time::class.java)
        startActivity(intent)
        this.overridePendingTransition(R.anim.swipe_screen_right,
            R.anim.swipe_screen_left)
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.swipe_screen_right2,
            R.anim.swipe_screen_left2)
    }


    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei: ExifInterface = ExifInterface(image_absolute_path);
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                return rotate(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                return rotate(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                return rotate(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                return rotate(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                return flip(bitmap, true, vertical = false)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                return flip(bitmap, false, vertical = true)
            }
            else -> {
                return bitmap
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) (-1f) else 1f, if (vertical) (-1f) else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true);
    }



}