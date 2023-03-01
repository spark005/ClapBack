package com.example.clapback

import android.os.IBinder

class FriendR {
    var sender: String? = null
    var recipient : String? = null

    // If response is a quick field to check if this is a request you're supposed
    // to respond to (so if you were the recipient, you can respond. Otherwise no)
    var ifResponse: Boolean = false

    constructor(){}

    constructor(sender: String?, recipient: String?, ifResposne: Boolean?) {
        this.sender = sender
        this.recipient = recipient
        this.ifResponse = ifResponse
    }

}