package com.example.clapback

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class CustomReactionAdapter (val context: Context, var reactionList: ArrayList<String>, var storage: StorageReference):
    RecyclerView.Adapter<CustomReactionAdapter.ReactionViewHolder>() {

    lateinit var currentUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomReactionAdapter.ReactionViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.activity_custom_reaction_adapter, parent, false)
        return CustomReactionAdapter.ReactionViewHolder(view)
    }

    //TODO MAKE AND ASSIGN XML FILE
    class ReactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reactName = itemView.findViewById<TextView>(R.id.react_name)
        val trash = itemView.findViewById<ImageView>(R.id.trashReact)
        val reactPic = itemView.findViewById<ImageView>(R.id.imageReact)
    }

    override fun getItemCount(): Int {
        return reactionList.size
    }

    override fun onBindViewHolder(holder: CustomReactionAdapter.ReactionViewHolder, position: Int) {
        val reactorName = reactionList[position]

        val pic = File.createTempFile(reactorName, "jpg")
        storage.child(reactorName).getFile(pic).addOnSuccessListener {
            val bitmap: Bitmap =
                modifyOrientation(
                    BitmapFactory.decodeFile(pic.absolutePath),
                    pic.absolutePath
                )
            holder.reactPic.setImageBitmap(bitmap)

        }.addOnFailureListener{

        }

        holder.reactName.text = reactorName

        holder.trash.setOnClickListener {
            storage.child(reactorName).delete().addOnSuccessListener {
                reactionList.remove(reactorName)
            }
        }

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