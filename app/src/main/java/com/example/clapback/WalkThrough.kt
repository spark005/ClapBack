package com.example.clapback

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat


class WalkThrough : AppCompatActivity() {

    private lateinit var detector: GestureDetectorCompat
    private lateinit var btn_next: Button
    private lateinit var btn_next_1: Button
    private lateinit var btn_next_2: Button
    private var is_walkthrough: Boolean = false
    private var is_walkthrough1: Boolean = false
    private var is_walkthrough2: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.walkthrough)

        detector = GestureDetectorCompat(this, DiaryGestureListener())
        btn_next = findViewById(R.id.walkthrough_next_btn)
        is_walkthrough = true

        btn_next.setOnClickListener {
            is_walkthrough = false
            first_walkThrough()
        }
    }
    private fun first_walkThrough() {
        setContentView(R.layout.walkthrough_1)
        btn_next_1 = findViewById(R.id.walkthrough1_next_btn)
        is_walkthrough1 = true

        btn_next_1.setOnClickListener {
            is_walkthrough1 = false
            second_walkThrough()
        }
    }
    private fun second_walkThrough() {
        setContentView(R.layout.walkthrough_2)
        btn_next_2 = findViewById(R.id.walkthrough2_next_btn)
        is_walkthrough2 = true

        btn_next_2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (detector.onTouchEvent(event)) {
            return true
        }
        else {
            return super.onTouchEvent(event)
        }
    }

    inner class DiaryGestureListener : GestureDetector.OnGestureListener {
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
                this@WalkThrough.onSwipeLeft()
            } else if (e1.x < e2.x) {
                this@WalkThrough.onSwipeRight()
            }
            return true
        }
    }

    private fun onSwipeLeft() {
        if (is_walkthrough) {
            is_walkthrough = false
            first_walkThrough()
        } else if (is_walkthrough1) {
            is_walkthrough1 = false
            second_walkThrough()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onSwipeRight() {
        if (is_walkthrough) {
            //do Nothing
        } else if (is_walkthrough1) {
            is_walkthrough = true
            is_walkthrough1 = false
            setContentView(R.layout.walkthrough)
        } else {
            is_walkthrough2 = false
            first_walkThrough()
        }
    }
}