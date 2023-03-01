package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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