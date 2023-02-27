package com.example.clapback

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class UserAdapter (val context: Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private lateinit var filteredList: ArrayList<User>


    //TODO Figure out what a recycler view is and what this code does
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val currentUser = userList[position]

        holder.textName.text = currentUser.name

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