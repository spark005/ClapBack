package com.example.clapback

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject
import java.io.File

/**
 * This class is for when the user search all the users and opens the profile page. Made a new one that dosen't contain report, change nickname, etc
 */
class OtherUserProfileNotFriend: AppCompatActivity() {

    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var username: TextView
    private lateinit var description: TextView
    private lateinit var social: TextView
    private lateinit var image: CircleImageView
    private lateinit var cancelBtn: Button
    private lateinit var addFriendBtn: Button
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otheruser_profile_not_friend)

        supportActionBar?.hide()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()

        username = findViewById(R.id.other_username)
        description = findViewById(R.id.description)
        social = findViewById(R.id.social)
        image = findViewById(R.id.other_profile_image)
        cancelBtn = findViewById(R.id.cancel_btn)
        addFriendBtn = findViewById(R.id.addFriend_btn)

        val currentUserUid = mAuth.currentUser?.uid
        if (currentUserUid != null) {
            mDbRef.child("user").child(currentUserUid).get().addOnSuccessListener { snapshot ->
                currentUser = snapshot.getValue(User::class.java)!!
            }.addOnFailureListener { exception ->
                // handle error
            }
        }

        val otherUserUid = intent.getStringExtra("uid")
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/$otherUserUid")

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

        var otherUser = User()
        mDbRef.child("user").child(otherUserUid!!).get().addOnSuccessListener {
            otherUser = it.getValue(User::class.java)!!

            if (otherUser.bio == "") {
                description.text = getString(R.string.default_bio)
            } else {
                description.setText(otherUser.bio + " | " + otherUser.fmovie + " | " +
                        otherUser.fmusic + " | " + otherUser.fbook).toString()
            }

            if (otherUser.social == "") {
                social.text = "No Social Media"
            } else {
                social.text = otherUser.social
            }
        }.addOnFailureListener {
            Log.e("ERROR", it.toString())
        }

        cancelBtn.setOnClickListener {
            val intent = Intent(this, SearchOtherUsers::class.java)
            startActivity(intent)
        }

        addFriendBtn.setOnClickListener {
            mDbRef.child("user").child(otherUserUid!!).get().addOnSuccessListener {
                otherUser = it.getValue(User::class.java)!!
                val intent = Intent(this, FriendRequest::class.java).apply {
                    putExtra("OtherUserProfileNotFriend_email", otherUser.email)
                }
                startActivity(intent)
                Toast.makeText(this@OtherUserProfileNotFriend, "Press Send Friend Request!", Toast.LENGTH_SHORT).show()
            }
        }
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