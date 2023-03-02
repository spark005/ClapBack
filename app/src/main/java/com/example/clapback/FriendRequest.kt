package com.example.clapback

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendRequest : AppCompatActivity() {

    private lateinit var sendRequestBtn: Button
    private lateinit var usernameField: EditText
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        usernameField = findViewById(R.id.search_user)
        sendRequestBtn = findViewById(R.id.send_request)

        // Grabbing current logged in user
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        val currentUserUID = mAuth.currentUser?.uid
        if (currentUserUID != null) {

            mDbRef.child("user").child(currentUserUID).get().addOnSuccessListener {

                currentUser = it.getValue(User::class.java)!!

            }

        } else {

            // If this occurs, BIG ERROR HAS OCCURRED PLEASE FIX
            Toast.makeText(this, "**CANNOT FIND CURRENT USER**", Toast.LENGTH_SHORT).show()
            println(currentUser.toString())

        }





        // Allows user to send friend request
        sendRequestBtn.setOnClickListener {

            // Error checking for no user entered
            if (usernameField.text.toString().equals("")) {
                Toast.makeText(this, "Nothing Entered In Name Field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val searchedEmail = usernameField.text.toString()
            var searchUID = "Nothing"

            // Linearly traversing users to see if a user exists with the given email
            mDbRef.child("user").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for(postSnapshot in snapshot.children) {

                        val traversedUser = postSnapshot.getValue(User::class.java)

                        if (traversedUser?.email.equals(searchedEmail)) {
                            searchUID = traversedUser!!.uid.toString()
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            // If the user does not exist
            if (searchUID.equals("Nothing")) {
                // If user not found
                Toast.makeText(this, "User Not Found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Grabbing searched user from database
            mDbRef.child("user").child(searchUID).get().addOnSuccessListener {
                if (it.exists()) {
                    val foundFriend = it.getValue(User::class.java)

                    // Error handler if getValue doesn't "populate" the foundFriend user object
                    // **For Debugging Purposes**
                    if (foundFriend == null) {

                        Toast.makeText(this, "**BIG ERROR HERE FIX**", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // If friend request was already sent before
                    if (foundFriend.friendRequests.contains(FriendR(currentUser.uid, foundFriend.uid, true))) {

                        Toast.makeText(this, "Friend request already sent", Toast.LENGTH_SHORT).show()

                    } else {

                        // Adding the request to both user's request list
                        foundFriend.friendRequests.add(FriendR(currentUser.uid, foundFriend.uid, true))
                        currentUser.friendRequests.add(FriendR(currentUser.uid,
                            foundFriend.uid, false))

                        // Uploading friend requests to database of both parties
                        foundFriend.uid?.let { ffuid -> mDbRef.child(ffuid).child("friendRequests")
                            .setValue(foundFriend.friendRequests)}
                        currentUser.uid?.let { cuuid -> mDbRef.child(cuuid).child("friendRequests")
                            .setValue(currentUser.friendRequests)}

                    }
                }

            } .addOnFailureListener {
                // If user not found
                Toast.makeText(this, "User Not In Database", Toast.LENGTH_SHORT).show()
            }





        }
    }
}