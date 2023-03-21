package com.example.clapback

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        var mDbRef = FirebaseDatabase.getInstance().getReference()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        val currentUser = userList[position]
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/${currentUser.uid}")



        val nickName = mDbRef.child("user").child(currentUserUid!!).child("friendlist_nickname").child(currentUser.uid!!).child("nickname")

        nickName.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val nickName = task.result?.value as? String
                if (nickName.isNullOrEmpty()) {
                    holder.textName.text = currentUser.name
                } else {
                    holder.textName.text = nickName
                }
            }
        }

      /*  if (nickName == "") {
            holder.textName.text = currentUser.name
        } else {
            holder.textName.text = nickName
        }*/
       // holder.textName.text = currentUser.name
        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap: Bitmap =
                modifyOrientation(
                    BitmapFactory.decodeFile(pic.absolutePath),
                    pic.absolutePath
                )

            holder.image.setImageBitmap(bitmap)

        }.addOnFailureListener{
            holder.image.setImageResource(R.drawable.mongle)
        }

        holder.image.setOnClickListener{
            val intent = Intent(context, OtherUserProfile::class.java)
            intent.putExtra("uid", currentUser.uid)

            context.startActivity(intent)
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

    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei: ExifInterface = ExifInterface(image_absolute_path);
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                return rotate(bitmap, 90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                return rotate(bitmap, 180f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                return rotate(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                return rotate(bitmap, 270f)
            }
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                return flip(bitmap, true, vertical = false)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                return flip(bitmap, false, vertical = true)
            }
            else -> {
                return bitmap
            }
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) (-1f) else 1f, if (vertical) (-1f) else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true);
    }

}

