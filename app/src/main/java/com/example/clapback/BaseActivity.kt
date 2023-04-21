package com.example.clapback

import android.app.ActivityManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import java.net.URI

abstract class BaseActivity : AppCompatActivity() {

    private var currentTheme = DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        currentTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_THEME, DEFAULT)
        super.onCreate(savedInstanceState)
    }

    protected fun setTheme() {
        currentTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_THEME, DEFAULT)
        setTheme(currentTheme)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(
                ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                    ContextCompat.getColor(this, getColorPrimary())
                )
            )
        }
    }

    protected fun switchTheme(theme: String) {
        currentTheme = when(theme) {
            "Default Theme" -> DEFAULT
            "Warm Theme" -> SECOND
            "Dark Theme" -> THIRD
            else -> -1
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(KEY_THEME, currentTheme).apply()
    }

    protected fun isCustom(): Boolean {
        return currentTheme == -1
    }

    protected fun isDark(): Boolean {
        return currentTheme == THIRD
    }

    private fun getColorPrimary() = when(currentTheme) {
        DEFAULT -> R.color.blue
        SECOND -> R.color.colorPrimarySecond
        THIRD -> R.color.black
        else -> android.R.color.background_light
    }

    protected fun getColor() = when(currentTheme) {
        DEFAULT -> R.color.white
        SECOND -> R.color.colorAccentSecond
        THIRD -> R.color.black
        else -> android.R.color.background_light
    }

    companion object {
        private const val KEY_THEME = "Theme"
        private const val DEFAULT = R.style.Theme_ClapBack
        private const val SECOND = R.style.Theme_Second
        private const val THIRD = R.style.Theme_Third
    }
}