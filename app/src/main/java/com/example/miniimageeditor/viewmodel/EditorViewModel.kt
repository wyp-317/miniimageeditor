package com.example.miniimageeditor.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EditorViewModel(app: Application) : AndroidViewModel(app) {
    val sourceUri = MutableStateFlow<Uri?>(null)
    val exportBitmap = MutableStateFlow<Bitmap?>(null)
    val currentMatrix = MutableStateFlow(Matrix())
}
