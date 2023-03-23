package com.example.clapback

import android.net.Uri
import com.google.firebase.database.DatabaseReference

open class Message {
    var senderId: String? = null
    var message: String? = null
    var image: String? = null
    var messageId: String? = null
    var reaction: Int? = null
    var reply: String? = null
    //var isImage: Boolean? = null

    constructor(){}

    constructor(message: String?, senderId: String?, time: String?) {
        this.message = message
        this.senderId = senderId
        this.messageId = senderId + time
    }

    constructor(image: Uri?, senderId: String?, time: String?) {
        this.image = image.toString()
        this.senderId = senderId
        this.messageId = senderId + time
    }

    fun setReaction(reactionId: Int?, mDbRef: DatabaseReference, senderRoom: String?, receiverRoom: String?, key: String) {
        if (reactionId == this.reaction) {
            this.reaction = null
        } else {
            this.reaction = reactionId
        }
        mDbRef.child("chats").child(senderRoom!!).child("messages").child(key).child("reaction")
            .setValue(this.reaction).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom!!).child("messages").child(key).child("reaction")
                    .setValue(this.reaction)
            }
    }

    fun setReply(reply: String?, mDbRef: DatabaseReference, senderRoom: String?, receiverRoom: String?, key: String) {
        this.reply = reply

        mDbRef.child("chats").child(senderRoom!!).child("messages").child(key).child("reply")
            .setValue(this.reply).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom!!).child("messages").child(key).child("reply")
                    .setValue(this.reply)
            }
    }
}