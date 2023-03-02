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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RequestAdapter(val context: Context, var userList: ArrayList<User>, var isPending: Boolean?= false):
    RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.request_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(mAuth.currentUser!!.uid).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!
        }.addOnFailureListener {
            Log.e("Error", "Couldn't find user")
        }
        return RequestViewHolder(view)
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.user_name)
        val acceptBtn = itemView.findViewById<Button>(R.id.accept)
        val declineBtn = itemView.findViewById<Button>(R.id.decline)
        val friendAccepted = itemView.findViewById<TextView>(R.id.friend_accepted)
        val cancelRequest = itemView.findViewById<Button>(R.id.cancel_request)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val sender = userList[position]
        userList.remove(sender)

        holder.textName.text = sender.name

        if (!isPending!!) {
            holder.acceptBtn.setOnClickListener {
                deleteRequests(sender)

                sender.friendlist.add(currentUser.uid!!)
                currentUser.friendlist.add(sender.uid!!)

                mDbRef.child("user").child(mAuth.currentUser?.uid!!).setValue(currentUser)
                mDbRef.child("user").child(sender.uid!!).setValue(sender)

                removeBtns(holder)
                holder.friendAccepted.visibility = VISIBLE
            }

            holder.declineBtn.setOnClickListener {
                deleteRequests(sender)
                notifyDataSetChanged()

                mDbRef.child("user").child(mAuth.currentUser?.uid!!).setValue(currentUser)
                mDbRef.child("user").child(sender.uid!!).setValue(sender)
            }
        } else {
            removeBtns(holder)
            holder.cancelRequest.visibility = VISIBLE

            holder.cancelRequest.setOnClickListener {
                deleteRequests(sender)
                notifyDataSetChanged()

                mDbRef.child("user").child(mAuth.currentUser?.uid!!).setValue(currentUser)
                mDbRef.child("user").child(sender.uid!!).setValue(sender)
            }
        }
    }

    private fun removeBtns(holder: RequestViewHolder){
        holder.acceptBtn.visibility = INVISIBLE
        holder.declineBtn.visibility = INVISIBLE
    }

    private fun deleteRequests(user: User) {
        if (!isPending!!) {
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
        } else {
            for (request in user.friendRequests) {
                if ((request.recipient == user.uid) && (request.sender == currentUser.uid) && (request.ifResponse == true)) {
                    user.friendRequests.remove(request)
                    break
                }
            }
            for (request in currentUser.friendRequests) {
                if ((request.recipient == user.uid) && (request.sender == currentUser.uid) && (request.ifResponse == false)) {
                    currentUser.friendRequests.remove(request)
                    break
                }
            }
        }
    }

}