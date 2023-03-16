package com.example.clapback

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class OtherUserProfile: AppCompatActivity() {

    private lateinit var mDbRef: DatabaseReference

    private lateinit var username: TextView
    private lateinit var description: TextView
    private lateinit var image: CircleImageView
    private lateinit var nickname: EditText
    private lateinit var saveBtn: Button
    private lateinit var cancelBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otherusers_profile_page)

        mDbRef = FirebaseDatabase.getInstance().getReference()

        username = findViewById(R.id.other_username)
        description = findViewById(R.id.description)
        image = findViewById(R.id.other_profile_image)
        nickname = findViewById(R.id.change_nickname)
        saveBtn = findViewById(R.id.save_btn)
        cancelBtn = findViewById(R.id.cancel_btn)

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
            username.text = otherUser.name

            if (otherUser.bio == null) {
                description.text = "No Description"
            } else {
                description.text = otherUser.bio
            }
        }.addOnFailureListener {
            Log.e("ERROR", it.toString())
        }

        saveBtn.setOnClickListener {
            val nickName = nickname.text.toString()

            if (nickName == "") {
                //If the nickname is empty, then change back to friend's name?
                Toast.makeText(this@OtherUserProfile, "Nickname is empty", Toast.LENGTH_SHORT).show()
            } else {
                changeNickname(nickName)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        cancelBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    //Display the changed nickname only to that user // if the nickname is empty then display as friend's name else display as changed nickname
    private fun changeNickname(nickName: String) {

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