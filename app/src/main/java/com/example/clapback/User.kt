package com.example.clapback

import android.net.Uri

class User {
    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var bio: String? = ""
    var image: Uri? = null

    var fmovie: String? = ""
    var fmusic: String? = ""
    var fbook: String? = ""

    var streak: Int? = 1
    var friendlist = ArrayList<String>()
    var friendRequests = ArrayList<FriendR>()

    constructor(){}

    constructor(name: String?, email: String?, uid: String?, friendlist: ArrayList<String> ,
                friendRequests: ArrayList<FriendR>) {
        this.name = name
        this.email = email
        this.uid = uid
        this.friendlist = friendlist
        this.friendRequests = friendRequests
    }

    override fun toString(): String {
        println(this.name)
        return super.toString()
    }
}