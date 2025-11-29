package com.example.miniimageeditor.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import com.example.miniimageeditor.R
import kotlin.math.min

class ShimmerImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress = 0f
    private var corner = 24f
    private var bitmap: Bitmap? = null
    private var shader: BitmapShader? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerImageView)
        corner = a.getDimension(R.styleable.ShimmerImageView_cornerRadius, corner)
        val resId = a.getResourceId(R.styleable.ShimmerImageView_imageSrc, 0)
        a.recycle()
        val logoId = resources.getIdentifier("logo_hand_glow", "drawable", context.packageName)
        if (logoId != 0) {
            setImageResource(logoId)
        } else if (resId != 0) {
            setImageResource(resId)
        } else {
            setImageResource(R.drawable.app_icon_foreground)
        }
    }

    fun setImageResource(@DrawableRes id: Int) {
        try {
            val d = context.resources.getDrawable(id, context.theme)
            val b = if (d is BitmapDrawable) d.bitmap else run {
                val bmp = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                d.setBounds(0, 0, c.width, c.height)
                d.draw(c)
                bmp
            }
            bitmap = b
            shader = BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            imgPaint.shader = shader
            invalidate()
        } catch (_: Exception) {}
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val rect = RectF(0f, 0f, w, h)
        val bmp = bitmap
        val sh = shader
        if (bmp != null && sh != null) {
            val m = Matrix()
            val sx = w / bmp.width
            val sy = h / bmp.height
            m.setScale(sx, sy)
            sh.setLocalMatrix(m)
            canvas.drawRoundRect(rect, corner, corner, imgPaint)
        }

        val sweepWidth = w * 0.25f
        val x = progress * (w + sweepWidth) - sweepWidth
        sweepPaint.shader = LinearGradient(
            x, 0f, x + sweepWidth, 0f,
            intArrayOf(0x00FFFFFF, 0x44FFFFFF, 0x00FFFFFF),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, corner, corner, sweepPaint)

        progress += 0.02f
        if (progress > 1f) progress = 0f
        postInvalidateOnAnimation()
    }
}
