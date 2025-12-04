package com.example.miniimageeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.miniimageeditor.databinding.ActivityCollageLinearBinding
import com.example.miniimageeditor.util.SaveUtils

class CollageGridActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollageLinearBinding
    private lateinit var uris: ArrayList<Uri>
    private var grid: Int = 2
    private var resultBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageLinearBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnExport.setOnClickListener { resultBitmap?.let { SaveUtils.saveToAlbum(this, it) } }

        uris = intent.getParcelableArrayListExtra("uris") ?: arrayListOf()
        grid = intent.getIntExtra("grid", 2)
        binding.toolbar.title = ""

        compose()
    }

    private fun compose() {
        val bitmaps = uris.mapNotNull { uri ->
            try { contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) } } catch (_: Exception) { null }
        }
        val n = grid
        val output = 2048
        val tile = output / n
        val canvasBmp = Bitmap.createBitmap(output, output, Bitmap.Config.ARGB_8888)
        val c = Canvas(canvasBmp)
        intent.getStringExtra("bg_uri")?.let { s ->
            try {
                val uri = Uri.parse(s)
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bg = android.graphics.BitmapFactory.decodeStream(stream)
                    val bgScaled = centerCropScale(bg, output, output)
                    c.drawBitmap(bgScaled, 0f, 0f, null)
                }
            } catch (_: Exception) {}
        }
        var index = 0
        for (row in 0 until n) {
            for (col in 0 until n) {
                val b = bitmaps.getOrNull(index)
                val placed = if (b != null) centerCropScale(b, tile, tile) else Bitmap.createBitmap(tile, tile, Bitmap.Config.ARGB_8888)
                c.drawBitmap(placed, (col * tile).toFloat(), (row * tile).toFloat(), null)
                index++
            }
        }
        resultBitmap = canvasBmp
        binding.ivPreview.setImageBitmap(canvasBmp)
    }

    private fun centerCropScale(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcRatio = src.width.toFloat() / src.height
        val dstRatio = targetW.toFloat() / targetH
        val cropW: Int
        val cropH: Int
        if (srcRatio > dstRatio) {
            cropH = src.height
            cropW = (cropH * dstRatio).toInt()
        } else {
            cropW = src.width
            cropH = (cropW / dstRatio).toInt()
        }
        val x = (src.width - cropW) / 2
        val y = (src.height - cropH) / 2
        val cropped = Bitmap.createBitmap(src, x.coerceAtLeast(0), y.coerceAtLeast(0), cropW.coerceAtLeast(1), cropH.coerceAtLeast(1))
        return Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
    }
}
