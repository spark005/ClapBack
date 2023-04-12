package com.example.clapback

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class CustomReactions : AppCompatActivity() {

    private lateinit var add: Button
    private lateinit var newReaction: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_reactions)

        add = findViewById(R.id.addButton)

        //FUNCTION FOR WHEN YOU SUCCESSFULLY GRAB AN IMAGE
        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                //SET RECEIVE REACTION EXAMPLE
                val reactionBox = findViewById<RelativeLayout>(R.id.reactionBoxEX)
                reactionBox.setVisibility(View.VISIBLE)
                val reaction = findViewById<ImageView>(R.id.reactionEX)
                reaction.setImageURI(result.data?.data)

                //SET RECEIVE REACTION EXAMPLE
                val reactionBoxS = findViewById<RelativeLayout>(R.id.reactionBoxSEX)
                reactionBoxS.setVisibility(View.VISIBLE)
                val reactionS = findViewById<ImageView>(R.id.reactionSEX)
                reactionS.setImageURI(result.data?.data)

                newReaction = result.data?.data!!
            }
        }

        add.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            //show and store pic
            intent.action = Intent.ACTION_GET_CONTENT
            getPic.launch(intent)

        }
    }

}