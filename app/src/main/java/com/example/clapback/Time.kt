package com.example.clapback

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class Time : AppCompatActivity() {
    companion object {
        var CURRENT_REMAINING_TIME = "current_time_string"
    }

    private lateinit var streak: TextView
    private lateinit var cb_name: TextView
    private lateinit var currentTimeTextView: TextView
    private lateinit var btn_lets_chat: Button
    private lateinit var cb_image: CircleImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var currentUid: String
    private lateinit var cb_uid: String

    private lateinit var detector: GestureDetectorCompat
    private lateinit var progressBarHorizontal: ProgressBar
    private lateinit var textViewHorizontalProgress: TextView

    var progressStatus = 0
    var handler: Handler? = null

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
        cb_name = findViewById(R.id.cb_of_day)
        cb_image = findViewById(R.id.cb_profile_image)
        currentTimeTextView = findViewById(R.id.remaining_time)
        btn_lets_chat = findViewById(R.id.lets_chat)
        progressBarHorizontal = findViewById(R.id.progressBarHorizontal)
        textViewHorizontalProgress = findViewById(R.id.textViewHorizontalProgress)

        currentUid = mAuth.currentUser?.uid.toString()

        mDbRef.child("user").child(currentUid).child("streak").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val new_streak = snapshot.getValue(Integer::class.java)
                streak.text = String.format("Streak: %d", new_streak)

                //Set progress bar status to streaks
                progressStatus = new_streak!!.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("STREAK ERROR", "Streak is not working")
            }
        })

        mDbRef.child("user").child(currentUid).child("clapback").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cb_uid = snapshot.getValue(String::class.java).toString()
                val storage = FirebaseStorage.getInstance().reference.child("profilePic/$cb_uid")
                mDbRef.child("user").child(cb_uid!!).get()
                    .addOnSuccessListener {
                        val cb = it.getValue(User::class.java)
                        cb_name.text = cb?.name

                        val pic = File.createTempFile("profile", "jpg")
                        storage.getFile(pic).addOnSuccessListener {
                            val bitmap: Bitmap =
                                modifyOrientation(
                                    BitmapFactory.decodeFile(pic.absolutePath),
                                    pic.absolutePath
                                )
                            cb_image.setImageBitmap(bitmap)

                        }.addOnFailureListener{
                            cb_image.setImageResource(R.drawable.pfp)
                        }

                        Log.d("Target User", cb?.name!!)
                    }.addOnFailureListener {
                        Log.e("ERROR", "Couldn't find User")
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("CB ERROR", "Streak is not working")
            }
        })

        if (remainingTimeInMillis == 0L) {
            startCountdownTimer()
        }

        var handler = Handler()
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
            val intent = Intent(this, ChatActivity::class.java)

            intent.putExtra("name", cb_name.text)
            intent.putExtra("uid", cb_uid)

            startActivity(intent)
        }

        //whats a handler? who knows
        //This is used to show the streak progress bar
        handler = Handler(Handler.Callback {

            progressBarHorizontal.progress = progressStatus
            textViewHorizontalProgress.text = "${progressStatus}/${progressBarHorizontal.max} till next reaction!"

            //idk anything after this line
            handler?.sendEmptyMessageDelayed(0, 100)

            true
        })
        handler?.sendEmptyMessage(0)

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

    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei: ExifInterface = ExifInterface(image_absolute_path);
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                return rotate(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                return rotate(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                return rotate(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                return rotate(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                return flip(bitmap, true, vertical = false)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                return flip(bitmap, false, vertical = true)
            }
            else -> {
                return bitmap
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) (-1f) else 1f, if (vertical) (-1f) else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true);
    }
}