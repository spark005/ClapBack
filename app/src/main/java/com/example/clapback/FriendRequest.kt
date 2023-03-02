package com.example.clapback

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendRequest : AppCompatActivity() {

    private lateinit var sendRequestBtn: Button
    private lateinit var usernameField: EditText
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    private lateinit var requestRecyclerView: RecyclerView
    private lateinit var pendingRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var userPendingList: ArrayList<User>
    private lateinit var adapter: RequestAdapter
    private lateinit var pendingAdapter: PendingAdapter
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        usernameField = findViewById(R.id.search_user)
        sendRequestBtn = findViewById(R.id.send_request)

        // Grabbing current logged in user
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        userPendingList = ArrayList()
        adapter = RequestAdapter(this, userList)
        pendingAdapter = PendingAdapter(this, userPendingList)

        requestRecyclerView = findViewById(R.id.userRequestRecyclerView)
        pendingRecyclerView = findViewById(R.id.userPendingRequest)

        requestRecyclerView.layoutManager = LinearLayoutManager(this)
        requestRecyclerView.adapter = adapter
        pendingRecyclerView.layoutManager = LinearLayoutManager(this)
        pendingRecyclerView.adapter = pendingAdapter

        mDbRef.child("user").child(mAuth.currentUser!!.uid).child("friendRequests").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DEBUG", snapshot.toString())
                userList.clear()
                for(postSnapshot in snapshot.children) {
                    if (postSnapshot.exists()) {
                        val currentRequest = postSnapshot.getValue(FriendR::class.java)
                        if (currentRequest?.ifResponse == true) {
                            mDbRef.child("user").child(currentRequest?.sender!!).get()
                                .addOnSuccessListener {
                                    val targetUser = it.getValue(User::class.java)
                                    Log.d("Target User", targetUser?.name!!)
                                    userList.add(targetUser)
                                    adapter.notifyDataSetChanged()
                                    Log.d("DEBUG", userList.toString())
                                }.addOnFailureListener {
                                    Log.e("ERROR", "Couldn't find User")
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
                Log.e("ERROR", error.toString())
            }
        })
        mDbRef.child("user").child(mAuth.currentUser!!.uid).child("friendRequests").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DEBUG", snapshot.toString())
                userPendingList.clear()
                for(postSnapshot in snapshot.children) {
                    if (postSnapshot.exists()) {
                        val currentRequest = postSnapshot.getValue(FriendR::class.java)
                        if (currentRequest?.ifResponse == false) {
                            mDbRef.child("user").child(currentRequest?.recipient!!).get()
                                .addOnSuccessListener {
                                    val targetUser = it.getValue(User::class.java)
                                    Log.d("Target User", targetUser?.name!!)
                                    userPendingList.add(targetUser)
                                    pendingAdapter.notifyDataSetChanged()
                                    Log.d("DEBUG", userPendingList.toString())
                                }.addOnFailureListener {
                                    Log.e("ERROR", "Couldn't find User")
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
                Log.e("ERROR", error.toString())
            }
        })

        val currentUserUID = mAuth.currentUser?.uid
        if (currentUserUID != null) {
            mDbRef.child("user").child(currentUserUID).addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java)!!
                    Log.d("CURRENT USER", currentUser.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
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
            usernameField.text.clear()
            var searchUID = "Nothing"

            // Linearly traversing users to see if a user exists with the given email
            mDbRef.child("user").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for(postSnapshot in snapshot.children) {

                        val foundFriend = postSnapshot.getValue(User::class.java)

                        if (foundFriend?.email.equals(searchedEmail)) {
                            searchUID = foundFriend!!.uid.toString()

                            sendRequest(searchUID)
                            break
                        }

                    }

                    // If the user does not exist
                    if (searchUID.equals("Nothing")) {
                        Toast.makeText(applicationContext, "User Not Found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    // Sends friend request to user and saves requests in database for both sender and recipient
    fun sendRequest(searchUID: String) {
        mDbRef.child("user").child(searchUID).get().addOnSuccessListener {
            val foundFriend = it.getValue(User::class.java)

            // Error handler if getValue doesn't "populate" the foundFriend user object
            // **For Debugging Purposes**
            if (foundFriend == null) {

                Toast.makeText(this, "**BIG ERROR HERE FIX**", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // TODO If friend request was already sent before, this doesn't work
            if (foundFriend.friendRequests.contains(FriendR(currentUser.uid, foundFriend.uid, true))) {

                Toast.makeText(this, "Friend request already sent", Toast.LENGTH_SHORT).show()

            } else {

                // Adding the request to both user's request list
                foundFriend.friendRequests.add(FriendR(currentUser.uid, foundFriend.uid, true))
                currentUser.friendRequests.add(FriendR(currentUser.uid,
                    foundFriend.uid, false))

                // Uploading friend requests to database of both parties
                foundFriend.uid?.let { ffuid -> mDbRef.child("user").child(ffuid).child("friendRequests")
                    .setValue(foundFriend.friendRequests)}
                currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendRequests")
                    .setValue(currentUser.friendRequests)}

            }

        } .addOnFailureListener {
            // If user not found
            Toast.makeText(this, "User Not In Database", Toast.LENGTH_SHORT).show()
        }
    }
}