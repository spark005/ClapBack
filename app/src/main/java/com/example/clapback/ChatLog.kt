package com.example.clapback

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.time.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatLog : BaseActivity() {

    private lateinit var chatLayout: RelativeLayout
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageKeys: ArrayList<String?>
    private lateinit var  mDbRef: DatabaseReference
    private lateinit var image: Uri
    private lateinit var friendUid: String

    var receiverRoom: String? = null
    var senderRoom: String? = null

    val name = "Hidden Messages"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_log)

        chatLayout = findViewById(R.id.chat_layout_chat_log)

        mDbRef = FirebaseDatabase.getInstance().getReference()

       // val receiverUID = intent.getStringExtra("uid")
        friendUid = intent.getStringExtra("uid")!!


        //TODO Firebase code
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom = friendUid + senderUid
        receiverRoom = senderUid + friendUid

        chatRecyclerView = findViewById(R.id.chatRecyclerView_Chat_Log)

        messageList = ArrayList()
        messageKeys = ArrayList()


        messageAdapter = MessageAdapter(this, messageList, mDbRef, senderRoom, receiverRoom, messageKeys, findViewById(R.id.replying_chat_log))

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        Log.e("MyApp", "receiverUID $friendUid")
        Log.e("MyApp", "senderUID $senderUid")

        // logic for adding data to recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postSnapshot in snapshot.children) {

                        val message = postSnapshot.getValue(Message::class.java)
                        messageKeys.add(postSnapshot.key)

                        messageList.add(message!!)


                    }
                    messageAdapter.notifyDataSetChanged()

                }


                override fun onCancelled(error: DatabaseError) {
                    // commented out to_do("not yet implemented")
                }

            })

        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                image = result.data?.data!!
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null && data.data != null) {
            mDbRef = FirebaseDatabase.getInstance().getReference()
            val timestamp: String? = System.currentTimeMillis().toString()
            val contentResolver = applicationContext.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(data.data!!, takeFlags)
            val messageObject = Message(data.data, FirebaseAuth.getInstance().currentUser?.uid, timestamp)
            val messageId = messageObject.messageId
            val store = FirebaseStorage.getInstance().getReference("attachments/$messageId")
            store.putFile(data.data!!)

            mDbRef.child("chats").child(senderRoom!!).child("messages").child(messageObject.messageId!!)
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageObject.messageId!!)
                        .setValue(messageObject).addOnSuccessListener {
                            //TODO THE IMAGE TO VIDEO IS JUST FOR SPRINT 2
                        }
                        .addOnFailureListener {
                        }
                }
        }
    }
}