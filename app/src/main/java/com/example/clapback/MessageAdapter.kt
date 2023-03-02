package com.example.clapback

import android.content.Context
import android.media.Image
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>):
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
            }
            ReceiveImgViewHolder::class.java -> {
                val viewHolder = holder as ReceiveImgViewHolder
                var uri = Uri.parse(currentMessage.image)
                try {
                    holder.receiveImgMessage.setImageURI(uri)
                } catch (e : java.lang.Exception) {
                    holder.receiveImgMessage.setImageResource(R.drawable.select_image)
                }
            }
            SentImgViewHolder::class.java -> {
                val viewHolder = holder as SentImgViewHolder
                var uri = Uri.parse(currentMessage.image)
                try {
                    holder.sentImgMessage.setImageURI(uri)
                } catch (e : java.lang.Exception) {
                    holder.sentImgMessage.setImageResource(R.drawable.select_image)
                }
            }
        }
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