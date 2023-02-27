package com.example.clapback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clapback.socket.SocketHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mSocket: Socket
    private val uid = "As2FInWsECSdhCKmB4wtRCQMefD2"
    private val gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Socket Setup
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        //TODO figure out what this does
        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerView)

        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        mSocket = SocketHandler.getSocket()

        mSocket.on(Socket.EVENT_CONNECT, onConnect)
        mSocket.on("userListUpdate", updateUserList)
        // Going into user node of realtime database
//        mDbRef.child("user").addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                userList.clear()
//                for(postSnapshot in snapshot.children) {
//
//                    val currentUser = postSnapshot.getValue(User::class.java)
//
//                    if (mAuth.currentUser?.uid != currentUser?.uid) {
//                        userList.add(currentUser!!)
//                    }
//
//                }
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })
    }

    var onConnect = Emitter.Listener {
        val data = User(uid)
        val jsonData = gson.toJson(data)
        mSocket.emit("render_userlist", jsonData)
    }

    var updateUserList = Emitter.Listener {
        val list: List<User> = gson.fromJson(it[0].toString(), Array<User>::class.java).toList()
        runOnUiThread{
            userList.clear()
            for (user in list) {
                userList.add(user)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            // logic for logout

//            mAuth.signOut()
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