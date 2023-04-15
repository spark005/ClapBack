package com.example.clapback

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await


class CustomReactions : AppCompatActivity() {

    private lateinit var add: Button
    private lateinit var save: Button
    private lateinit var newReaction: Uri

    private lateinit var reactionList: ArrayList<String>
    private lateinit var crAdapter: CustomReactionAdapter
    private lateinit var myReactions: RecyclerView
    lateinit var storage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_reactions)

        add = findViewById(R.id.addButton)
        save = findViewById(R.id.saveButton)
        val profileUid = FirebaseAuth.getInstance().currentUser?.uid
        storage = FirebaseStorage.getInstance().reference.child("reactions/$profileUid")

        reactionList = ArrayList()
        crAdapter = CustomReactionAdapter(this, reactionList,storage)

        myReactions = findViewById(R.id.reactionRecyclerView)
        myReactions.adapter = crAdapter

        updateReactions()

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

                    updateReactions()
                }
            }

            builder.setNegativeButton("Cancel"){ dialog, which ->
                dialog.cancel()
            }

            builder.show()
        }
    }

    //Used to updated the users reactions Recyclerview
    private fun updateReactions() {
        reactionList.clear()

        //get all stored reactions and add them to the reactionList.
        //Its not instantaneous so added an onComplete
        storage.listAll().addOnCompleteListener {
            for (react in it.getResult().items) {
                reactionList.add(react.name)
                crAdapter.notifyDataSetChanged()
            }

            crAdapter.notifyDataSetChanged()
        }

    }
}