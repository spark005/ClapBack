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
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class MessageAdapter(val context: Context, val messageList: ArrayList<Message>,
                     val mDbRef: DatabaseReference, val senderRoom: String?, val receiverRoom: String?,
                     val messageKeys: ArrayList<String?>, val repto : RelativeLayout):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1;
    val ITEM_SENT = 2;
    val IMAGE_RECEIVE = 3
    val IMAGE_SENT = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            1 -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
                return ReceiveViewHolder(view)
            }
            2 -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.send, parent, false)
                return SentViewHolder(view)
            }
            3 -> {
                val view: View = LayoutInflater.from(context).inflate(R.layout.receive_image, parent, false)
                return ReceiveImgViewHolder(view)

            }

        }
        val view: View = LayoutInflater.from(context).inflate(R.layout.send_image, parent, false)
        return SentImgViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]

        when (holder.javaClass) {
            SentViewHolder::class.java -> {
                val viewHolder = holder as SentViewHolder
                holder.sentMessage.text = currentMessage.message
                if (currentMessage.reply != null) {
                    val rbox = holder.itemView.findViewById<RelativeLayout>(R.id.replyMessage)
                    val rtext = holder.itemView.findViewById<TextView>(R.id.repMessage)

                    rtext.text = currentMessage.reply
                    rbox.setVisibility(View.VISIBLE)
                }
            }
            ReceiveViewHolder::class.java -> {
                val viewHolder = holder as ReceiveViewHolder
                holder.receiveMessage.text = currentMessage.message
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
                    }
                } else {
                    reactionBox.setVisibility(View.GONE)
                }
            }
            ReceiveImgViewHolder::class.java -> {
                val viewHolder = holder as ReceiveImgViewHolder
                val storage = FirebaseStorage.getInstance().reference.child("attachments/${currentMessage.messageId}")
                val pic = File.createTempFile("attachment", "jpg")
                storage.getFile(pic).addOnSuccessListener {
                    val bitmap: Bitmap =
                        modifyOrientation(
                            BitmapFactory.decodeFile(pic.absolutePath),
                            pic.absolutePath
                        )
                    holder.receiveImgMessage.setImageBitmap(bitmap)
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
                var uri = Uri.parse(currentMessage.image)
                try {
                    holder.sentImgMessage.setImageURI(uri)
                } catch (e : java.lang.Exception) {
                    holder.sentImgMessage.setImageResource(R.drawable.select_image)
                }
            }
        }

        holder.itemView.setOnClickListener {
            when (holder.javaClass) {
                ReceiveViewHolder::class.java -> {
                    val viewHolder = holder as ReceiveViewHolder
                    val key = messageKeys[position]
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
                SentViewHolder::class.java -> {
                    val viewHolder = holder as SentViewHolder
                    val key = messageKeys[position]
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
                                        Log.d("edit", newMessage)
                                        currentMessage.editMessage(newMessage, mDbRef, senderRoom, receiverRoom, key!!)
                                        holder.itemView.visibility = VISIBLE
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
    }
    class ReceiveImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveImgMessage = itemView.findViewById<ImageView>(R.id.received_image)

    }
}