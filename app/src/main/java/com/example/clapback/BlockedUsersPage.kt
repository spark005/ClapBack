package com.example.clapback

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlockedUsersPage : BaseActivity(), OnSwipeListener {

    private lateinit var unblockRecyclerView: RecyclerView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var currentUser: User
    private lateinit var adapter: BlockedUserAdapter
    private lateinit var detector: GestureDetectorCompat
    private lateinit var backButton: Button
    lateinit var blockedList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blocked_user_list)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        backButton = findViewById(R.id.back_button)

        detector = GestureDetectorCompat(this, DiaryGestureListener(this))

        blockedList = ArrayList()
        adapter = BlockedUserAdapter(this, blockedList)
        unblockRecyclerView = findViewById(R.id.userBRecyclerView)
        unblockRecyclerView.layoutManager = LinearLayoutManager(this)
        unblockRecyclerView.adapter = adapter

        // Initializing current user and userlist
        val currentUserUID = mAuth.currentUser?.uid

        // TODO, fix list removal
        // Adding blocked users to blocked list
        mDbRef.child("user").child(currentUserUID!!).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!

            // Going into user node of realtime database
            mDbRef.child("user").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    blockedList.clear()
                    for (postSnapshot in snapshot.children) {

                        val traversedUser = postSnapshot.getValue(User::class.java)

                        //TODO, one day get rid of these horrible linear traversals
                        if (currentUser.uid != traversedUser?.uid
                            && currentUser.blockedUsers.contains(traversedUser?.uid)
                        ) {
                            blockedList.add(traversedUser!!)
                            adapter.notifyDataSetChanged()
                        }

                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // commented out to_do("not yet implemented")
                }

            })
        }

        // Back button implementation
        backButton.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }

    }




    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        detector.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }
    inner class DiaryGestureListener(private val onSwipeListener: OnSwipeListener) : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onShowPress(e: MotionEvent) {
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return false
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1.x > e2.x) {
                onSwipeListener.onSwipeLeft()
            }
            return true
        }
    }


    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.swipe_screen_right,
            R.anim.swipe_screen_left)
    }

    override fun onSwipeLeft() {
        TODO("Not yet implemented")
    }

    override fun onSwipeRight() {
        TODO("Not yet implemented")
    }


}



