package com.example.clapback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.searchView)
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText!!)      //added null check same as line 38 i.e. String?
                return true
            }
        })

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        //TODO figure out what this does
        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerView)

        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        // Going into user node of realtime database
        mDbRef.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()
                for(postSnapshot in snapshot.children) {

                    val currentUser = postSnapshot.getValue(User::class.java)

                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }

                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
            Toast.makeText(this, "No such friend", Toast.LENGTH_SHORT).show()
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
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sort) {
            mDbRef.child("user").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()
                    for(postSnapshot in snapshot.children) {

                        val currentUser = postSnapshot.getValue(User::class.java)

                        if (mAuth.currentUser?.uid != currentUser?.uid) {
                            userList.add(currentUser!!)
                        }

                    }
                    //Since the sort didn't work after searching a friend, so I combined them together and commented below line just in case there is an error
                    //userList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it -> it.name.toString() })
                    sortFilteredList()

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        if (item.itemId == R.id.logout) {
            // logic for logout

            mAuth.signOut()
            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent)
            return true
        }

        if (item.itemId == R.id.edtProfile) {

            val intent = Intent(this@MainActivity, EditProfile::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true
    }
}