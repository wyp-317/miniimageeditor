package com.example.miniimageeditor.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class TransformableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private val matrixValues = FloatArray(9)
    private val m = Matrix()
    var maxScale = 3f
    private var minScale = 1f
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val current = currentScale()
            val target = (current * detector.scaleFactor).coerceIn(minScale, maxScale)
            val factor = target / current
            m.postScale(factor, factor, detector.focusX, detector.focusY)
            imageMatrix = m
            ensureBounds()
            return true
        }
    })
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            m.postTranslate(-distanceX, -distanceY)
            imageMatrix = m
            ensureBounds()
            return true
        }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val cur = currentScale()
            val target = if (cur < (minScale * 2f)) (minScale * 2f) else minScale
            val factor = target / cur
            m.postScale(factor, factor, e.x, e.y)
            imageMatrix = m
            ensureBounds()
            return true
        }
    })

    private var lastRotation = 0f
    private var rotating = false

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    rotating = true
                    lastRotation = rotationBetween(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (rotating && event.pointerCount == 2) {
                    val r = rotationBetween(event)
                    val dr = r - lastRotation
                    m.postRotate(dr, width / 2f, height / 2f)
                    imageMatrix = m
                    lastRotation = r
                }
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                rotating = false
            }
        }
        return true
    }

    fun reset() {
        m.reset()
        imageMatrix = m
        configureMinScale()
        ensureBounds()
    }

    private fun rotationBetween(e: MotionEvent): Float {
        val dx = e.getX(1) - e.getX(0)
        val dy = e.getY(1) - e.getY(0)
        return (Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat())
    }

    fun currentScale(): Float {
        m.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun configureMinScale() {
        val d = drawable ?: return
        if (width == 0 || height == 0) return
        val vw = width.toFloat()
        val vh = height.toFloat()
        val dw = d.intrinsicWidth.toFloat().coerceAtLeast(1f)
        val dh = d.intrinsicHeight.toFloat().coerceAtLeast(1f)
        val scale = minOf(vw / dw, vh / dh)
        minScale = scale
        m.setScale(scale, scale)
        val tx = (vw - dw * scale) / 2f
        val ty = (vh - dh * scale) / 2f
        m.postTranslate(tx, ty)
        imageMatrix = m
    }

    private fun ensureBounds() {
        val d = drawable ?: return
        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        m.mapRect(rect)
        var dx = 0f
        var dy = 0f
        if (rect.width() >= width) {
            if (rect.left > 0) dx = -rect.left
            if (rect.right < width) dx = width - rect.right
        } else {
            dx = (width - rect.width()) / 2f - rect.left
        }
        if (rect.height() >= height) {
            if (rect.top > 0) dy = -rect.top
            if (rect.bottom < height) dy = height - rect.bottom
        } else {
            dy = (height - rect.height()) / 2f - rect.top
        }
        if (dx != 0f || dy != 0f) {
            m.postTranslate(dx, dy)
            imageMatrix = m
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureMinScale()
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        reset()
    }
    override fun setImageDrawable(drawable: android.graphics.drawable.Drawable?) {
        super.setImageDrawable(drawable)
        reset()
    }
}
