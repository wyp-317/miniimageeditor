package com.example.miniimageeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.miniimageeditor.databinding.ActivityCollageLinearBinding
import com.example.miniimageeditor.util.SaveUtils
import kotlin.math.max

class CollageLinearActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollageLinearBinding
    private lateinit var uris: ArrayList<Uri>
    private lateinit var orientation: String
    private var resultBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageLinearBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uris = intent.getParcelableArrayListExtra("uris") ?: arrayListOf()
        orientation = intent.getStringExtra("orientation") ?: "horizontal"

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnExport.setOnClickListener {
            resultBitmap?.let { SaveUtils.saveToAlbum(this, it) }
        }

        compose()
    }

    private fun compose() {
        val bitmaps = uris.mapNotNull { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) }
            } catch (e: Exception) { null }
        }
        if (bitmaps.isEmpty()) return

        val targetSize = 2048
        val scaled = bitmaps.map { b ->
            val w = b.width
            val h = b.height
            if (orientation == "horizontal") {
                val scale = targetSize.toFloat() / h
                Bitmap.createScaledBitmap(b, (w * scale).toInt(), targetSize, true)
            } else {
                val scale = targetSize.toFloat() / w
                Bitmap.createScaledBitmap(b, targetSize, (h * scale).toInt(), true)
            }
        }

        val result: Bitmap = if (orientation == "horizontal") {
            val totalW = scaled.sumOf { it.width }
            val maxH = scaled.maxOf { it.height }
            Bitmap.createBitmap(totalW, maxH, Bitmap.Config.ARGB_8888).also { bmp ->
                val c = Canvas(bmp)
                // draw optional background
                intent.getStringExtra("bg_uri")?.let { s ->
                    try {
                        val uri = Uri.parse(s)
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bg = android.graphics.BitmapFactory.decodeStream(stream)
                            val bgScaled = centerCropScale(bg, totalW, maxH)
                            c.drawBitmap(bgScaled, 0f, 0f, null)
                        }
                    } catch (_: Exception) {}
                }
                var x = 0
                scaled.forEach { s ->
                    c.drawBitmap(s, x.toFloat(), ((maxH - s.height) / 2f), null)
                    x += s.width
                }
            }
        } else {
            val totalH = scaled.sumOf { it.height }
            val maxW = scaled.maxOf { it.width }
            Bitmap.createBitmap(maxW, totalH, Bitmap.Config.ARGB_8888).also { bmp ->
                val c = Canvas(bmp)
                intent.getStringExtra("bg_uri")?.let { s ->
                    try {
                        val uri = Uri.parse(s)
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bg = android.graphics.BitmapFactory.decodeStream(stream)
                            val bgScaled = centerCropScale(bg, maxW, totalH)
                            c.drawBitmap(bgScaled, 0f, 0f, null)
                        }
                    } catch (_: Exception) {}
                }
                var y = 0
                scaled.forEach { s ->
                    c.drawBitmap(s, ((maxW - s.width) / 2f), y.toFloat(), null)
                    y += s.height
                }
            }
        }
        resultBitmap = result
        binding.ivPreview.setImageBitmap(result)
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
