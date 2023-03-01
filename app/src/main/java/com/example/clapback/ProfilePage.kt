package com.example.clapback

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.GestureDetectorCompat

class ProfilePage : AppCompatActivity(), OnSwipeListener {

    private lateinit var friends: CardView
    private lateinit var chat: CardView
    private lateinit var settings: CardView
    private lateinit var bio: CardView
    private lateinit var requests: CardView
    private lateinit var notifications: CardView
    private lateinit var edit: Button
    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile_page)

        // Brief line to remove action bar
        supportActionBar?.hide()

        friends = findViewById(R.id.friends)
        chat = findViewById(R.id.chat)
        settings = findViewById(R.id.settings)
        bio = findViewById(R.id.bio)
        requests = findViewById(R.id.requests)
        notifications = findViewById(R.id.notifications)
        detector = GestureDetectorCompat(this, DiaryGestureListener(this))
        edit = findViewById(R.id.edit)

        settings.setOnClickListener() {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        edit.setOnClickListener() {
            val intent = Intent(this, EditMainProfile::class.java)
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
            if (e1.x < e2.x) {
                onSwipeListener.onSwipeRight()
            }
            return true
        }
    }

    override fun onSwipeLeft() {
        TODO("Not yet implemented")
    }


    override fun onSwipeRight() {
        val intent = Intent(this, Time::class.java)
        startActivity(intent)
    }


}