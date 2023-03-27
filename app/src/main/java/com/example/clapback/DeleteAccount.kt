package com.example.clapback

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DeleteAccount : AppCompatActivity() {

    private lateinit var cancel : TextView
    private lateinit var delete : Button
    private lateinit var password : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete_account)
    }
}