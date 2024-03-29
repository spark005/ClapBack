package com.example.clapback

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class MessageAdapter(val context: Context, val messageList: ArrayList<Message>,
                     val mDbRef: DatabaseReference, val senderRoom: String?, val receiverRoom: String?,
                     val messageKeys: ArrayList<String?>, val repto : RelativeLayout, val sender: String?, val receiver: String?):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1;
    val ITEM_SENT = 2;
    val IMAGE_RECEIVE = 3
    val IMAGE_SENT = 4
    val PROMPT = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            ITEM_RECEIVE -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
                return ReceiveViewHolder(view)
            }
            ITEM_SENT -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.send, parent, false)
                return SentViewHolder(view)
            }
            IMAGE_RECEIVE -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.receive_image, parent, false)
                return ReceiveImgViewHolder(view)

            }
            IMAGE_SENT -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.send_image, parent, false)
                return SentImgViewHolder(view)
            }
        }
        val view: View = LayoutInflater.from(context).inflate(R.layout.conversation_prompt, parent, false)
        return ConversationPromptViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]

        when (holder.javaClass) {
            SentViewHolder::class.java -> {
                val viewHolder = holder as SentViewHolder
                holder.sentMessage.text = currentMessage.message
                if (currentMessage.edited!!) {
                    holder.itemView.findViewById<TextView>(R.id.edited_indicator).visibility = VISIBLE
                }

                if (currentMessage.reply != null) {
                    val rbox = holder.itemView.findViewById<RelativeLayout>(R.id.replyMessage)
                    val rtext = holder.itemView.findViewById<TextView>(R.id.repMessage)

                    rtext.text = currentMessage.reply
                    rbox.setVisibility(View.VISIBLE)
                }

                val reactionBox = holder.itemView.findViewById<RelativeLayout>(R.id.reactionBoxS)
                if (currentMessage.reaction != null) {
                    reactionBox.setVisibility(View.VISIBLE)
                    val reaction = holder.itemView.findViewById<ImageView>(R.id.reactionS)

                    when(currentMessage.reaction) {
                        1 -> {
                            reaction.setImageResource(R.drawable.rheart)
                        }
                        2 -> {
                            reaction.setImageResource(R.drawable.rquest)
                        }
                        3 -> {
                            reaction.setImageResource(R.drawable.rnelson)
                        }
                        4 -> {
                            val storage = FirebaseStorage.getInstance().reference.child("reactions/$receiver")
                            val pic = File.createTempFile("customReaction", "jpg")
                            storage.child(currentMessage.reactName!!).getFile(pic).addOnSuccessListener {
                                val bitmap: Bitmap =
                                    modifyOrientation(
                                        BitmapFactory.decodeFile(pic.absolutePath),
                                        pic.absolutePath
                                    )
                                reaction.setImageBitmap(bitmap)
                            }.addOnFailureListener{

                            }
                        }
                        5 -> {
                            reaction.setImageResource(R.drawable.tup)
                        }
                        6 -> {
                            reaction.setImageResource(R.drawable.tdown)
                        }
                    }
                } else {
                    reactionBox.setVisibility(View.GONE)
                }

                val timeBox = holder.itemView.findViewById<RelativeLayout>(R.id.time)
                if (currentMessage.time != null && position == (messageList.size - 1)) {
                    val seen = holder.itemView.findViewById<TextView>(R.id.seen)
                    seen.text = "Seen " + currentMessage.time

                    timeBox.visibility = View.VISIBLE
                } else {
                    timeBox.visibility = View.GONE
                }

            }
            ReceiveViewHolder::class.java -> {
                val viewHolder = holder as ReceiveViewHolder
                holder.receiveMessage.text = currentMessage.message
                if (currentMessage.edited!!) {
                    holder.itemView.findViewById<TextView>(R.id.edited_indicator_r).visibility = VISIBLE
                }
                if (currentMessage.reply != null) {
                    val rbox = holder.itemView.findViewById<RelativeLayout>(R.id.replyMessageR)
                    val rtext = holder.itemView.findViewById<TextView>(R.id.repMessageR)

                    rtext.text = currentMessage.reply
                    rbox.setVisibility(View.VISIBLE)
                }
                val reactionBox = holder.itemView.findViewById<RelativeLayout>(R.id.reactionBox)
                if (currentMessage.reaction != null) {
                    reactionBox.setVisibility(View.VISIBLE)
                    val reaction = holder.itemView.findViewById<ImageView>(R.id.reaction)

                    when(currentMessage.reaction) {
                        1 -> {
                            reaction.setImageResource(R.drawable.rheart)
                        }
                        2 -> {
                            reaction.setImageResource(R.drawable.rquest)
                        }
                        3 -> {
                            reaction.setImageResource(R.drawable.rnelson)
                        }
                        4 -> {
                            val storage = FirebaseStorage.getInstance().reference.child("reactions/$sender")
                            val pic = File.createTempFile("customReaction", "jpg")
                            storage.child(currentMessage.reactName!!).getFile(pic).addOnSuccessListener {
                                val bitmap: Bitmap =
                                    modifyOrientation(
                                        BitmapFactory.decodeFile(pic.absolutePath),
                                        pic.absolutePath
                                    )
                                reaction.setImageBitmap(bitmap)
                            }.addOnFailureListener{

                            }
                        }
                        5 -> {
                            reaction.setImageResource(R.drawable.tup)
                        }
                        6 -> {
                            reaction.setImageResource(R.drawable.tdown)
                        }
                    }
                } else {
                    reactionBox.setVisibility(View.GONE)
                }
            }
            ReceiveImgViewHolder::class.java -> {
                val viewHolder = holder as ReceiveImgViewHolder
                val storage = FirebaseStorage.getInstance().reference.child("attachments/${currentMessage.messageId}")
                if (currentMessage.image!!.contains("image")) {
                    val pic = File.createTempFile("attachment", "jpg")
                    storage.getFile(pic).addOnSuccessListener {
                        val bitmap: Bitmap =
                            modifyOrientation(
                                BitmapFactory.decodeFile(pic.absolutePath),
                                pic.absolutePath
                            )
                        holder.receiveImgMessage.setImageBitmap(bitmap)
                    }
                }
                else if (currentMessage.image!!.contains("video")) {
                    val storage = FirebaseStorage.getInstance().reference.child("attachments/${currentMessage.messageId}")
                    storage.downloadUrl.addOnSuccessListener {
                        holder.receiveImgMessage.visibility = GONE
                        holder.receiveVidMessage.visibility = VISIBLE
                        holder.receiveVidMessage.setVideoURI(it)
                        holder.receiveVidMessage.start()
                    }
                }



                //var uri = Uri.parse(currentMessage.image)
                //try {
                //    holder.receiveImgMessage.setImageURI(uri)
                //} catch (e : java.lang.Exception) {
                //    holder.receiveImgMessage.setImageResource(R.drawable.select_image)
                //}
            }
            SentImgViewHolder::class.java -> {
                val viewHolder = holder as SentImgViewHolder
                /*val storage = FirebaseStorage.getInstance().reference.child("attachments/${currentMessage.messageId}")
                val pic = File.createTempFile("attachment", "jpg")
                storage.getFile(pic).addOnSuccessListener {
                    val bitmap: Bitmap =
                        modifyOrientation(
                            BitmapFactory.decodeFile(pic.absolutePath),
                            pic.absolutePath
                        )
                    holder.sentImgMessage.setImageBitmap(bitmap)
                }*/
                val uri = Uri.parse(currentMessage.image)
                if (currentMessage.image!!.contains("image")) {
                    Log.d("Message Attachment", "Image")
                    try {
                        holder.sentImgMessage.setImageURI(uri)
                        Log.d("Image Attachment", "Decoded $uri")
                    } catch (e: java.lang.Exception) {
                        holder.sentImgMessage.setImageResource(R.drawable.select_image)
                        Log.d("Image Attachment", "Not")
                    }
                }
                else if (currentMessage.image!!.contains("video")) {
                    Log.d("Message Attachment", "Video")
                    holder.sentImgMessage.visibility = GONE
                    holder.sentVidMessage.visibility = VISIBLE
                    try {
                        holder.sentVidMessage.setVideoURI(uri)
                        holder.sentVidMessage.start()
                        Log.d("Video Attachment", "setting to $uri and " + holder.sentVidMessage.isPlaying)
                    } catch (e: java.lang.Exception) {
                        Log.d("Video Attachment", "it didn't")
                    }
                }
            }
            ConversationPromptViewHolder::class.java -> {
                val viewHolder = holder as ConversationPromptViewHolder
                holder.conversationPrompt.text = currentMessage.message
                val reactionBox = holder.itemView.findViewById<RelativeLayout>(R.id.reactionBoxC)
                if (currentMessage.reaction != null) {
                    reactionBox.setVisibility(View.VISIBLE)
                    val reaction = holder.itemView.findViewById<ImageView>(R.id.reactionC)

                    when(currentMessage.reaction) {
                        1 -> {
                            reaction.setImageResource(R.drawable.rheart)
                        }
                        2 -> {
                            reaction.setImageResource(R.drawable.rquest)
                        }
                        3 -> {
                            reaction.setImageResource(R.drawable.rnelson)
                        }
                        4 -> {
                            val storage = FirebaseStorage.getInstance().reference.child("reactions/$sender")
                            val pic = File.createTempFile("customReaction", "jpg")
                            storage.child(currentMessage.reactName!!).getFile(pic).addOnSuccessListener {
                                val bitmap: Bitmap =
                                    modifyOrientation(
                                        BitmapFactory.decodeFile(pic.absolutePath),
                                        pic.absolutePath
                                    )
                                reaction.setImageBitmap(bitmap)
                            }.addOnFailureListener{

                            }
                        }
                    }
                } else {
                    reactionBox.setVisibility(View.GONE)
                }
            }
        }

        holder.itemView.setOnClickListener {
            when (holder.javaClass) {
                ConversationPromptViewHolder::class.java -> {
                    val viewHolder = holder as ConversationPromptViewHolder
                    //val key = messageKeys[position]
                    val key = currentMessage.messageId
                    val firstPopup = PopupMenu(context, holder.itemView)
                    firstPopup.inflate(R.menu.r_or_r)
                    firstPopup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                        when (item!!.itemId) {
                            R.id.reply -> {
                                repto.findViewById<TextView>(R.id.replyingTo).text = holder.conversationPrompt.text
                                repto.visibility = View.VISIBLE
                            }
                            R.id.react -> {
                                val popup = PopupMenu(context, holder.itemView)
                                popup.inflate(R.menu.reactions)

                                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                                    when (item!!.itemId) {
                                        R.id.heart -> {
                                            currentMessage.setReaction(1, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.question -> {
                                            currentMessage.setReaction(2, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.laugh -> {
                                            currentMessage.setReaction(3, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.customReacts -> {
                                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                                            builder.setTitle("Reactions")

                                            //set the view as recycler
                                            builder.setView(R.layout.select_reaction_view)


                                            builder.setNegativeButton("Cancel"){ dialog, which ->
                                                dialog.cancel()
                                            }

                                            // have to show first before we can edit recycler
                                            val built = builder.show()
                                            val rcv = built.findViewById<RecyclerView>(R.id.reactionCustomRecyclerView)
                                            val storage = FirebaseStorage.getInstance().reference.child("reactions/$sender")
                                            val reactionList = ArrayList<String>()
                                            val srAdapter = SelectReaction(built.context, reactionList, storage, currentMessage, mDbRef, senderRoom, receiverRoom, built)
                                            //set layout as grid with a row size of
                                            rcv.layoutManager = GridLayoutManager(built.context, 5)
                                            rcv.adapter = srAdapter

                                            reactionList.clear()
                                            //get all stored reactions and add them to the reactionList.
                                            //Its not instantaneous so added an onComplete
                                            storage.listAll().addOnCompleteListener {
                                                for (react in it.getResult().items) {
                                                    reactionList.add(react.name)
                                                    srAdapter.notifyDataSetChanged()
                                                }

                                                srAdapter.notifyDataSetChanged()
                                            }

                                        }
                                    }

                                    true
                                })
                                popup.show()
                            }
                        }

                        true
                    })
                    firstPopup.show()
                }
                ReceiveViewHolder::class.java -> {
                    val viewHolder = holder as ReceiveViewHolder
                    //val key = messageKeys[position]
                    val key = currentMessage.messageId
                    val firstPopup = PopupMenu(context, holder.itemView)
                    firstPopup.inflate(R.menu.r_or_r)
                    firstPopup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                        when (item!!.itemId) {
                            R.id.reply -> {
                                repto.findViewById<TextView>(R.id.replyingTo).text = holder.receiveMessage.text
                                repto.visibility = View.VISIBLE
                            }
                            R.id.react -> {
                                val popup = PopupMenu(context, holder.itemView)
                                popup.inflate(R.menu.reactions)

                                //very 3am kind of way to get a user's current streak
                                var strk: Int? = 0
                                mDbRef.child("user").child(sender!!).child("streak").get().addOnSuccessListener {
                                    strk = it.getValue<Int?>()

                                    //if less than 50 you cant see custom
                                    if ((strk)!! < 50) {
                                        popup.menu.findItem(R.id.customReacts).isVisible = false
                                        if ((strk)!! < 20) {
                                            popup.menu.findItem(R.id.thumbsDown).isVisible = false
                                            if ((strk)!! < 5) {
                                                popup.menu.findItem(R.id.thumbsUp).isVisible = false
                                            }
                                        }
                                    }
                                }


                                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                                    when (item!!.itemId) {
                                        R.id.heart -> {
                                            currentMessage.setReaction(1, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.question -> {
                                            currentMessage.setReaction(2, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.laugh -> {
                                            currentMessage.setReaction(3, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.thumbsUp -> {
                                            currentMessage.setReaction(5, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.thumbsDown -> {
                                            currentMessage.setReaction(6, mDbRef, senderRoom, receiverRoom, key.toString())
                                            notifyDataSetChanged()
                                        }
                                        R.id.customReacts -> {
                                            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                                            builder.setTitle("Reactions")

                                            //set the view as recycler
                                            builder.setView(R.layout.select_reaction_view)


                                            builder.setNegativeButton("Cancel"){ dialog, which ->
                                                dialog.cancel()
                                            }

                                            // have to show first before we can edit recycler
                                            val built = builder.show()
                                            val rcv = built.findViewById<RecyclerView>(R.id.reactionCustomRecyclerView)
                                            val storage = FirebaseStorage.getInstance().reference.child("reactions/$sender")
                                            val reactionList = ArrayList<String>()
                                            val srAdapter = SelectReaction(built.context, reactionList, storage, currentMessage, mDbRef, senderRoom, receiverRoom, built)
                                            //set layout as grid with a row size of
                                            rcv.layoutManager = GridLayoutManager(built.context, 5)
                                            rcv.adapter = srAdapter

                                            reactionList.clear()
                                            //get all stored reactions and add them to the reactionList.
                                            //Its not instantaneous so added an onComplete
                                            storage.listAll().addOnCompleteListener {
                                                for (react in it.getResult().items) {
                                                    reactionList.add(react.name)
                                                    srAdapter.notifyDataSetChanged()
                                                }

                                                srAdapter.notifyDataSetChanged()
                                            }

                                        }
                                    }

                                    true
                                })
                                popup.show()
                            }
                        }

                        true
                    })
                    if (!currentMessage.deleted!!) firstPopup.show()
                }
                SentViewHolder::class.java -> {
                    val viewHolder = holder as SentViewHolder
                    //val key = messageKeys[position]
                    val key = currentMessage.messageId
                    val popup = PopupMenu(context, holder.itemView)
                    popup.inflate(R.menu.edit_or_del_msg)
                    popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                        when (item!!.itemId) {
                            R.id.edit_msg -> {
                                val builder = AlertDialog.Builder(this.context)
                                val layoutInflater = LayoutInflater.from(this.context).inflate(R.layout.edit_message, null)
                                val editText = layoutInflater.findViewById<EditText>(R.id.edit_message_text)
                                var newMessage = ""
                                with(builder) {
                                    setTitle("Edit your message:")
                                    var oldMessage = ""
                                    mDbRef.child("chats").child(senderRoom!!).child("messages")
                                            .child(key!!).child("message").get().addOnSuccessListener {
                                                oldMessage = "${it.value}"
                                                Log.i("firebase", "Got value ${it.value}")
                                                editText.setText(oldMessage)
                                        }

                                    setPositiveButton("Done") {dialog, which ->
                                        newMessage = editText.text.toString()
                                        Log.d("edit", viewHolder.itemId.toString())
                                        currentMessage.editMessage(newMessage, mDbRef, senderRoom, receiverRoom, key!!)
                                        //holder.itemView.findViewById<TextView>(R.id.edited_indicator).visibility = VISIBLE
                                        mDbRef.child("chats").child(senderRoom!!).child("messages")
                                            .child(key!!).child("edited").setValue(true).addOnSuccessListener {
                                                mDbRef.child("chats").child(receiverRoom!!).child("messages")
                                                    .child(key!!).child("edited").setValue(true)
                                            }
                                        notifyDataSetChanged()
                                    }
                                    setNegativeButton("Cancel"){dialog, which ->
                                        Log.d("Main", "Canceled")
                                    }
                                    setView(layoutInflater)
                                    show()
                                }
                            }

                            R.id.delete_msg -> {
                                currentMessage.delMessage(mDbRef, senderRoom, receiverRoom, key!!)
                                holder.itemView.findViewById<TextView>(R.id.edited_indicator).visibility = GONE
                                notifyDataSetChanged()
                            }
                            else -> {}
                        }
                        true
                    })
                    if (!currentMessage.deleted!!) popup.show()
                }
            }
        }
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

    override fun getItemViewType(position: Int): Int {

        val currentMessage = messageList[position]
        if (currentMessage.senderId!!.startsWith("prompt")) {
            return PROMPT
        }
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            if (currentMessage.image != null) {
                return IMAGE_SENT
            }
            return ITEM_SENT

        }
        if (currentMessage.image != null) {
            return IMAGE_RECEIVE
        }
        return ITEM_RECEIVE
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage = itemView.findViewById<TextView>(R.id.txt_received_message)
    }

    class SentImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentImgMessage = itemView.findViewById<ImageView>(R.id.sent_image)
        val sentVidMessage = itemView.findViewById<VideoView>(R.id.sent_video)
    }
    class ReceiveImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveImgMessage = itemView.findViewById<ImageView>(R.id.received_image)
        val receiveVidMessage = itemView.findViewById<VideoView>(R.id.received_video)
    }

    class ConversationPromptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val conversationPrompt = itemView.findViewById<TextView>(R.id.txt_convo_prompt)
    }
}