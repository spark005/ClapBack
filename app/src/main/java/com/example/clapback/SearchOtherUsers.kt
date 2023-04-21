package com.example.clapback

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.*
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

/**
 *  This class is for the all users. It is when the user tries to search all the users and sort them
 */
class SearchOtherUsers : AppCompatActivity(){

    private lateinit var userList: ArrayList<User>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapterForAllUsers
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var searchOtherView: SearchView
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_all_users)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()




        userList = ArrayList()
        adapter = UserAdapterForAllUsers(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter


        supportActionBar?.title = "Search Users"

        val currentUserUID = mAuth.currentUser?.uid
        mDbRef.child("user").child(currentUserUID!!).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!

            // Going into user node of realtime database
            mDbRef.child("user").addValueEventListener(object: ValueEventListener {
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

            })
        }



        searchOtherView = findViewById(R.id.searchOtherView)
        searchOtherView.clearFocus()
        searchOtherView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText!!.lowercase())
                return true
            }
        })

    }
    /*
     *  Filter the users inside the searchView
     */
    private fun filterList(newText: String) {
        var filteredList: ArrayList<User> = ArrayList()
        for (user in userList) {
            if (user.name?.lowercase()?.contains(newText)!!) {
                filteredList.add(user)
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No such User", Toast.LENGTH_SHORT).show()
            userRecyclerView.visibility = View.INVISIBLE
        } else {
            userRecyclerView.visibility = View.VISIBLE
            adapter.setFilteredList(filteredList)
        }
    }

    private fun sortFilteredList() {
        adapter.getFilteredList().sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it -> it.name.toString() })
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sort, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sort) {
            mDbRef.child("user").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it -> it.name.toString() })
                    sortFilteredList()

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    //Commented out
                }

            })
        }
        if (item.itemId == R.id.go_back) {
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }
        if (item.itemId == R.id.rec_friends) {
            val intent = Intent(this, recFriends::class.java)
            startActivity(intent)
        }
        return true
    }
}