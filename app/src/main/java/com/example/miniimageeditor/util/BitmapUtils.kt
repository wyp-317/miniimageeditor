package com.example.miniimageeditor.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface

object BitmapUtils {
    fun decodeWithExifAndSample(cr: ContentResolver, uri: Uri, maxPixels: Int = 6_000_000, maxDim28: Int = 2048): Bitmap? {
        return try {
            val exif = runCatching {
                cr.openInputStream(uri)?.use { ExifInterface(it) }
            }.getOrNull()

            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = android.graphics.ImageDecoder.createSource(cr, uri)
                android.graphics.ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    decoder.setAllocator(android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE)
                    val size = info.size
                    val max = kotlin.math.max(size.width, size.height).toFloat()
                    if (max > maxDim28) {
                        val scale = max / maxDim28
                        val tw = (size.width / scale).toInt()
                        val th = (size.height / scale).toInt()
                        decoder.setTargetSize(tw, th)
                    }
                }
            } else {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                cr.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, opts) }
                var sample = 1
                val pixels = opts.outWidth * opts.outHeight
                while (pixels / (sample * sample) > maxPixels) sample *= 2
                val opts2 = BitmapFactory.Options().apply { inSampleSize = sample }
                cr.openInputStream(uri)?.use { input -> BitmapFactory.decodeStream(input, null, opts2) }
            }

            if (decoded != null && exif != null) applyExif(decoded, exif) else decoded
        } catch (_: Throwable) { null }
    }

    private fun applyExif(src: Bitmap, exif: ExifInterface): Bitmap {
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val m = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.preScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.preScale(-1f, 1f) }
        }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    fun applyEffects(src: Bitmap, filterMode: Int, brightness: Float, contrast: Float, exposure: Float): Bitmap? {
        return try {
            val w = src.width
            val h = src.height
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(w * h)
            src.getPixels(pixels, 0, w, 0, 0, w, h)
            val gain = 1f + exposure
            val cFactor = 1f + contrast
            for (i in pixels.indices) {
                val a = (pixels[i] ushr 24) and 0xFF
                var r = ((pixels[i] ushr 16) and 0xFF) / 255f
                var g = ((pixels[i] ushr 8) and 0xFF) / 255f
                var b = (pixels[i] and 0xFF) / 255f
                // exposure
                r *= gain; g *= gain; b *= gain
                // contrast around 0.5
                r = (r - 0.5f) * cFactor + 0.5f
                g = (g - 0.5f) * cFactor + 0.5f
                b = (b - 0.5f) * cFactor + 0.5f
                // brightness
                r += brightness; g += brightness; b += brightness
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                val avg = (r + g + b) / 3f
                when (filterMode) {
                    1 -> {
                        val wLift = smoothstep(0.6f, 1.0f, lum)
                        r += 0.12f * wLift; g += 0.12f * wLift; b += 0.12f * wLift
                        val sat = 1.15f
                        r = mix(avg, r, sat); g = mix(avg, g, sat); b = mix(avg, b, sat)
                    }
                    2 -> {
                        val d = 1f - smoothstep(0f, 0.5f, lum)
                        val sat = 1f + 0.6f * d
                        r = mix(avg, r, sat); g = mix(avg, g, sat); b = mix(avg, b, sat)
                        r = (r - 0.5f) * 1.05f + 0.5f
                        g = (g - 0.5f) * 1.05f + 0.5f
                        b = (b - 0.5f) * 1.05f + 0.5f
                    }
                    3 -> {
                        val lift = (1f - smoothstep(0f, 0.5f, lum)) * 0.22f
                        r += lift; g += lift; b += lift
                    }
                    4 -> {
                        r = lum; g = lum; b = lum
                    }
                }
                r = r.coerceIn(0f, 1f); g = g.coerceIn(0f, 1f); b = b.coerceIn(0f, 1f)
                pixels[i] = (a shl 24) or (( (r * 255f).toInt() and 0xFF) shl 16) or (( (g * 255f).toInt() and 0xFF) shl 8) or (( (b * 255f).toInt() and 0xFF))
            }
            out.setPixels(pixels, 0, w, 0, 0, w, h)
            out
        } catch (_: Throwable) { null }
    }

    private fun mix(a: Float, b: Float, t: Float): Float = a * (1f - t) + b * t
    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3 - 2 * t)
    }
}
