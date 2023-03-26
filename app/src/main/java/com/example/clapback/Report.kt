package com.example.clapback

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Report: AppCompatActivity() {
    private lateinit var sendBtn: Button
    private lateinit var cancelReportBtn: Button
    private lateinit var reportTxt: EditText
    private lateinit var otherUserUid: String

    private lateinit var  mDbRef: DatabaseReference
    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_page)

       // val receiverUID = "BjhDxngcjdgpGA5CCzvE7Gdp35q2"
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = "BjhDxngcjdgpGA5CCzvE7Gdp35q2" + senderUid
        receiverRoom = senderUid + "BjhDxngcjdgpGA5CCzvE7Gdp35q2"

        sendBtn = findViewById(R.id.send_btn)
        cancelReportBtn = findViewById(R.id.cancel_report_btn)
        reportTxt = findViewById(R.id.report_text)
        otherUserUid = intent.getStringExtra("uid")!!

        //Our Admin account, receives the messages, UID is BjhDxngcjdgpGA5CCzvE7Gdp35q2
        sendBtn.setOnClickListener {

            if (reportTxt.text.isEmpty()) {
                Toast.makeText(this@Report, "Can't send empty report", Toast.LENGTH_SHORT).show()
            } else {
                val timestamp: String? = System.currentTimeMillis().toString()
                val message = reportTxt.text.toString()
                val messageObject = Message(message, senderUid, timestamp)

                mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }

                Toast.makeText(
                    this@Report,
                    "Your report has been sent successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, OtherUserProfile::class.java).apply {
                    putExtra("uid", otherUserUid)
                }
                startActivity(intent)
            }
        }
        cancelReportBtn.setOnClickListener {
            val intent = Intent(this, OtherUserProfile::class.java).apply {
                putExtra("uid", otherUserUid)
            }
            startActivity(intent)
        }
    }
}