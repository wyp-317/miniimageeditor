package com.example.miniimageeditor

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import com.example.miniimageeditor.databinding.DialogZoomPreviewBinding

class ZoomPreviewDialog(private val context: Context) {
    fun show(uri: Uri, onConfirm: (Uri) -> Unit) {
        val binding = DialogZoomPreviewBinding.inflate(LayoutInflater.from(context))
        binding.zoomImage.maxScale = 3f
        binding.zoomImage.setImageURI(uri)
        AlertDialog.Builder(context)
            .setTitle("预览画布")
            .setView(binding.root)
            .setPositiveButton("确认使用") { _, _ -> onConfirm(uri) }
            .setNegativeButton("取消", null)
            .show()
    }
}

