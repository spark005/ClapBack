package com.example.clapback

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import top.defaults.colorpicker.ColorPickerPopup

class CustomizeTheme : AppCompatActivity() {

    private lateinit var backgroundPic: ImageView
    private lateinit var selectBackground: Button
    private lateinit var colorPreview: View
    private lateinit var colorBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var image: Uri
    private var defaultColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize_theme)

        backgroundPic = findViewById(R.id.backgroundpic)
        selectBackground = findViewById(R.id.selectbackground)
        colorPreview = findViewById(R.id.preview_selected_color)
        confirmBtn = findViewById(R.id.confirm_button)
        colorBtn = findViewById(R.id.pick_color_button)

        colorBtn.setOnClickListener(
            object : OnClickListener {
                override fun onClick(v: View?) {
                    ColorPickerPopup.Builder(this@CustomizeTheme).initialColor(
                        Color.RED
                    )
                        .enableBrightness(
                            true
                        )
                        .enableAlpha(
                            true
                        )
                        .okTitle(
                            "Choose"
                        )
                        .cancelTitle(
                            "Cancel"
                        )
                        .showIndicator(
                            true
                        )
                        .showValue(
                            true
                        )
                        .build()
                        .show(
                            v,
                            object : ColorPickerPopup.ColorPickerObserver() {
                                override fun onColorPicked(color: Int) {
                                    defaultColor = color
                                    colorPreview.setBackgroundColor(defaultColor)
                                }
                            })
                }
            })

        val getPic = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                backgroundPic.setImageURI(result.data?.data)
                image = result.data?.data!!
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(image, takeFlags)
            }
        }

        selectBackground.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            getPic.launch(intent)
        }

        confirmBtn.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("Background", image.toString()).apply()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("Color", defaultColor).apply()
            val intent = Intent(this@CustomizeTheme, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }
}