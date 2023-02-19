package com.example.clapback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.snapshots

class EditProfile : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        // Brief line to remove action bar
        supportActionBar?.hide()

        // Initializing firebase authentication
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        btnSave = findViewById(R.id.save_button)
        btnCancel = findViewById(R.id.cancel_button)

        editPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        editEmail.setText(mAuth.currentUser!!.email)
        editName.setText(mAuth.currentUser!!.displayName)

        btnSave.setOnClickListener {
            val newName = editName.text.toString()
            changeNameOfUser(newName)
            val intent = Intent(this@EditProfile, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this@EditProfile, MainActivity::class.java)
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