package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {

    private lateinit var changePass: Button
    private lateinit var changeEmail: Button
    private lateinit var delete : Button
    private lateinit var back : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_layout)

        changePass = findViewById(R.id.change_password_button)
        changeEmail = findViewById(R.id.change_email_button)
        delete = findViewById(R.id.delete_account_button)
        back = findViewById(R.id.back_button)

        changePass.setOnClickListener {
            val intent = Intent(this, EditPassword::class.java)
            startActivity(intent)
        }

        changePass.setOnClickListener {
            val intent = Intent(this, EditPassword::class.java)
            startActivity(intent)
        }

        delete.setOnClickListener {
            val intent = Intent(this, DeleteAccount::class.java)
            startActivity(intent)
        }

        back.setOnClickListener {
            finish()
        }
    }

}