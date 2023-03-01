package com.example.clapback

import android.os.IBinder

class FriendR {
    var sender: String? = null
    var recipient : String? = null

    constructor(){}

    constructor(sender: String?, recipient: String?) {
        this.sender = sender
        this.recipient = recipient
    }

}