package com.example.clapback

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RequestAdapter(val context: Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.request_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        return RequestViewHolder(view)
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.user_name)
        val acceptBtn = itemView.findViewById<Button>(R.id.accept)
        val declineBtn = itemView.findViewById<Button>(R.id.decline)
        val friendAccepted = itemView.findViewById<TextView>(R.id.friend_accepted)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val sender = userList[position]

        mDbRef.child("user").child(mAuth.currentUser!!.uid).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!
        }.addOnFailureListener {
            Log.e("Error", "Couldn't find user")
        }

        holder.textName.text = sender.name

        holder.acceptBtn.setOnClickListener {
            deleteRequests(sender)

            sender.friendlist.add(currentUser.uid!!)
            currentUser.friendlist.add(sender.uid!!)

            currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendRequests")
                .setValue(currentUser.friendRequests) }
            currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendlist")
                .setValue(currentUser.friendlist) }
            sender.uid?.let { sduid -> mDbRef.child("user").child(sduid).child("friendRequests")
                .setValue(sender.friendRequests) }
            sender.uid?.let { sduid -> mDbRef.child("user").child(sduid).child("friendlist")
                .setValue(sender.friendlist) }

            removeBtns(holder)
            holder.friendAccepted.visibility = VISIBLE
        }

        holder.declineBtn.setOnClickListener {
            deleteRequests(sender)

            currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendRequests")
                .setValue(currentUser.friendRequests) }
            sender.uid?.let { sduid -> mDbRef.child("user").child(sduid).child("friendRequests")
                .setValue(sender.friendRequests) }

            userList.remove(sender)
            notifyDataSetChanged()
        }
    }

    private fun removeBtns(holder: RequestViewHolder){
        holder.acceptBtn.visibility = INVISIBLE
        holder.declineBtn.visibility = INVISIBLE
    }

    private fun deleteRequests(user: User) {
        // user is sender
        for (request in user.friendRequests) {
            if ((request.recipient == currentUser.uid) && (request.sender == user.uid) && (request.ifResponse == false)) {
                user.friendRequests.remove(request)
                break
            }
        }
        for (request in currentUser.friendRequests) {
            if ((request.recipient == currentUser.uid) && (request.sender == user.uid) && (request.ifResponse == true)) {
                currentUser.friendRequests.remove(request)
                break
            }
        }
    }

}