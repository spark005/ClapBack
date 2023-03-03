package com.example.clapback

import android.net.Uri

open class Message {
    var senderId: String? = null
    var message: String? = null
    var image: String? = null
    var messageId: String? = null
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

}