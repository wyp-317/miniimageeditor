package com.example.miniimageeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.miniimageeditor.databinding.ActivityCollageFreeBinding
import com.example.miniimageeditor.ui.TransformableImageView
import com.example.miniimageeditor.util.SaveUtils

class CollageFreeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollageFreeBinding
    private lateinit var uris: ArrayList<Uri>
    private lateinit var bgImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageFreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uris = intent.getParcelableArrayListExtra("uris") ?: arrayListOf()
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.canvasRoot.setBackgroundColor(0xFFFFFFFF.toInt())
        bgImageView = ImageView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        binding.canvasRoot.addView(bgImageView)
        addImages()

        binding.btnPickBackground.setOnClickListener {
            BackgroundPickerDialog(this) { resId ->
                bgImageView.setImageResource(resId)
            }.show()
        }

        binding.btnExport.setOnClickListener { exportCanvas() }
    }

    private fun addImages() {
        uris.forEach { uri ->
            val iv = TransformableImageView(this)
            iv.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
            iv.scaleType = ImageView.ScaleType.MATRIX
            try {
                contentResolver.openInputStream(uri)?.use {
                    val bmp = android.graphics.BitmapFactory.decodeStream(it)
                    iv.setImageBitmap(bmp)
                }
            } catch (_: Exception) { }
            binding.canvasRoot.addView(iv)
        }
    }

    override fun onStart() {
        super.onStart()
        intent.getStringExtra("bg_uri")?.let { s ->
            try {
                bgImageView.setImageURI(Uri.parse(s))
            } catch (_: Exception) { }
        }
    }

    private fun exportCanvas() {
        val w = binding.canvasRoot.width
        val h = binding.canvasRoot.height
        if (w <= 0 || h <= 0) return
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        binding.canvasRoot.draw(c)
        SaveUtils.saveToAlbum(this, bmp)
    }
}
