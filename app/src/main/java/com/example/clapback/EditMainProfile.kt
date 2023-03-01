package com.example.clapback

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditMainProfile : AppCompatActivity() {

    private lateinit var cancel: TextView
    private lateinit var confirm: ImageView
    private lateinit var changePic: TextView
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var bio: EditText
    private lateinit var image: CircleImageView
    private lateinit var newPic: Uri

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    
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
        image = findViewById(R.id.profile_image)

        val profileUid = FirebaseAuth.getInstance().currentUser?.uid
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/$profileUid")

        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(pic.absolutePath)
            image.setImageBitmap(bitmap)

        }.addOnFailureListener{
            image.setImageResource(R.drawable.mongle)
        }

        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                image.setImageURI(result.data?.data)
                newPic = result.data?.data!!

                val store = FirebaseStorage.getInstance().getReference("profilePic/$profileUid")

                store.putFile(newPic)
            }
        }
        changePic.setOnClickListener() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            intent.action = Intent.ACTION_GET_CONTENT
            getPic.launch(intent)
        }

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        username.setText(mAuth.currentUser!!.displayName)

        cancel.setOnClickListener() {
            val intent = Intent(this, ProfilePage::class.java)
            finish()
            startActivity(intent)
        }
        confirm.setOnClickListener() {
            val newName = username.text.toString()
            changeNameOfUser(newName)
            val intent = Intent(this, ProfilePage::class.java)
            finish()
            startActivity(intent)
        }
    }

    private fun changeNameOfUser(name:String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(mAuth.currentUser!!.uid).child("name").setValue(name)
            .addOnSuccessListener {
                val toast = Toast.makeText(applicationContext, "Name changed successfully", Toast.LENGTH_SHORT)
                toast.show()
            }
            .addOnFailureListener {
                val toast = Toast.makeText(applicationContext, "Failed to change name", Toast.LENGTH_SHORT)
                toast.show()
            }
    }

}