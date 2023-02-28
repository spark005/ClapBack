package com.example.clapback

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FriendRequest : AppCompatActivity() {

    private lateinit var sendRequestBtn: Button
    private lateinit var usernameField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        usernameField = findViewById(R.id.search_user)
        sendRequestBtn = findViewById(R.id.send_request)

        sendRequestBtn.setOnClickListener {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}