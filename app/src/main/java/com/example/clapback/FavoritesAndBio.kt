package com.example.clapback

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FavoritesAndBio : AppCompatActivity() {

    private lateinit var bio : TextView
    private lateinit var song : TextView
    private lateinit var book : TextView
    private lateinit var movie : TextView
    private lateinit var back : Button
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDbRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_music_movie)

        supportActionBar?.hide()
        bio = findViewById(R.id.my_bio)
        song = findViewById(R.id.my_fav_song)
        book = findViewById(R.id.my_fav_book)
        movie = findViewById(R.id.my_fav_movie)
        back = findViewById(R.id.back_to_profile)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        val profileUid = mAuth.currentUser?.uid
        if (profileUid != null) {
            mDbRef.child("user").child(profileUid).get().addOnSuccessListener {
                val currentUser = it.getValue(User::class.java)
                if (!currentUser?.bio.equals("")) {
                    bio.setText(currentUser?.bio).toString()
                }
                if (!currentUser?.fmusic.equals("")) {
                    song.setText(currentUser?.fmusic).toString()
                }
                if (!currentUser?.fmovie.equals("")) {
                    movie.setText(currentUser?.fmovie).toString()
                }
                if (!currentUser?.fbook.equals("")) {
                    book.setText(currentUser?.fbook).toString()
                }
            }
        }
        back.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            finish()
            startActivity(intent)
        }

    }
}