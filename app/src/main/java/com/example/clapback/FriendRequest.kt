package com.example.clapback

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FriendRequest : AppCompatActivity() {

    private lateinit var sendRequestBtn: Button
    private lateinit var usernameField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        usernameField = findViewById(R.id.search_user)
        sendRequestBtn = findViewById(R.id.send_request)

        // TODO placeholder user info
        val currentUser = User()

        lateinit var mDbRef: DatabaseReference

        // Allows user to send friend request
        sendRequestBtn.setOnClickListener {

            // Error checking for no user entered

            mDbRef = FirebaseDatabase.getInstance().getReference()
            val searchedEmail = usernameField.text.toString()
            mDbRef.child("user").child(searchedEmail).get().addOnSuccessListener {
                if (it.exists()) {
                    val foundFriend = it.getValue(User::class.java)

                    // If friend request was already sent
                    if (foundFriend?.friendRequests?.contains(FriendR(currentUser.uid, foundFriend.uid)) == true) {
                        Toast.makeText(this, "Friend request already sent", Toast.LENGTH_SHORT).show()
                    } else {
                        foundFriend?.friendRequests?.add(FriendR(currentUser.uid, foundFriend.uid))
                        // Uploading friend requests to database
                        foundFriend?.uid?.let { ffuid -> mDbRef.child(ffuid).child("friendRequests")
                            .setValue(foundFriend.friendRequests)}

                    }



                }

            } .addOnFailureListener {
                // If user not found
                Toast.makeText(this, "User Not Found", Toast.LENGTH_SHORT).show()
            }





        }
    }
}