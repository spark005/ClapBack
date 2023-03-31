package com.example.clapback

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.time.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val RC_SELECT_IMAGE = 2
class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageKeys: ArrayList<String?>
    private lateinit var  mDbRef: DatabaseReference
    private lateinit var channel: NotificationChannel
    private lateinit var notificationManager: NotificationManager
    private lateinit var selectImageButton: ImageView
    private lateinit var image: Uri
    private lateinit var typingIndicator: View
    private lateinit var typingText: TextView

    var receiverRoom: String? = null
    var senderRoom: String? = null

    val CHANNEL_ID = "MESSAGE"
    val name = "Hidden Messages"
    val descriptionText = "You sent a message"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAE_TUIns:APA91bE-ueNd3N7EXpSiRujjrZIenbNz3ihrMZ1Tl9Y2dPce-EsAo0ei5PsfS2YcXxStzBnHcZ4CKG5jpPJBt248JiQRikj3_1xmvE-Xlt0XIJuVy9IeMNcN-Q7uJHZO9J7EGTNHNo4r"
    private val contentType = "application/json"

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var mDbRef = FirebaseDatabase.getInstance().getReference()

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val friendUid = intent.getStringExtra("uid")
        val nickName = mDbRef.child("user").child(currentUserUid!!).child("friendlist_nickname").child(friendUid!!).child("nickname")


        val name = intent.getStringExtra("name")
        val receiverUID = intent.getStringExtra("uid")


        //TODO Firebase code
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUID + senderUid
        receiverRoom = senderUid + receiverUID
        val mList = null

        var typingRef = mDbRef.child("chats").child(receiverRoom!!).child("senderTyping")
        var userRef = mDbRef.child("chats").child(senderRoom!!).child("senderTyping")

        nickName.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val nickName = task.result?.value as? String
                if (nickName.isNullOrEmpty()) {
                    supportActionBar?.title = name
                } else {
                    supportActionBar?.title = nickName
                }
            }
        }

        typingText = findViewById(R.id.typing_text)
        typingIndicator = findViewById(R.id.typingIndicator)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        selectImageButton = findViewById(R.id.chooseImage)
        sendButton = findViewById(R.id.sentButton)
        messageList = ArrayList()
        messageKeys = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, mDbRef, senderRoom, receiverRoom, messageKeys, findViewById(R.id.replying))

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter


        messageBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    typingRef.setValue(true)
                } else {
                    typingRef.setValue(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val receiverTypingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receiverTyping = snapshot.value as? Boolean
                if (receiverTyping == true) {
                    addTypingIndicator()
                } else {
                    removeTypingIndicator()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        userRef.addValueEventListener(receiverTypingListener)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun notific() {
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("You got a message")
                .setContentText("Do you want to view it?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(1234, builder.build())
        }

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

                    if(!(messageList[messageList.size - 1].senderId.equals(currentUserUid)) && messageList[messageList.size - 1].time == null) {
                        val sdf = SimpleDateFormat("hh:mm")
                        val time = sdf.format(Date())

                        messageList[messageList.size - 1].setTime(time, mDbRef, senderRoom, receiverRoom, messageKeys[messageKeys.size - 1].toString())
                    }

                    messageAdapter.notifyDataSetChanged()

                    //notific()
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


        //TODO using firebase database will have to change
        // adding the message to database
        sendButton.setOnClickListener() {
            val timestamp:String? = System.currentTimeMillis().toString()
            val message = messageBox.text.toString()
            val messageObject = Message(message, senderUid, timestamp)

            val replier = findViewById<RelativeLayout>(R.id.replying)
            if (replier.visibility == View.VISIBLE) {
                messageObject.reply = findViewById<TextView>(R.id.replyingTo).text.toString()
            }

            replier.visibility = View.GONE

            mDbRef.child("chats").child(senderRoom!!).child("messages").child(messageObject.messageId!!)
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageObject.messageId!!)
                        .setValue(messageObject)
                }
            messageBox.setText("")


            /*var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("You got a message")
                .setContentText("Do you want to view it?")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(1234, builder.build())*/


            mDbRef.child("user").child(receiverUID.toString()).get().addOnSuccessListener{
                val foundFriend = it.getValue(User::class.java)

                //this if statement is just to assure the ide that yes, found friend exists
                if (!(foundFriend == null)) {
                    if (foundFriend.notifications!! && foundFriend.messNotifs!!) {
                        val notification = JSONObject()
                        val notifcationBody = JSONObject()
                        val topic = "/topics/" + receiverUID

                        try {
                            notifcationBody.put("title", "Enter_title")
                            notifcationBody.put(
                                "message",
                                message
                            )   //Enter your notification message
                            notification.put("to", topic)
                            notification.put("data", notifcationBody)
                        } catch (e: JSONException) {

                        }

                        sendNotification(notification)
                    }
                }
            }
        }

        selectImageButton.setOnClickListener() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            getPic.launch(intent)
        }

        findViewById<ImageView>(R.id.cancelReply).setOnClickListener(){
            findViewById<RelativeLayout>(R.id.replying).visibility = View.GONE
        }


    }
    private fun addTypingIndicator() {
            runOnUiThread {
                typingIndicator.visibility = View.VISIBLE
                typingText.visibility = View.VISIBLE
            }
    }

    private fun removeTypingIndicator() {
        runOnUiThread {
            typingIndicator.visibility = View.INVISIBLE
            typingText.visibility = View.INVISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null && data.data != null) {
            mDbRef = FirebaseDatabase.getInstance().getReference()
            val timestamp:String? = System.currentTimeMillis().toString()
            val contentResolver = applicationContext.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(data.data!!, takeFlags)
            val messageObject = Message(data.data, FirebaseAuth.getInstance().currentUser?.uid, timestamp)
            val messageId = messageObject.messageId
            val store = FirebaseStorage.getInstance().getReference("attachments/$messageId")
            store.putFile(data.data!!)

            Toast.makeText(this@ChatActivity, "Sending...", Toast.LENGTH_SHORT).show()
            mDbRef.child("chats").child(senderRoom!!).child("messages").child(messageObject.messageId!!)
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").child(messageObject.messageId!!)
                        .setValue(messageObject).addOnSuccessListener {
                            Toast.makeText(this@ChatActivity, "Image sent", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@ChatActivity, "Image not sent", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }


    private fun sendNotification(notification: JSONObject) {
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> {

            },
            Response.ErrorListener {
                Toast.makeText(this@ChatActivity, "Request error", Toast.LENGTH_LONG).show()
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}