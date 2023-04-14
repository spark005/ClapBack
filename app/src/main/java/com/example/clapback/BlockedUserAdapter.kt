package com.example.clapback

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class BlockedUserAdapter (val context: Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<BlockedUserAdapter.RequestViewHolder>() {

    lateinit var mAuth: FirebaseAuth
    lateinit var mDbRef: DatabaseReference
    lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.blocked_user_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        return RequestViewHolder(view)
    }


    // Initializing unblock button and user image button
    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unblockBtn = itemView.findViewById<Button>(R.id.unblock_button)
        val imageBtn = itemView.findViewById<ImageButton>(R.id.imageButton)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {

        val blockedUser = userList[position]
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/${currentUser.uid}")

        // Adding user profile pic next to their name
        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap: Bitmap =
                modifyOrientation(
                    BitmapFactory.decodeFile(pic.absolutePath),
                    pic.absolutePath
                )

            holder.imageBtn.setImageBitmap(bitmap)

        }.addOnFailureListener{
            holder.imageBtn.setImageResource(R.drawable.mongle)
        }

        // When the user clicks an image, navigate to the selected user's profile
        holder.imageBtn.setOnClickListener{
            val intent = Intent(context, OtherUserProfile::class.java)
            intent.putExtra("uid", blockedUser.uid)

            context.startActivity(intent)
        }

        // Grabbing the current user
        val currentUserUID = mAuth.currentUser?.uid
        if (currentUserUID != null) {
            mDbRef.child("user").child(currentUserUID).addValueEventListener(object:
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java)!!
                    Log.d("CURRENT USER", currentUser.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    // commented out to_do("not yet implemented")
                }

            })
        }

        // When the user selects unblock user, the blocked user will be removed from the list
        holder.unblockBtn.setOnClickListener {
            // Removing the blocked user from the removed user's list, and
            // rewriting said data to Firebase
            currentUser.friendlist.remove(blockedUser.uid)
            currentUserUID.let { cuuid -> mDbRef.child("user").child(cuuid!!).child("blockedUsers")
                .setValue(currentUser.friendRequests) }
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class  UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageButton>(R.id.imageButton)
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

