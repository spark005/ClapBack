package com.example.clapback

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream


private lateinit var select: Button
private lateinit var profilePic: ImageView
private lateinit var confirm: Button
private lateinit var image: Uri

class ProfilePic : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_pic)

        select = findViewById(R.id.selectbutton)
        profilePic = findViewById(R.id.profilepic)
        confirm = findViewById(R.id.confirmbutton)
        val profileUid = FirebaseAuth.getInstance().currentUser?.uid

        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                profilePic.setImageURI(result.data?.data)
                image = result.data?.data!!
            }
        }

        select.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            intent.action = Intent.ACTION_GET_CONTENT
            getPic.launch(intent)

        }

        confirm.setOnClickListener {
            if (profilePic.drawable == null){
                val warning = AlertDialog.Builder(this)
                warning.setTitle("Are you sure you want to continue without a Profile pic?")
                warning.setMessage("Don't worry. You can always add or change one in Profile Settings")

                warning.setPositiveButton("Yep") { dialog, which ->
                    profilePic.setImageResource(R.drawable.mongle)
                    //image = Uri.parse("android.resource://res/drawable/mongle.png")

                    val intent = Intent(this@ProfilePic, WalkThrough::class.java)

                    val store = FirebaseStorage.getInstance().getReference("profilePic/$profileUid")

                    val updates = store.putFile(image)

                    finish()
                    startActivity(intent)
                }

                warning.setNegativeButton("Nah") { dialog, which ->
                    return@setNegativeButton
                }
                warning.show()
            } else {
                val store = FirebaseStorage.getInstance().getReference("profilePic/$profileUid")

                store.putFile(image)
                
                val intent = Intent(this@ProfilePic, WalkThrough::class.java)
                finish()
                startActivity(intent)
            }
        }
    }
}