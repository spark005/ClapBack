package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import java.util.concurrent.TimeUnit

class Time : AppCompatActivity() {
    companion object {
        var CURRENT_REMAINING_TIME = "current_time_string"
    }

    private lateinit var streak: TextView
    private lateinit var currentTimeTextView: TextView
    private lateinit var btn_lets_chat: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    private lateinit var detector: GestureDetectorCompat

    private var remainingTimeInMillis: Long = 0
    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.today_cb)

        detector = GestureDetectorCompat(this, DiaryGestureListener())
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/" + mAuth.currentUser!!.uid)

        streak = findViewById(R.id.streak)
        currentTimeTextView = findViewById(R.id.remaining_time)
        btn_lets_chat = findViewById(R.id.lets_chat)

        mDbRef.child("user").child(mAuth.currentUser!!.uid).child("streak").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val new_streak = snapshot.getValue(Integer::class.java)
                streak.text = String.format("Streak: %d", new_streak)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("STREAK ERROR", "Streak is not working")
            }
        })

        if (remainingTimeInMillis == 0L) {
            startCountdownTimer()
        }

        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingTimeInMillis)
                val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeInMillis) % 60
                val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeInMillis) % 60
                currentTimeTextView.text = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds)

                updateCurrentRemainingTime()

                if (remainingTimeInMillis == 0L) {
                    // Do something when timer finishes
                    resetCountdownTimer()
                    startCountdownTimer()
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        })



        btn_lets_chat.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun updateCurrentRemainingTime(): String {
        val remainingHours = TimeUnit.MILLISECONDS.toHours(remainingTimeInMillis)
        val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeInMillis) % 60
        val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeInMillis) % 60

        CURRENT_REMAINING_TIME = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds)
        return CURRENT_REMAINING_TIME
    }

    fun startCountdownTimer() {
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
        calendar.timeInMillis = currentTimeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        if (currentTimeMillis >= calendar.timeInMillis) {
            calendar.add(Calendar.DATE, 1)
        }

        remainingTimeInMillis = calendar.timeInMillis - currentTimeMillis

        countdownTimer = object : CountDownTimer(remainingTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeInMillis = millisUntilFinished
                 updateCurrentRemainingTime()
            }

            override fun onFinish() {
                remainingTimeInMillis = 0
            }
        }
        updateCurrentRemainingTime()
        countdownTimer?.start()
    }

    fun resetCountdownTimer() {
        countdownTimer?.cancel()
        remainingTimeInMillis = 0
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
                this@Time.onSwipeLeft()
            } else if (e1.x < e2.x) {
                this@Time.onSwipeRight()
            }
            return true
        }
    }

    private fun onSwipeLeft() {
        val intent = Intent(this, ProfilePage::class.java)
        startActivity(intent)
        this.overridePendingTransition(R.anim.swipe_screen_right2, R.anim.swipe_screen_left2)
    }

    private fun onSwipeRight() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(R.anim.swipe_screen_right,
            R.anim.swipe_screen_left)
    }
}