package com.example.clapback

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlockedUsersPage : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var currentUser: User
    private lateinit var adapter: UserAdapter
    lateinit var blockedList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blocked_user_list)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        // Initializing current user and userlist
        val currentUserUID = mAuth.currentUser?.uid
        blockedList = ArrayList()

        // Adding blocked users to blocked list
        mDbRef.child("user").child(currentUserUID!!).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!

            // Going into user node of realtime database
            mDbRef.child("user").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    blockedList.clear()
                    for (postSnapshot in snapshot.children) {

                        val traversedUser = postSnapshot.getValue(User::class.java)

                        //If the UID is admin's uid, do not clear the userList
                        if (currentUser.uid != traversedUser?.uid
                            && currentUser.blockedUsers.contains(traversedUser?.uid)
                        ) {
                            blockedList.add(traversedUser!!)
                        }

                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // commented out to_do("not yet implemented")
                }

            })
        }

    }


}