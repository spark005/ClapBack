package com.example.clapback

import android.net.Uri

open class Message {
    open var senderId: String? = null
    open var message: String? = null
    open var image: String? = null
    //var isImage: Boolean? = null

    constructor(){}

    constructor(message: String?, senderId: String?) {
        this.message = message
        this.senderId = senderId
    }

    constructor(image: Uri?, senderId: String?) {
        this.image = image.toString()
        this.senderId = senderId
    }
}