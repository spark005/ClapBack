package com.example.clapback

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditMainProfile : AppCompatActivity() {

    private lateinit var cancel: TextView
    private lateinit var confirm: ImageView
    private lateinit var changePic: TextView
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var bio: EditText
    private lateinit var image: CircleImageView
    private lateinit var newPic: Uri

    // Fave variables in user description
    private lateinit var fmovie:EditText
    private lateinit var fmusic:EditText
    private lateinit var fbook:EditText
    private lateinit var social:EditText

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editmain_profile)

        // Brief line to remove action bar
        supportActionBar?.hide()

        cancel = findViewById(R.id.cancel)
        confirm = findViewById(R.id.confirm)
        changePic = findViewById(R.id.change_profile)
        name = findViewById(R.id.name)
        username = findViewById(R.id.username)
        bio = findViewById(R.id.bio)

        fmovie = findViewById(R.id.movie)
        fmusic = findViewById(R.id.music)
        fbook = findViewById(R.id.book)
        social = findViewById(R.id.socialMedia)

        image = findViewById(R.id.profile_image)


        val profileUid = FirebaseAuth.getInstance().currentUser?.uid
        val storage = FirebaseStorage.getInstance().reference.child("profilePic/$profileUid")

        val pic = File.createTempFile("profile", "jpg")
        storage.getFile(pic).addOnSuccessListener {
            val bitmap: Bitmap =
                modifyOrientation(
                    BitmapFactory.decodeFile(pic.absolutePath),
                    pic.absolutePath
                )
            image.setImageBitmap(bitmap)

        }.addOnFailureListener{
            image.setImageResource(R.drawable.mongle)
        }

        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                image.setImageURI(result.data?.data)
                newPic = result.data?.data!!

                val store = FirebaseStorage.getInstance().getReference("profilePic/$profileUid")

                store.putFile(newPic)
            }
        }
        changePic.setOnClickListener() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            intent.action = Intent.ACTION_GET_CONTENT
            getPic.launch(intent)
        }

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()


        // Setting the profile's initial fields up
        if (profileUid != null) {
            mDbRef.child("user").child(profileUid).get().addOnSuccessListener {
                val currentUser = it.getValue(User::class.java)

                username.setText(currentUser?.name).toString()


                if (!currentUser?.bio.equals("")) {
                    bio.setText(currentUser?.bio).toString()
                } else {
                    bio.setHint("Current Bio").toString()
                }

                if (!currentUser?.fmovie.equals("")) {
                    fmovie.setText(currentUser?.fmovie).toString()
                } else {
                    fmovie.setHint("**Favorite Movie**").toString()
                }

                if (!currentUser?.fmusic.equals("")) {
                    fmusic.setText(currentUser?.fmusic).toString()
                } else {
                    fmusic.setHint("**Favorite Song**").toString()
                }

                if (!currentUser?.fbook.equals("")) {
                    fbook.setText(currentUser?.fbook).toString()
                } else {
                    fbook.setHint("**Favorite Book**").toString()
                }

                if (!currentUser?.social.equals("")) {
                    social.setText(currentUser?.social).toString()
                } else {
                    social.setHint("**Social Media**").toString()
                }


                cancel.setOnClickListener() {
                    val intent = Intent(this, ProfilePage::class.java)
                    finish()
                    startActivity(intent)
                }
                confirm.setOnClickListener() {

                    // Setting user's attributes to inputted strings and saving that info
                    currentUser?.bio = bio.text.toString()
                    currentUser?.fmovie = fmovie.text.toString()
                    currentUser?.fmusic = fmusic.text.toString()
                    currentUser?.fbook = fbook.text.toString()
                    currentUser?.name = username.text.toString()
                    currentUser?.social = social.text.toString()
                    addUserInfo(currentUser!!)
                    val intent = Intent(this, ProfilePage::class.java)
                    finish()
                    startActivity(intent)
                }
            }
        }
    }


    // Not needed Function

    /*private fun changeNameOfUser(name:String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(mAuth.currentUser!!.uid).child("name").setValue(name)
            .addOnSuccessListener {
                val toast = Toast.makeText(applicationContext, "Name changed successfully", Toast.LENGTH_SHORT)
                toast.show()
            }
            .addOnFailureListener {
                val toast = Toast.makeText(applicationContext, "Failed to change name", Toast.LENGTH_SHORT)
                toast.show()
            }
    }*/

    // Adding user to database
    private fun addUserInfo(user: User) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        user.uid?.let { mDbRef.child("user").child(it).setValue(user) }
    }


    // This has something to do with the picture or something idk
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