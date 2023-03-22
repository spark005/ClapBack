package com.example.clapback

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>,
                     val mDbRef: DatabaseReference, val senderRoom: String?, val receiverRoom: String?,
                     val messageKeys: ArrayList<String?>):
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
            }
            ReceiveViewHolder::class.java -> {
                val viewHolder = holder as ReceiveViewHolder
                holder.receiveMessage.text = currentMessage.message
                val reactionBox = holder.itemView.findViewById<RelativeLayout>(R.id.reactionBox)
                if (currentMessage.reaction != null) {
                    reactionBox.setVisibility(View.VISIBLE)
                    val reaction = holder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.reaction)

                    when(currentMessage.reaction) {
                        1 -> {
                            reaction.setImageResource(R.drawable.rheart)
                        }
                        2 -> {
                            reaction.setImageResource(R.drawable.rheart)
                        }
                        3 -> {
                            reaction.setImageResource(R.drawable.rheart)
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