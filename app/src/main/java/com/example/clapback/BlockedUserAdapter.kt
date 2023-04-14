package com.example.clapback

import android.app.AlertDialog
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
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class BlockedUserAdapter (val context: Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder>() {

    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: User
    lateinit var mDbRef: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.blocked_user_layout, parent, false)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        return BlockedUserViewHolder(view)
    }

    class BlockedUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unblockBtn = itemView.findViewById<Button>(R.id.unblock_button)
        val image = itemView.findViewById<ImageButton>(R.id.imageButton)
        val textName = itemView.findViewById<TextView>(R.id.txt_name)
    }

    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) {

        // Grabbing current user
        mDbRef.child("user").child(mAuth.currentUser!!.uid).get().addOnSuccessListener {
            currentUser = it.getValue(User::class.java)!!
        }.addOnFailureListener {
            Log.e("Error", "Couldn't find user")
        }

        val blockedUser = userList[position]
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/${blockedUser.uid}")

        holder.textName.text = blockedUser.name
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
            intent.putExtra("uid", blockedUser.uid)
            context.startActivity(intent)
        }

        // TODO implement this button, and make simplified profile page for user
       holder.unblockBtn.setOnClickListener {
           currentUser.uid?.let { cuuid -> mDbRef.child("user").child(cuuid).child("friendRequests")
               .setValue(currentUser.friendRequests) }
       }

    }

    override fun getItemCount(): Int {
        return userList.size
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

