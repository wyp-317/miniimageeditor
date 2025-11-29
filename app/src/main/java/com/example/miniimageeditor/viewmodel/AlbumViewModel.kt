package com.example.miniimageeditor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniimageeditor.media.MediaItem
import com.example.miniimageeditor.media.MediaStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlbumViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MediaStoreRepository(app)
    private val _items = MutableStateFlow<List<MediaItem>>(emptyList())
    val items: StateFlow<List<MediaItem>> = _items

    fun load() {
        viewModelScope.launch {
            _items.value = repo.queryImagesAndVideos()
        }
    }
}
