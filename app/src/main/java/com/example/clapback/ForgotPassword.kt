package com.example.clapback

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPassword : AppCompatActivity() {
    private lateinit var enterEmail: EditText
    private lateinit var sendButton: Button
    private lateinit var returnLogin : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        returnLogin = findViewById(R.id.back_to_login)
        enterEmail = findViewById(R.id.EmailAddress)
        sendButton = findViewById(R.id.send_button)


        returnLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Functionality for resetting password
        sendButton.setOnClickListener {
            val email = enterEmail.text.toString()

            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                    } else {
                        Toast.makeText(this@ForgotPassword, "ERROR: Email couldn't be sent!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}