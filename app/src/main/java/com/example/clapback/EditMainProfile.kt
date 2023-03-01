package com.example.clapback

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class EditMainProfile : AppCompatActivity() {

    private lateinit var cancel: TextView
    private lateinit var confirm: ImageView
    private lateinit var changePic: TextView
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var bio: EditText
    private lateinit var image: CircleImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editmain_profile)

        // Brief line to remove action bar
        supportActionBar?.hide()

        cancel = findViewById(R.id.cancel)
        confirm = findViewById(R.id.confirm)
        changePic = findViewById(R.id.change_profile)
        name = findViewById(R.id.name)
        username = findViewById(R.id.username)
        bio = findViewById(R.id.bio)
        val profileUid = FirebaseAuth.getInstance().currentUser?.uid
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/$profileUid")

        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(pic.absolutePath)
            image.setImageBitmap(bitmap)

        }.addOnFailureListener{
            image.setImageResource(R.drawable.mongle)
        }

        changePic.setOnClickListener() {

        }

        cancel.setOnClickListener() {
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }

    }
}