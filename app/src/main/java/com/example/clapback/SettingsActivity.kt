package com.example.clapback

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.content.DialogInterface
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TITLE_TAG = "Settings"
private lateinit var confirm: Button

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle("Settings")
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!  //not sure y error
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)

            val preference: Preference? = findPreference("cpass_header")
            val intent = Intent(context, EditPassword::class.java)
            preference?.setIntent(intent)

            val email: Preference? = findPreference("cemail_header")
            val emailIntent = Intent(context, EditEmail::class.java)
            email?.setIntent(emailIntent)
        }
    }
    class MessagesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.messages_preferences, rootKey)
        }
    }

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }

    //THIS DOESNT WORK UGHGDHGSKGSJGDSJKHG
    class DeleteFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            //setPreferencesFromResource(R.xml.delete_preferences, rootKey)

            //val button: Preference? = findPreference(getString(R.string.delete_header)) //this is null idk y
            //button?.onPreferenceClickListener =
                //Preference.OnPreferenceClickListener {
                    //val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
                    //dialogBuilder.setMessage("Do you want to Delete your account ? This action is irreversible")
                        // if the dialog is cancelable
                        //.setCancelable(false)
                        // positive button text and action
                        //.setPositiveButton("Proceed", DialogInterface.OnClickListener {
                                //dialog, id -> finish()
                        //})
                        // negative button text and action
                        //.setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                //dialog, id -> dialog.cancel()
                        //})

                    //true
                //}
        }
        private fun finish() {
            TODO("Not yet implemented, THis is where I think u put code to delete Luke")


            //TODO, create way for user to re-input their credentials for reauthentication

            val user = Firebase.auth.currentUser!!

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.

            val credential = EmailAuthProvider
                .getCredential("user@example.com", "password1234")

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                .addOnCompleteListener { Log.d("This is a Tag", "User re-authenticated.") }


            // TODO logic for logout, Luke needs to finish when page is done
            /*mAuth.signOut()
            val intent = Intent(this@MainActivity, Login::class.java)
            finish()
            startActivity(intent)
            return true*/

            // Code for account deletion
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("This is a Tag", "User account deleted.")
                    }
                }

        }
    }

}

