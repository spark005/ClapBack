package com.example.clapback

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


class CustomReactions : AppCompatActivity() {

    private lateinit var add: Button
    private lateinit var save: Button
    private lateinit var newReaction: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_reactions)

        add = findViewById(R.id.addButton)
        save = findViewById(R.id.saveButton)
        val profileUid = FirebaseAuth.getInstance().currentUser?.uid

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

                save.visibility = View.VISIBLE
            }
        }


        add.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            //show and store pic
            intent.action = Intent.ACTION_GET_CONTENT
            getPic.launch(intent)

        }

        save.setOnClickListener {
            //Name dialoge
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Title")

            val input = EditText(this)
            builder.setView(input)

            //Save
            builder.setPositiveButton("OK"){ dialog, which ->
                if (input.text.isNotEmpty()) {
                    val name = input.text.toString()

                    val store = FirebaseStorage.getInstance().getReference("reactions/$profileUid/$name")

                    store.putFile(newReaction)
                    dialog.cancel()
                    save.visibility = View.GONE
                }
            }

            builder.setNegativeButton("Cancel"){ dialog, which ->
                dialog.cancel()
            }

            builder.show()
        }
    }

}