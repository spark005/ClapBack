package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.sql.Types.NULL

class recFriends : AppCompatActivity() {
    private lateinit var userList: ArrayList<User>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapterForAllUsers
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var currentUser: User
    private lateinit var backBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rec_friends_page)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        backBtn = findViewById(R.id.back_button)


        userList = ArrayList()
        adapter = UserAdapterForAllUsers(this, userList)

        userRecyclerView = findViewById(R.id.userRRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter


        val currentUserUID = mAuth.currentUser?.uid
        mDbRef.child("user").child(currentUserUID!!).get().addOnSuccessListener { it ->
            currentUser = it.getValue(User::class.java)!!
            userList.clear()

            // Generating random user
            var randUser = User()
            var mutUser = User()
            for (users in currentUser.friendlist) {
                mDbRef.child("user").child(users).get().addOnSuccessListener { us ->
                    randUser = us.getValue(User::class.java)!!
                    Log.d("CURRENT USER", "-------------$randUser----------------")
                    if (randUser.friendlist.size != 0) {
                        Log.d("CURRENT USER", "-------------" + randUser.friendlist.size + "----------------")
                        for (randUsers in randUser.friendlist) {
                            if (randUsers == currentUserUID) {
                                continue
                            }
                            mDbRef.child("user").child(randUsers).get().addOnSuccessListener { rand ->
                                mutUser = rand.getValue(User::class.java)!!
                                if (userList.size < 5) {
                                    Log.d("CURRENT USER", "-------------" + userList.size + "--------US-------")
                                    userList.add(mutUser)
                                    adapter.notifyDataSetChanged()

                                }
                            }
                        }
                    }
                }
                if (userList.size == 5) {
                    break
                }
                adapter.notifyDataSetChanged()
            }


            // Going into user node of realtime database
            /*mDbRef.child("user").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()
                    for(postSnapshot in snapshot.children) {

                        val traversedUser = postSnapshot.getValue(User::class.java)

                        //We can search users excluding the friends because other users
                        //Serach user that is not currentUser + Admin + Friends
                        if(currentUser.uid != traversedUser?.uid && traversedUser?.uid != "BjhDxngcjdgpGA5CCzvE7Gdp35q2" && !currentUser.friendlist.contains(traversedUser?.uid)) {
                            userList.add(traversedUser!!)
                        }

                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // commented out to_do("not yet implemented")
                }

            })*/
        }


        // Back button implementation
        backBtn.setOnClickListener {
            val intent = Intent(this, SearchOtherUsers::class.java)
            startActivity(intent)
        }


    }
}


