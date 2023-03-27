package com.example.clapback

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

        val receiverUID = "BjhDxngcjdgpGA5CCzvE7Gdp35q2"
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

                // TODO email functionality
                /*val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("swimmerchrist7@gmail.com"))
                email.putExtra(Intent.EXTRA_SUBJECT, "Report")
                email.putExtra(Intent.EXTRA_TEXT, message)

                //need this to prompts email client only

                //need this to prompts email client only
                email.type = "message/rfc822"

                startActivity(Intent.createChooser(email, "Choose an Email client :"))*/

                //val address = arrayOf<String>("swimmerchrist7@gmail.com", "lawsonluke.business@gmail.com")
                //composeEmail(address, "report", messageObject.toString())
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

    /*@SuppressLint("QueryPermissionsNeeded")
    private fun composeEmail(addresses: Array<String>, subject: String?, messageObject: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            intent.data = Uri.parse("mailto:") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_TEXT, messageObject)
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this@Report, "Failed to send report", Toast.LENGTH_SHORT).show()
        }
    }*/


}