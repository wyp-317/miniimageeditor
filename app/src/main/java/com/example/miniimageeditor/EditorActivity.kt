package com.example.miniimageeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.miniimageeditor.databinding.ActivityEditorBinding
import com.example.miniimageeditor.gl.ImageEditorRenderer
import com.example.miniimageeditor.viewmodel.EditorViewModel
import com.example.miniimageeditor.ui.CropOverlayView
import com.example.miniimageeditor.data.db.AppDatabase
import com.example.miniimageeditor.data.db.EditHistory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditorActivity : ComponentActivity() {
    private lateinit var binding: ActivityEditorBinding
    private lateinit var vm: EditorViewModel
    private val renderer = ImageEditorRenderer()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector
    private var gestureActive = false

    private enum class Mode { EDIT, FILTER, ADJUST }
    private var currentMode = Mode.EDIT

    private data class EditorState(
        val scale: Float,
        val tx: Float,
        val ty: Float,
        val crop: android.graphics.RectF,
        val filterMode: Int,
        val brightness: Float,
        val contrast: Float,
        val exposure: Float
    )
    private val undoStack = ArrayDeque<EditorState>()
    private val redoStack = ArrayDeque<EditorState>()

    private fun snapshot(): EditorState = EditorState(
        renderer.currentScale(),
        renderer.currentTranslation().first,
        renderer.currentTranslation().second,
        android.graphics.RectF(binding.cropOverlay.cropRect),
        getFilterMode(),
        curBrightness,
        curContrast,
        curExposure
    )

    private fun applyState(s: EditorState) {
        renderer.setTransform(s.scale, s.tx, s.ty)
        binding.cropOverlay.cropRect.set(s.crop)
        setFilterMode(s.filterMode)
        setAdjustments(s.brightness, s.contrast, s.exposure)
        binding.glView.requestRender()
        binding.cropOverlay.invalidate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extra = (8 * resources.displayMetrics.density).toInt()
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bars.bottom + extra)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        vm = ViewModelProvider(this)[EditorViewModel::class.java]
        val uriString = intent.getStringExtra("uri")
        val uri = if (uriString != null) Uri.parse(uriString) else null
        vm.sourceUri.value = uri

        binding.glView.setEGLContextClientVersion(2)
        binding.glView.setRenderer(renderer)
        binding.glView.renderMode = android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY

        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
            }
            contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val exif = ExifInterface(bytes.inputStream())
                val oriented = applyExif(bmp, exif)
                renderer.bitmap = oriented
                binding.glView.requestRender()
            }
        }

        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                undoStack.addLast(snapshot()); redoStack.clear(); gestureActive = true
                return true
            }
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val w = binding.glView.width.toFloat()
                val h = binding.glView.height.toFloat()
                val fx = (detector.focusX / w) * 2f - 1f
                val fy = (detector.focusY / h) * -2f + 1f
                renderer.applyScaleAt(detector.scaleFactor, fx, fy)
                binding.glView.requestRender()
                return true
            }
            override fun onScaleEnd(detector: ScaleGestureDetector) { gestureActive = false }
        })
        gestureDetector = GestureDetector(this, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onShowPress(e: MotionEvent) {}
            override fun onSingleTapUp(e: MotionEvent): Boolean = false
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (!gestureActive) { undoStack.addLast(snapshot()); redoStack.clear(); gestureActive = true }
                val speed = 2.6f
                renderer.setTranslation(-distanceX / binding.glView.width * speed, distanceY / binding.glView.height * -speed)
                binding.glView.requestRender()
                return true
            }
            override fun onLongPress(e: MotionEvent) {}
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
        })

        val doubleTapDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                renderer.resetTransform()
                binding.glView.requestRender()
                return true
            }
        })

        binding.glView.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            doubleTapDetector.onTouchEvent(event)
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                gestureActive = false
            }
            true
        }

        binding.btnExport.setOnClickListener { exportResult() }

        // Aspect ratio buttons
        binding.btnRatioFree.setOnClickListener { binding.cropOverlay.updateAspectRatio(null) }
        binding.btnRatio11.setOnClickListener { binding.cropOverlay.updateAspectRatio(1f) }
        binding.btnRatio34.setOnClickListener { binding.cropOverlay.updateAspectRatio(3f/4f) }
        binding.btnRatio916.setOnClickListener { binding.cropOverlay.updateAspectRatio(9f/16f) }

        // Undo / Redo
        binding.btnUndo.setOnClickListener {
            val last = undoStack.removeLastOrNull()
            if (last != null) {
                val cur = snapshot()
                redoStack.addLast(cur)
                applyState(last)
            }
        }
        binding.btnRedo.setOnClickListener {
            val next = redoStack.removeLastOrNull()
            if (next != null) {
                val cur = snapshot()
                undoStack.addLast(cur)
                applyState(next)
            }
        }

        // Listen crop changes to create undo checkpoints
        binding.cropOverlay.listener = object : CropOverlayView.OnCropChangeListener {
            override fun onCropStart() {
                undoStack.addLast(snapshot()); redoStack.clear()
            }
            override fun onCropChanged(rect: android.graphics.RectF) {}
            override fun onCropEnd(rect: android.graphics.RectF) {}
        }
        // Mode switching
        binding.groupModes.check(binding.btnModeEdit.id)
        binding.groupModes.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.btnModeEdit.id -> switchMode(Mode.EDIT)
                binding.btnModeFilter.id -> switchMode(Mode.FILTER)
                binding.btnModeAdjust.id -> switchMode(Mode.ADJUST)
            }
        }

        // Filters
        binding.btnFilter1.setOnClickListener { undoStack.addLast(snapshot()); redoStack.clear(); setFilterMode(1) }
        binding.btnFilter2.setOnClickListener { undoStack.addLast(snapshot()); redoStack.clear(); setFilterMode(2) }
        binding.btnFilter3.setOnClickListener { undoStack.addLast(snapshot()); redoStack.clear(); setFilterMode(3) }
        binding.btnFilter4.setOnClickListener { undoStack.addLast(snapshot()); redoStack.clear(); setFilterMode(4) }

        // Adjustments
        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) { undoStack.addLast(snapshot()); redoStack.clear() }
            curBrightness = value / 100f * 0.5f
            renderer.setBrightness(curBrightness)
            binding.glView.requestRender()
        }
        binding.sliderContrast.addOnChangeListener { _, value, fromUser ->
            if (fromUser) { undoStack.addLast(snapshot()); redoStack.clear() }
            curContrast = value / 100f * 0.8f
            renderer.setContrast(curContrast)
            binding.glView.requestRender()
        }
        binding.sliderExposure.addOnChangeListener { _, value, fromUser ->
            if (fromUser) { undoStack.addLast(snapshot()); redoStack.clear() }
            curExposure = value / 100f * 0.8f
            renderer.setExposure(curExposure)
            binding.glView.requestRender()
        }
        Log.d("Editor", "Loaded uri=" + uri)
    }

    private var curBrightness = 0f
    private var curContrast = 0f
    private var curExposure = 0f
    private var curFilter = 0

    private fun switchMode(mode: Mode) {
        currentMode = mode
        when (mode) {
            Mode.EDIT -> {
                binding.panelEdit.animate().alpha(1f).setDuration(180).withStartAction { binding.panelEdit.visibility = android.view.View.VISIBLE }.start()
                binding.panelFilter.animate().alpha(0f).setDuration(180).withEndAction { binding.panelFilter.visibility = android.view.View.GONE }.start()
                binding.panelAdjust.animate().alpha(0f).setDuration(180).withEndAction { binding.panelAdjust.visibility = android.view.View.GONE }.start()
                binding.cropOverlay.visibility = android.view.View.VISIBLE
            }
            Mode.FILTER -> {
                binding.panelEdit.animate().alpha(0f).setDuration(180).withEndAction { binding.panelEdit.visibility = android.view.View.GONE }.start()
                binding.panelFilter.animate().alpha(1f).setDuration(180).withStartAction { binding.panelFilter.visibility = android.view.View.VISIBLE }.start()
                binding.panelAdjust.animate().alpha(0f).setDuration(180).withEndAction { binding.panelAdjust.visibility = android.view.View.GONE }.start()
                binding.cropOverlay.visibility = android.view.View.INVISIBLE
            }
            Mode.ADJUST -> {
                binding.panelEdit.animate().alpha(0f).setDuration(180).withEndAction { binding.panelEdit.visibility = android.view.View.GONE }.start()
                binding.panelFilter.animate().alpha(0f).setDuration(180).withEndAction { binding.panelFilter.visibility = android.view.View.GONE }.start()
                binding.panelAdjust.animate().alpha(1f).setDuration(180).withStartAction { binding.panelAdjust.visibility = android.view.View.VISIBLE }.start()
                binding.cropOverlay.visibility = android.view.View.INVISIBLE
            }
        }
    }

    private fun setFilterMode(mode: Int) {
        curFilter = mode
        renderer.setFilterMode(mode)
        binding.glView.requestRender()
    }

    private fun getFilterMode(): Int = curFilter

    private fun setAdjustments(b: Float, c: Float, e: Float) {
        curBrightness = b
        curContrast = c
        curExposure = e
        renderer.setBrightness(b)
        renderer.setContrast(c)
        renderer.setExposure(e)
    }

    private fun exportResult() {
        val src = renderer.bitmap ?: return
        Log.d("Editor", "Export start w=" + src.width + " h=" + src.height)
        val crop = binding.cropOverlay.cropRect
        val viewW = binding.glView.width.toFloat()
        val viewH = binding.glView.height.toFloat()

        val scale = renderer.currentScale()
        val (txNdc, tyNdc) = renderer.currentTranslation()
        val dxPx = txNdc * viewW / 2f
        val dyPx = -tyNdc * viewH / 2f

        val sx = src.width / viewW
        val sy = src.height / viewH

        fun v2sX(vx: Float): Int {
            val vcx = viewW / 2f
            val vxOriginal = vcx + (vx - vcx - dxPx) / scale
            return (vxOriginal * sx).toInt()
        }
        fun v2sY(vy: Float): Int {
            val vcy = viewH / 2f
            val vyOriginal = vcy + (vy - vcy - dyPx) / scale
            return (vyOriginal * sy).toInt()
        }

        var left = v2sX(crop.left).coerceIn(0, src.width - 1)
        var top = v2sY(crop.top).coerceIn(0, src.height - 1)
        var right = v2sX(crop.right).coerceIn(0, src.width)
        var bottom = v2sY(crop.bottom).coerceIn(0, src.height)

        if (right <= left) right = (left + 1).coerceAtMost(src.width)
        if (bottom <= top) bottom = (top + 1).coerceAtMost(src.height)

        val imgW = (right - left).coerceAtLeast(1)
        val imgH = (bottom - top).coerceAtLeast(1)

        val cropped = try {
            Bitmap.createBitmap(src, left, top, imgW, imgH)
        } catch (e: Exception) {
            Log.e("Editor", "Crop failed: ${e.message}")
            src
        }
        val effected = com.example.miniimageeditor.util.BitmapUtils.applyEffects(cropped, curFilter, curBrightness, curContrast, curExposure)
        saveToAlbum(effected ?: cropped)
    }

    private fun saveToAlbum(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            val name = "MiniEdit_${System.currentTimeMillis()}.png"
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MiniEdit")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        Log.d("Editor", "Saving to MediaStore uri=" + uri)
        binding.savingOverlay.visibility = android.view.View.VISIBLE
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var ok = false
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { out ->
                    ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val cv = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                    contentResolver.update(uri, cv, null, null)
                }
            }
            launch(kotlinx.coroutines.Dispatchers.Main) {
                binding.savingOverlay.visibility = android.view.View.GONE
                if (ok) {
                    Toast.makeText(this@EditorActivity, getString(R.string.saved_to_album), Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        AppDatabase.get(this@EditorActivity).historyDao().insert(
                            EditHistory(displayName = values.getAsString(MediaStore.Images.Media.DISPLAY_NAME), timestamp = System.currentTimeMillis())
                        )
                    }
                    finish()
                } else {
                    Toast.makeText(this@EditorActivity, getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
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
}
