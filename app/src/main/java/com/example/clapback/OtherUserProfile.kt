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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class OtherUserProfile: AppCompatActivity() {

    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var username: TextView
    private lateinit var description: TextView
    private lateinit var social: TextView
    private lateinit var image: CircleImageView
    private lateinit var nickname: EditText
    private lateinit var saveBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var reportBtn: Button
    private lateinit var removeFriend: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otherusers_profile_page)

        supportActionBar?.hide()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()

        username = findViewById(R.id.other_username)
        description = findViewById(R.id.description)
        social = findViewById(R.id.social)
        image = findViewById(R.id.other_profile_image)
        nickname = findViewById(R.id.change_nickname)
        saveBtn = findViewById(R.id.save_btn)
        cancelBtn = findViewById(R.id.cancel_btn)
        reportBtn = findViewById(R.id.report_btn)
        removeFriend = findViewById(R.id.remove_friend)

        val otherUserUid = intent.getStringExtra("uid")
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/$otherUserUid")

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val nickName = mDbRef.child("user").child(currentUserUid!!).child("friendlist_nickname").child(otherUserUid!!).child("nickname")

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

            nickName.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val nickName = task.result?.value as? String
                    if (!nickName.isNullOrEmpty()) {
                        username.text = nickName + " ("+ otherUser.name +")"
                    } else {
                        username.text = otherUser.name
                    }
                }
            }

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

        saveBtn.setOnClickListener {
            val nickNameGet = nickname.text.toString()

            if (nickNameGet == "") {
                //If the nickname is empty, then change back to friend's name
                changeNickname(otherUser.name.toString())
            } else {
                changeNickname(nickNameGet)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        cancelBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        reportBtn.setOnClickListener {
            val intent = Intent(this, Report::class.java).apply {
                putExtra("uid", otherUserUid)
            }
            startActivity(intent)
        }

        removeFriend.setOnClickListener {
            val friendName = otherUser.name.toString()
            var currentUser = User()
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to remove $friendName as a friend?")
                .setCancelable(false)
                .setPositiveButton("Remove") { dialog, id ->
                    // Delete selected note from database

                    otherUser.friendlist.remove(currentUserUid)
                    currentUser.friendlist.remove(otherUserUid)
                    mDbRef.child("user").child(currentUserUid).child("friendlist").setValue(currentUser.friendlist)
                    mDbRef.child("user").child(otherUserUid).child("friendlist").setValue(otherUser.friendlist)
                    mDbRef.child("chats").child(currentUserUid + otherUserUid).removeValue()
                    mDbRef.child("chats").child(otherUserUid + currentUserUid).removeValue()

                    Log.d("Remove F", "$currentUserUid removing $otherUserUid")
                    val intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                    //TODO("This doesnt work i think lol")
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    //Display the changed nickname only to that user // if the nickname is empty then display as friend's name else display as changed nickname
    private fun changeNickname(nickName: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val friendUid = intent.getStringExtra("uid")

        //Save the name under current user -> friend list -> friend uid -> nickname
        mDbRef.child("user").child(currentUserUid!!).child("friendlist_nickname").child(friendUid!!).child("nickname").setValue(nickName)

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