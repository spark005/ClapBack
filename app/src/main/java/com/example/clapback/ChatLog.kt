package com.example.clapback

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.itextpdf.text.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.*
import java.util.*


class ChatLog : BaseActivity() {

    private lateinit var chatLayout: RelativeLayout
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageKeys: ArrayList<String?>
    private lateinit var  mDbRef: DatabaseReference
    private lateinit var image: Uri
    private lateinit var friendUid: String
    private lateinit var senderUid: String

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
        senderUid = FirebaseAuth.getInstance().currentUser?.uid!!

        senderRoom = friendUid + senderUid
        receiverRoom = senderUid + friendUid

        chatRecyclerView = findViewById(R.id.chatRecyclerView_Chat_Log)

        messageList = ArrayList()
        messageKeys = ArrayList()


        messageAdapter = MessageAdapter(this, messageList, mDbRef, senderRoom, receiverRoom, messageKeys, findViewById(R.id.replying_chat_log))

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter



        supportActionBar?.title = "Chat Log"

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

            if (result.resultCode == RESULT_OK) {
                image = result.data?.data!!
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_log_download, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.chat_log_pdf -> {
                val chatLog = StringBuilder()
                val chatLogsRef = FirebaseDatabase.getInstance().getReference("chats/$senderRoom/messages")
                chatLogsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val document = Document()
                        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)


                        val fileName = "chat_log.pdf"
                        var outputFile = File(downloadsFolder, fileName)

                        var suffix = 1
                        var newFileName = ""
                        while (outputFile.exists()) {
                            newFileName = "chat_log$suffix.pdf"
                            outputFile = File(downloadsFolder, newFileName)
                            suffix++
                        }

                        val outputStream = FileOutputStream(outputFile)

                        // Create a PdfWriter to write to the output stream
                        PdfWriter.getInstance(document, outputStream)

                        // Open the document
                        document.open()

                        // Add chat log to the document
                        //     document.add(Paragraph("Chat Log:\n"))

                        // Add each message to the document
                        var messagesProcessed = 0
                        for (postSnapshot in snapshot.children) {
                            val message = postSnapshot.getValue(Message::class.java)

                            // Add message text to the document
                            val sender = message?.senderId
                            mDbRef.child("user").child(sender!!).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                    if (message?.image != null) {
                                        // If message has an image, add it to the document
                                        val bitmap = MediaStore.Images.Media.getBitmap(this@ChatLog.contentResolver, Uri.parse(message.image))
                                        val stream = ByteArrayOutputStream()
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                        val byteArray = stream.toByteArray()
                                        val image = Image.getInstance(byteArray)

                                        image.scaleToFit(50F, 50F)

                                        document.add(Paragraph("${dataSnapshot.getValue(String::class.java)}: "))

                                        document.add(image)
                                    } else {
                                        val name = dataSnapshot.getValue(String::class.java)
                                        val newMessage = "$name: ${message?.message}\n"
                                        val paragraph = Paragraph(newMessage)
                                        document.add(paragraph)
                                    }

                                    // Increment the number of processed messages
                                    messagesProcessed++

                                    // Check if all messages have been processed
                                    if (messagesProcessed == snapshot.childrenCount.toInt()) {
                                        // Close the document after all messages have been added
                                        document.close()
                                        Toast.makeText(this@ChatLog, "Chat Log Downloaded", Toast.LENGTH_SHORT).show()
                                    }

                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // handle error here
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })

                return true
            }
            R.id.chat_log_txt -> {
                val chatLog = StringBuilder()
                val chatLogsRef = FirebaseDatabase.getInstance().getReference("chats/$senderRoom/messages")
                chatLogsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnapshot in snapshot.children) {
                            val message = postSnapshot.getValue(Message::class.java)
                            val sender = message?.senderId
                            mDbRef.child("user").child(sender!!).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val name = dataSnapshot.getValue(String::class.java)
                                    var newMessage = ""
                                    if(message.message != null) {
                                        newMessage = "$name: ${message?.message}\n"
                                    } else {
                                        newMessage = "$name: IMAGE\n"
                                    }
                                    //  val newMessage = "$name: ${message?.message}\n"
                                    chatLog.append(newMessage)

                                    // Check if this is the last message
                                    if (postSnapshot.key == snapshot.children.last().key) {
                                        // Write the chat log to a file
                                        var fileName = "chat_log.txt"
                                        val fileContents = chatLog.toString()
                                        var newFileName = ""
                                        var outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
                                        outputStream.write(fileContents.toByteArray())
                                        outputStream.close()

                                        // Create a file in the downloads folder
                                        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                        var outputFile = File(downloadsFolder, fileName)

                                        var suffix = 1
                                        while (outputFile.exists()) {
                                            newFileName = "chat_log$suffix.txt"
                                            outputFile = File(downloadsFolder, newFileName)
                                            suffix++
                                        }

                                        // Copy the contents of the file to the output file
                                        val inputStream = FileInputStream(getFileStreamPath(fileName))
                                        outputStream = FileOutputStream(outputFile)
                                        inputStream.copyTo(outputStream)
                                        inputStream.close()
                                        outputStream.close()

                                        Toast.makeText(this@ChatLog, "Chat Log Downloaded", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // handle error here
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })

                return true
            }

            else -> return super.onOptionsItemSelected(item)
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