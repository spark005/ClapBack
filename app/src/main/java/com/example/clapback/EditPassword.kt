package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class EditPassword : AppCompatActivity() {

    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var change: Button
    private lateinit var cancel: TextView

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_password)

        // Brief line to remove action bar
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()

        oldPassword = findViewById(R.id.old_password)
        newPassword = findViewById(R.id.new_password)
        confirmPassword = findViewById(R.id.confirm_password)
        change = findViewById(R.id.change_btn)
        cancel = findViewById(R.id.cancel_btn)

        oldPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        newPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        confirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        change.setOnClickListener {
            val password = oldPassword.text.toString()

            if (password == "") {
                Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
            } else {
                //mAuth.confirmPasswordReset()
                //something here
            }
        }

        cancel.setOnClickListener() {
            //val intent = Intent(this, Settings::class.java)
            finish()
            //startActivity(intent)
        }

    }
}