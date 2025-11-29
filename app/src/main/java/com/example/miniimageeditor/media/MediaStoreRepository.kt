package com.example.miniimageeditor.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreRepository(private val context: Context) {
    suspend fun queryImagesAndVideos(limit: Int = 500): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()
        Log.d("MediaRepo", "Query start")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE
        )

        val selection = (
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
            " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        )

        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            var count = 0
            while (cursor.moveToNext() && count < limit) {
                val id = cursor.getLong(idCol)
                val type = cursor.getInt(typeCol)
                val name = cursor.getString(nameCol)
                val size = cursor.getLong(sizeCol)
                val contentUri: Uri = when (type) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    else -> continue
                }
                val isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val duration = if (isVideo) queryVideoDuration(contentUri) else null
                items.add(MediaItem(contentUri, isVideo, name, size, duration))
                count++
            }
            Log.d("MediaRepo", "Query done count=" + count)
        }
        items
    }

    private fun queryVideoDuration(uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, arrayOf(MediaStore.Video.Media.DURATION), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)) else null
            }
        } catch (e: Exception) { null }
    }
}
