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

class PendingAdapter(val context: Context, var pendingList: ArrayList<User>):
    RecyclerView.Adapter<PendingAdapter.PendingViewHolder>() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.request_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        return PendingViewHolder(view)
    }

    class PendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.user_name)
        val acceptBtn = itemView.findViewById<Button>(R.id.accept)
        val declineBtn = itemView.findViewById<Button>(R.id.decline)
        val cancelRequest = itemView.findViewById<Button>(R.id.cancel_request)
    }

    override fun getItemCount(): Int {
        return pendingList.size
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val recipient = pendingList[position]

        mDbRef.child("user").child(mAuth.currentUser!!.uid).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!
        }.addOnFailureListener {
            Log.e("Error", "Couldn't find user")
        }

        holder.textName.text = recipient.name

        removeBtns(holder)
        holder.cancelRequest.visibility = VISIBLE

        holder.cancelRequest.setOnClickListener {
            deleteRequests(recipient)

            currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendRequests")
                .setValue(currentUser.friendRequests) }
            recipient.uid?.let { rpuid -> mDbRef.child("user").child(rpuid).child("friendRequests")
                .setValue(recipient.friendRequests) }

            pendingList.remove(recipient)
            notifyDataSetChanged()
        }
    }

    private fun removeBtns(holder: PendingViewHolder){
        holder.acceptBtn.visibility = INVISIBLE
        holder.declineBtn.visibility = INVISIBLE
    }

    private fun deleteRequests(user: User) {
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