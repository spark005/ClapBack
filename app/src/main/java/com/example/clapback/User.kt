package com.example.clapback

import android.net.Uri

class User {
    var name: String? = null
    var originalName: String? = null
    var email: String? = null
    var uid: String? = null
    var bio: String? = ""
    var image: Uri? = null
    var nickname: String? = null
    var clapback: String? = "BjhDxngcjdgpGA5CCzvE7Gdp35q2"

    var fmovie: String? = ""
    var fmusic: String? = ""
    var fbook: String? = ""
    var social: String? = ""

    var streak: Int? = 1
    //user has already been notified of their reward
    var iKnow: Boolean? = false
    var notifications: Boolean? = true
    var messNotifs: Boolean? = true
    var frNotifs: Boolean? = true
    var cbNotifs: Boolean? = true
    var friendlist = ArrayList<String>()
    var blockedUsers = ArrayList<String>()
    var friendRequests = ArrayList<FriendR>()

    constructor(){}

    constructor(name: String?, email: String?, uid: String?, friendlist: ArrayList<String> ,
                friendRequests: ArrayList<FriendR>, blockedUsers: ArrayList<String>) {
        this.name = name
        this.originalName = name
        this.email = email
        this.uid = uid
        this.friendlist = friendlist
        this.friendRequests = friendRequests
        this.blockedUsers = blockedUsers
    }

    override fun toString(): String {
        println(this.name)
        return super.toString()
    }
}