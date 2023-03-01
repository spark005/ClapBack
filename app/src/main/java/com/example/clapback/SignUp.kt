package com.example.clapback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {


    //TODO, figure out what lateinit var means in kotlin

    // Text fields
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnSignUp: Button

    // Networking stuff
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Brief line to remove action bar
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()

        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        btnSignUp = findViewById(R.id.signup_button)

        editPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        btnSignUp.setOnClickListener {
            val name = editName.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (email == "" || password == "" || name == "") {
                Toast.makeText(this@SignUp, "Check your name, email, or password", Toast.LENGTH_SHORT).show()
            } else {
                signUp(name, email,password)
            }
        }
    }

    // Function to signup
    // TODO add scenario where user already exists
    private fun signUp(name: String, email: String, password: String) {
        // logic for creating a new user
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If sign in is successful, jump to home
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!)
                    val intent = Intent(this@SignUp, ProfilePic::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    if (password.length < 6) {
                        Toast.makeText(this@SignUp, "Password should be 6 letters or longer", Toast.LENGTH_SHORT).show()
                    }
                    if (!email.contains("@") || email.substring(0) == "@" || !email.contains(".") ||
                            email.substring(email.lastIndex) == ".") {
                        Toast.makeText(this@SignUp, "Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    // Adding user to database
    private fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef =FirebaseDatabase.getInstance().getReference()
        // Initializing empty friendlist and request list to save into database
        val friendlist = ArrayList<String>()
        val friendRequests = ArrayList<FriendR>()
        //friendlist.add("bitneqvCvdOl4fVxGmI4KIih6I43")
        mDbRef.child("user").child(uid).setValue(User(name, email, uid, friendlist, friendRequests))
    }
}