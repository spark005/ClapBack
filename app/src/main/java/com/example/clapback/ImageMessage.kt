package com.example.clapback

class ImageMessage {
    var imagePath: String? = null
    var senderId: String? = null

    constructor(){}

    constructor(path: String?, senderId: String?) {
        this.imagePath = path
        this.senderId = senderId
    }
}