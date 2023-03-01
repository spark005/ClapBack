package com.example.clapback

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


private lateinit var select: Button
private lateinit var profilePic: ImageView
private lateinit var confirm: Button
private lateinit var image: Uri

class ProfilePic : AppCompatActivity() {
    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

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
            //startActivityForResult(intent, IMAGE_REQUEST_CODE)
            //TODO look into this registerForActivityResult(intent, IMAGE_REQUEST_CODE)

        }

        confirm.setOnClickListener {
            if (profilePic.drawable == null){
                val warning = AlertDialog.Builder(this)
                warning.setTitle("Are you sure you want to continue without a Profile pic?")
                warning.setMessage("Don't worry. You can always add or change one in Profile Settings")

                warning.setPositiveButton("Yep") { dialog, which ->
                    profilePic.setImageResource(R.drawable.mongle)
                    //Should go to MainAcitivity since new user shouldn't have a cb . Same for other one
                    //TODO fix with pic upload
                    val intent = Intent(this@ProfilePic, MainActivity::class.java)
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

                val intent = Intent(this@ProfilePic, MainActivity::class.java)
                finish()
                startActivity(intent)
            }
        }
    }
}