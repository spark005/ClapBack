package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class DeleteAccount : AppCompatActivity() {




    private lateinit var cancel : TextView
    private lateinit var delete : Button
    private lateinit var password : EditText

    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete_account)


        // TODO Luke, this whole class is washed, delete later


        // Setting buttons
        cancel = findViewById(R.id.cancel)
        delete = findViewById(R.id.confirm_delete)
        password = findViewById(R.id.password)

        // Grabbing current logged in user email and entered password
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        val user = Firebase.auth.currentUser!!
        val userEmail = user.email.toString()
        val password = password.toString()

        // Button to delete profile
        delete.setOnClickListener {


            /*val credential = EmailAuthProvider
                .getCredential(userEmail, password)

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                .addOnFailureListener { return@addOnFailureListener }
                .addOnCompleteListener { Log.d("This is a Tag", "User re-authenticated.") }*/


            // Deleting user
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("This is a Tag", "User account deleted.")
                    } else {
                        Toast.makeText(
                            this@DeleteAccount,
                            "ERROR: Account couldn't be deleted!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            //deleteUserFromDatabase(user, mDbRef)
            mDbRef.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (postSnapshot in snapshot.children) {
                        val foundFriend = postSnapshot.getValue(User::class.java)

                        if (foundFriend?.uid.equals(user.uid)) {
                            postSnapshot.ref.removeValue()
                            /*Toast.makeText(
                                this@Settings,
                                "ERROR: Account couldn't be deleted!",
                                Toast.LENGTH_SHORT
                            ).show()*/
                            break
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


            // Signing out user
            mAuth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Sends user back to settings page
        cancel.setOnClickListener() {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }

}