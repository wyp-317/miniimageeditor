package com.example.miniimageeditor.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val maskPaint = Paint().apply {
        color = 0x99000000.toInt()
    }
    val cropRect = RectF()
    var aspectRatio: Float? = null
    var listener: OnCropChangeListener? = null
    private var multiTouchActive = false
    private var lastX = 0f
    private var lastY = 0f
    private var mode: Int = 0
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 1.5f
        alpha = 160
    }
    private val handleRadius = dp(10f)
    private val hitSlop = dp(16f)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val margin = dp(24f)
        cropRect.set(margin, margin, w - margin, h - margin)
        applyAspectToCenter()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), maskPaint)
        canvas.save()
        canvas.clipRect(cropRect)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.restore()
        canvas.drawRect(cropRect, borderPaint)
        val thirdW = cropRect.width() / 3f
        val thirdH = cropRect.height() / 3f
        for (i in 1..2) {
            val x = cropRect.left + thirdW * i
            canvas.drawLine(x, cropRect.top, x, cropRect.bottom, gridPaint)
            val y = cropRect.top + thirdH * i
            canvas.drawLine(cropRect.left, y, cropRect.right, y, gridPaint)
        }
        canvas.drawCircle(cropRect.left, cropRect.top, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.right, cropRect.top, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleRadius, handlePaint)
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleRadius, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                multiTouchActive = true
                mode = -1
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                multiTouchActive = false
                mode = 0
                return true
            }
        }
        if (event.pointerCount > 1 || multiTouchActive) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x; lastY = event.y
                mode = dragMode(event.x, event.y)
                listener?.onCropStart()
            }
            MotionEvent.ACTION_MOVE -> {
                if (multiTouchActive) return true
                val dx = event.x - lastX
                val dy = event.y - lastY
                when (mode) {
                    0 -> cropRect.offset(dx, dy)
                    1 -> { cropRect.left += dx; cropRect.top += dy; enforceAspectFromCorner(true, true) }
                    2 -> { cropRect.right += dx; cropRect.top += dy; enforceAspectFromCorner(false, true) }
                    3 -> { cropRect.left += dx; cropRect.bottom += dy; enforceAspectFromCorner(true, false) }
                    4 -> { cropRect.right += dx; cropRect.bottom += dy; enforceAspectFromCorner(false, false) }
                    5 -> { cropRect.left += dx; enforceAspectFromEdge(true) }
                    6 -> { cropRect.right += dx; enforceAspectFromEdge(false) }
                    7 -> { cropRect.top += dy; enforceAspectFromTopBottom(true) }
                    8 -> { cropRect.bottom += dy; enforceAspectFromTopBottom(false) }
                }
                constrain()
                lastX = event.x; lastY = event.y
                invalidate()
                listener?.onCropChanged(cropRect)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                multiTouchActive = false
                mode = 0
                listener?.onCropEnd(cropRect)
            }
        }
        return true
    }

    private fun dragMode(x: Float, y: Float): Int {
        val nearLeft = Math.abs(x - cropRect.left) < hitSlop
        val nearRight = Math.abs(x - cropRect.right) < hitSlop
        val nearTop = Math.abs(y - cropRect.top) < hitSlop
        val nearBottom = Math.abs(y - cropRect.bottom) < hitSlop
        return when {
            nearLeft && nearTop -> 1
            nearRight && nearTop -> 2
            nearLeft && nearBottom -> 3
            nearRight && nearBottom -> 4
            nearLeft -> 5
            nearRight -> 6
            nearTop -> 7
            nearBottom -> 8
            cropRect.contains(x, y) -> 0
            else -> 0
        }
    }

    private fun constrain() {
        if (cropRect.left < 0) cropRect.left = 0f
        if (cropRect.top < 0f) cropRect.top = 0f
        if (cropRect.right > width) cropRect.offset(width - cropRect.right, 0f)
        if (cropRect.bottom > height) cropRect.offset(0f, height - cropRect.bottom)
        val minSize = dp(96f)
        if (cropRect.width() < minSize) cropRect.right = cropRect.left + minSize
        if (cropRect.height() < minSize) cropRect.bottom = cropRect.top + minSize
        if (aspectRatio != null) {
            val r = aspectRatio!!
            val w = cropRect.width()
            val h = cropRect.height()
            val targetH = w / r
            val cy = cropRect.centerY()
            cropRect.top = cy - targetH / 2f
            cropRect.bottom = cy + targetH / 2f
            if (cropRect.top < 0f) { val diff = -cropRect.top; cropRect.top += diff; cropRect.bottom += diff }
            if (cropRect.bottom > height) { val diff = cropRect.bottom - height; cropRect.top -= diff; cropRect.bottom -= diff }
        }
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density

    fun updateAspectRatio(ratio: Float?) {
        aspectRatio = ratio
        applyAspectToCenter()
        invalidate()
    }

    private fun applyAspectToCenter() {
        val r = aspectRatio ?: return
        val cx = width / 2f
        val cy = height / 2f
        val w = cropRect.width()
        val h = cropRect.height()
        val targetH = w / r
        var th = targetH
        if (th > height) {
            th = height - dp(48f)
        }
        val tw = th * r
        cropRect.left = cx - tw / 2f
        cropRect.right = cx + tw / 2f
        cropRect.top = cy - th / 2f
        cropRect.bottom = cy + th / 2f
        constrain()
    }

    private fun enforceAspectFromCorner(leftSide: Boolean, topSide: Boolean) {
        val r = aspectRatio ?: return
        val w = cropRect.width()
        val h = cropRect.height()
        val targetH = w / r
        val dy = targetH - h
        if (topSide) cropRect.top -= dy else cropRect.bottom += dy
    }

    private fun enforceAspectFromEdge(leftSide: Boolean) {
        val r = aspectRatio ?: return
        val w = cropRect.width()
        val targetH = w / r
        val cy = cropRect.centerY()
        cropRect.top = cy - targetH / 2f
        cropRect.bottom = cy + targetH / 2f
    }

    private fun enforceAspectFromTopBottom(top: Boolean) {
        val r = aspectRatio ?: return
        val h = cropRect.height()
        val targetW = h * r
        val cx = cropRect.centerX()
        cropRect.left = cx - targetW / 2f
        cropRect.right = cx + targetW / 2f
    }

    interface OnCropChangeListener {
        fun onCropStart()
        fun onCropChanged(rect: RectF)
        fun onCropEnd(rect: RectF)
    }
}
