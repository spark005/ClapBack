package com.example.clapback

import android.content.Intent
import android.icu.text.DateFormat.DAY
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
import java.lang.Long.parseLong
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDate
import java.util.*

class Login : AppCompatActivity() {


    //Lateinit allows us to initialize a non-null variable outside constructor
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: TextView
    private lateinit var forgot : TextView

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
        forgot = findViewById(R.id.forgot_password)

        editPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        // Creating signup button functionality
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
        forgot.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
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
            val lastSignIn = mAuth.currentUser?.metadata?.lastSignInTimestamp
            val now = System.currentTimeMillis()
            val lastDate = getDateTime(lastSignIn!! + 3600*1000*24)
            val currentDate = getDateTime(now)
//            Log.d("DEBUG", lastDate!!)
//            Log.d("DEBUG", currentDate!!)
            if (it.exists() && (lastDate == currentDate)) {
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

    private fun getDateTime(s: Long): String? {
        try {
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val netDate = Date(s)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

}
