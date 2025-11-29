package com.example.miniimageeditor

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.miniimageeditor.databinding.ActivityCameraBinding
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : ComponentActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 201)
        } else {
            startCamera()
        }

        binding.btnCapture.setOnClickListener { takePhoto() }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().setTargetRotation(binding.previewView.display.rotation).build()
            val selector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(this, selector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val dir = File(cacheDir, "capture").apply { if (!exists()) mkdirs() }
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        val file = File(dir, name)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        capture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(this@CameraActivity, "拍摄失败: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = FileProvider.getUriForFile(this@CameraActivity, "${packageName}.fileprovider", file)
                val i = Intent(this@CameraActivity, EditorActivity::class.java).apply {
                    putExtra("uri", uri.toString())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(i)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 201 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "需要相机权限以预览与拍照", Toast.LENGTH_SHORT).show()
        }
    }
}
