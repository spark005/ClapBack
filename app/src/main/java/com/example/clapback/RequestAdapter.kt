package com.example.clapback

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.json.JSONException
import org.json.JSONObject

class RequestAdapter(val context: Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context)
    }

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=" + "AAAAE_TUIns:APA91bE-ueNd3N7EXpSiRujjrZIenbNz3ihrMZ1Tl9Y2dPce-EsAo0ei5PsfS2YcXxStzBnHcZ4CKG5jpPJBt248JiQRikj3_1xmvE-Xlt0XIJuVy9IeMNcN-Q7uJHZO9J7EGTNHNo4r"
    private val contentType = "application/json"

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
        val blockBtn = itemView.findViewById<Button>(R.id.block)
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

            if (sender.notifications!! && sender.frNotifs!!) {
                val topic = "/topics/" + sender.uid.toString()

                val notification = JSONObject()
                val notificationBody = JSONObject()

                try {
                    notificationBody.put("title", "Friend Request Accepted!")
                    notificationBody.put(
                        "message",
                        currentUser.name + " has accepted your friend request!"
                    )
                    notification.put("to", topic)
                    notification.put("data", notificationBody)
                    Log.e("TAG", "try")
                } catch (e: JSONException) {
                    Log.e("TAG", "onSend: " + e.message)
                }

                sendNotification(notification)
            }
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


        // For when a user blocks a request by a user
        holder.blockBtn.setOnClickListener {
            val warning = AlertDialog.Builder(context)
            warning.setTitle("Blocking User")
            warning.setMessage("Are you sure you want to block ${sender.name}?")

            // If the user chose yes on the warning popup, delete request
            warning.setPositiveButton("Yes") { dialog, which ->

                deleteRequests(sender)

                currentUser.blockedUsers.add(sender.uid.toString())

                currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendRequests")
                    .setValue(currentUser.friendRequests) }

                // Writing blocked users to database
                currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("blockedUsers")
                    .setValue(currentUser.blockedUsers) }

                sender.uid?.let { sduid -> mDbRef.child("user").child(sduid).child("friendRequests")
                    .setValue(sender.friendRequests) }

                userList.remove(sender)
                notifyDataSetChanged()

                // Confirmation of deletion
                Toast.makeText(context, "${sender.name} blocked", Toast.LENGTH_SHORT).show()

            }

            // Warning popup if no
            warning.setNegativeButton("No") { dialog, which ->
                return@setNegativeButton
            }
            warning.show()

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

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }

        requestQueue.add(jsonObjectRequest)
    }
}