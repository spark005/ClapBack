package com.example.clapback

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EditEmail : AppCompatActivity() {
    private lateinit var newEmail: EditText
    private lateinit var change: Button
    private lateinit var cancel: TextView


    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_email)

        // Brief line to remove action bar
        supportActionBar?.hide()

        newEmail = findViewById(R.id.email)
        cancel = findViewById(R.id.cancel)

        cancel.setOnClickListener() {
            //val intent = Intent(this, Settings::class.java)
            finish()
            //startActivity(intent)
        }

    }
}