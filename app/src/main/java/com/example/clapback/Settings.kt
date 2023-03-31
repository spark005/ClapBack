package com.example.clapback

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class Settings : AppCompatActivity() {

    private lateinit var changePass: Button
    private lateinit var changeEmail: Button
    private lateinit var delete : Button
    private lateinit var back : Button
    private lateinit var noti: Button
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var notificationToggleall: Switch
    private lateinit var notificationTogglem: Switch
    private lateinit var notificationTogglefr: Switch

    private lateinit var profileUid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_layout)

        // Establishing user stuff for account deletion
        val user = Firebase.auth.currentUser!!
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()

        profileUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        changePass = findViewById(R.id.change_password_button)
        changeEmail = findViewById(R.id.change_email_button)
        delete = findViewById(R.id.delete_account_button)
        back = findViewById(R.id.back_button)
        noti = findViewById(R.id.notifies)

        changePass.setOnClickListener {
            val intent = Intent(this, EditPassword::class.java)
            startActivity(intent)
        }

        changePass.setOnClickListener {
            val intent = Intent(this, EditPassword::class.java)
            startActivity(intent)
        }

        noti.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            val inflater = layoutInflater

            with(builder) {
                setTitle("Notifications")
                val dialogLayoutall = inflater.inflate(R.layout.notifications_switch, null)
                notificationToggleall = dialogLayoutall.findViewById<Switch>(R.id.toggle_notifications)

                notificationTogglem = dialogLayoutall.findViewById<Switch>(R.id.toggle_messnotifications)

                notificationTogglefr = dialogLayoutall.findViewById<Switch>(R.id.frqtoggle_notifications)

                mDbRef.child("user").child(profileUid).get().addOnSuccessListener {
                    val currentUser = it.getValue(User::class.java)
                    notificationToggleall.isChecked = currentUser?.notifications!!
                    notificationTogglem.isChecked = currentUser?.messNotifs!!
                    notificationTogglefr.isChecked = currentUser?.frNotifs!!
                }
                setView(dialogLayoutall)
                setPositiveButton("OK") { dialogInterface, i -> setNotifications(dialogInterface, i) }
                show()
            }
        }

        delete.setOnClickListener {
            val warning = AlertDialog.Builder(this)
            warning.setTitle("WARNING: ACCOUNT DELETION")
            warning.setMessage("Are you sure you want to delete your account?")

            /* TODO LUKE, you didn't *actually* delete the user from each other their
           friend's friendlists. You gotta do that. Right now it actually just
           won't display the user in their chat history */


            // Warning popup if yes
            warning.setPositiveButton("Yes") { dialog, which ->
                deleteUser(user)
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }

            // Warning popup if no
            warning.setNegativeButton("No") { dialog, which ->
                return@setNegativeButton
            }
            warning.show()
        }

        back.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            startActivity(intent)
        }
    }

    private fun setNotifications(dialogInterface: DialogInterface, i: Int) {
        Log.d("Debug", notificationToggleall.isChecked.toString())
        mDbRef.child("user").child(profileUid).child("notifications").setValue(notificationToggleall.isChecked)
        mDbRef.child("user").child(profileUid).child("messNotifs").setValue(notificationTogglem.isChecked)
        mDbRef.child("user").child(profileUid).child("frNotifs").setValue(notificationTogglefr.isChecked)

    }

    // Function to completely delete user
    private fun deleteUser(user: FirebaseUser) {
        mDbRef.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot in snapshot.children) {
                    val foundFriend = postSnapshot.getValue(User::class.java)

                    if (foundFriend?.uid.equals(user.uid)) {
                        postSnapshot.ref.removeValue()

                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("This is a Tag", "User account deleted.")
                                } else {
                                    Toast.makeText(
                                        this@Settings,
                                        "ERROR: Account couldn't be deleted!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        // Signing out user
                        mAuth.signOut()


                        break
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


}