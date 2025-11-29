package com.example.miniimageeditor.media

import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val displayName: String?,
    val size: Long,
    val durationMs: Long? = null
)
