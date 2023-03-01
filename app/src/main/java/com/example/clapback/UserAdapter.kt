package com.example.clapback

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class UserAdapter (val context: Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var filteredList: ArrayList<User> = ArrayList()


    //TODO Figure out what a recycler view is and what this code does
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val currentUser = userList[position]
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/${currentUser.uid}")


        holder.textName.text = currentUser.name
        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(pic.absolutePath)
            holder.image.setImageBitmap(bitmap)

        }.addOnFailureListener{
            holder.image.setImageResource(R.drawable.mongle)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)



            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class  UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.txt_name)
        val image = itemView.findViewById<ImageButton>(R.id.imageButton)
    }

    fun setFilteredList(filteredList: ArrayList<User>) {
        this.filteredList = filteredList
        userList = filteredList
        notifyDataSetChanged()
    }

    fun getFilteredList(): ArrayList<User> {
        return filteredList
    }
}