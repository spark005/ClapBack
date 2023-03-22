package com.example.clapback

import android.net.Uri
import com.google.firebase.database.DatabaseReference

open class Message {
    var senderId: String? = null
    var message: String? = null
    var image: String? = null
    var messageId: String? = null
    var reaction: Int? = null
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
        this.reaction = reactionId
        mDbRef.child("chats").child(senderRoom!!).child("messages").child(key).child("reaction")
            .setValue(reactionId).addOnSuccessListener {
                mDbRef.child("chats").child(receiverRoom!!).child("messages").child(key).child("reaction")
                    .setValue(reactionId)
            }
    }
}