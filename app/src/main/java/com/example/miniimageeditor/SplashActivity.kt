package com.example.miniimageeditor

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zh-CN"))
        setContentView(R.layout.activity_splash)

        val iv = findViewById<ImageView>(R.id.splashImage)
        val id = resources.getIdentifier("splash_cover", "drawable", packageName)
        if (id != 0) {
            iv.setImageResource(id)
        } else {
            val newFgImg = resources.getIdentifier("app_icon_png_foreground_img", "drawable", packageName)
            if (newFgImg != 0) {
                iv.setImageResource(newFgImg)
            } else {
                val newFg = resources.getIdentifier("app_icon_png_foreground", "drawable", packageName)
                if (newFg != 0) {
                    iv.setImageResource(newFg)
                } else {
                    val fg = resources.getIdentifier("app_icon_foreground", "drawable", packageName)
                    if (fg != 0) iv.setImageResource(fg) else iv.setImageResource(R.mipmap.ic_launcher)
                }
            }
        }

        iv.postDelayed({
            startActivity(android.content.Intent(this, MainActivity::class.java))
            finish()
        }, 600)
    }
}
