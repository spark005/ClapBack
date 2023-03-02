package com.example.clapback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    //TODO, figure out what lateinit var means in kotlin
    //Lateinit allows us to initialize a non-null variable outside constructor
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Brief line to remove action bar
        supportActionBar?.hide()

        // Initializing firebase authentication
        mAuth = FirebaseAuth.getInstance()

        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        btnLogin = findViewById(R.id.login_button)
        btnSignUp = findViewById(R.id.signup_link)

        editPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        // Creating signup button functionality
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (email == "" || password == "") {
                Toast.makeText(this@Login, "User does not exist", Toast.LENGTH_SHORT).show()
            } else {
                login(email,password)
            }
        }
    }

    private fun login(email: String, password: String) {
        // logic for logging in user

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, logs in user
                    val uid = task.result.user!!.uid
                    val intent = Intent(this@Login, Time::class.java)
                    incrementStreak(uid)
                    finish()
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@Login, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun incrementStreak(uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val user = it.getValue(User::class.java)
                var streak = user?.streak!!
                streak += 1
                Log.i("STREAK INFO", streak.toString())
                mDbRef.child("user").child(uid).child("streak").setValue(streak)
            }
        }.addOnFailureListener {
            Log.e("STREAK ERROR", "Could not increment streak!")
        }
    }

}
