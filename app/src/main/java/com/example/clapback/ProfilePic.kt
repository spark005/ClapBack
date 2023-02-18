package com.example.mymessageapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.clapback.R

private lateinit var button: Button
private lateinit var profilePic: ImageView

class ProfilePic : AppCompatActivity() {
    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_pic)

        button = findViewById(R.id.selectbutton)
        profilePic = findViewById(R.id.profilepic)

        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                profilePic.setImageURI(result.data?.data)
            }
        }

        button.setOnClickListener {
            print("we made it")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            intent.action = Intent.ACTION_GET_CONTENT
            getPic.launch(intent)
            //startActivityForResult(intent, IMAGE_REQUEST_CODE)
            //TODO look into this registerForActivityResult(intent, IMAGE_REQUEST_CODE)

        }
    }
}