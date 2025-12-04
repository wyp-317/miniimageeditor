package com.example.miniimageeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.miniimageeditor.databinding.ActivityCollageSelectBinding
import com.example.miniimageeditor.databinding.ItemSelectedThumbBinding
import com.example.miniimageeditor.databinding.ItemMediaBinding
import coil.load
import coil.request.videoFrameMillis
import android.content.SharedPreferences
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns
import com.example.miniimageeditor.media.MediaItem
import com.example.miniimageeditor.media.MediaStoreRepository

class CollageSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollageSelectBinding
    private val selectedUris = mutableListOf<Uri>()
    private lateinit var repo: MediaStoreRepository
    private lateinit var prefs: SharedPreferences
    private var selectedMode: String? = null
    private var backgroundUri: Uri? = null
    private val pickCanvasVisual = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            ZoomPreviewDialog(this).show(uri) { confirmed ->
                backgroundUri = confirmed
                android.widget.Toast.makeText(this, "画布已导入", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = MediaStoreRepository(this)
        prefs = getSharedPreferences("collage", MODE_PRIVATE)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupMediaGrid()
        setupSelectedBar()
        setupModeButtons()

        binding.btnStartCollage.setOnClickListener {
            val mode = selectedMode
            if (selectedUris.isNotEmpty() && mode != null) {
                prefs.edit().putString("last_mode", mode).apply()
                when (mode) {
                    "horizontal", "vertical" -> {
                        val i = Intent(this, CollageLinearActivity::class.java)
                        i.putParcelableArrayListExtra("uris", ArrayList(selectedUris))
                        i.putExtra("orientation", mode)
                        backgroundUri?.let { i.putExtra("bg_uri", it.toString()) }
                        startActivity(i)
                    }
                    "grid2", "grid3" -> {
                        val i = Intent(this, CollageGridActivity::class.java)
                        i.putParcelableArrayListExtra("uris", ArrayList(selectedUris))
                        i.putExtra("grid", if (mode == "grid2") 2 else 3)
                        backgroundUri?.let { i.putExtra("bg_uri", it.toString()) }
                        startActivity(i)
                    }
                }
            } else {
                android.widget.Toast.makeText(this, "请先选择拼图方式", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnImportCanvas.setOnClickListener {
            pickCanvasVisual.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        updateStartButton()
    }

    private fun setupMediaGrid() {
        binding.rvMedia.layoutManager = GridLayoutManager(this, 4)
        lifecycleScope.launchWhenStarted {
            val items = repo.queryImagesAndVideos()
            binding.rvMedia.adapter = MediaGridAdapter(items) { item ->
                if (!item.isVideo) {
                    selectedUris.add(item.uri)
                    binding.rvSelected.adapter?.notifyDataSetChanged()
                    updateStartButton()
                }
            }
        }
    }

    private fun setupSelectedBar() {
        binding.rvSelected.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSelected.adapter = SelectedThumbAdapter(selectedUris) { uri ->
            selectedUris.remove(uri)
            binding.rvSelected.adapter?.notifyDataSetChanged()
            updateStartButton()
        }
    }

    private fun setupModeButtons() {
        val last = prefs.getString("last_mode", null)
        when (last) {
            "horizontal" -> binding.btnModeHorizontal.isChecked = true
            "vertical" -> binding.btnModeVertical.isChecked = true
            "grid2" -> binding.btnModeGrid2.isChecked = true
            "grid3" -> binding.btnModeGrid3.isChecked = true
        }
        selectedMode = last
        binding.modeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedMode = when (checkedId) {
                    binding.btnModeHorizontal.id -> "horizontal"
                    binding.btnModeVertical.id -> "vertical"
                    binding.btnModeGrid2.id -> "grid2"
                    binding.btnModeGrid3.id -> "grid3"
                    else -> null
                }
                updateStartButton()
            }
        }
    }

    private fun updateStartButton() {
        val enable = selectedUris.isNotEmpty() && selectedMode != null
        binding.btnStartCollage.visibility = if (enable) View.VISIBLE else View.GONE
        binding.tvHint.visibility = View.VISIBLE
    }

    private fun handleImportCanvas(uri: Uri) {
        try {
            val mime = contentResolver.getType(uri) ?: ""
            if (!mime.startsWith("image/")) {
                android.widget.Toast.makeText(this, "文件格式不支持", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            var size: Long = -1
            contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst()) size = c.getLong(c.getColumnIndexOrThrow(OpenableColumns.SIZE))
            }
            if (size > 10L * 1024 * 1024) {
                android.widget.Toast.makeText(this, "文件过大（>10MB）", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            ZoomPreviewDialog(this).show(uri) { confirmed ->
                backgroundUri = confirmed
                android.widget.Toast.makeText(this, "画布已导入", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            android.widget.Toast.makeText(this, "加载失败", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private class MediaGridAdapter(
        private val items: List<MediaItem>,
        private val onClick: (MediaItem) -> Unit
    ) : RecyclerView.Adapter<MediaGridAdapter.VH>() {
        class VH(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            if (item.isVideo) {
                holder.binding.iconPlay.visibility = View.VISIBLE
                holder.binding.duration.visibility = View.VISIBLE
                val d = item.durationMs ?: 0L
                val sec = (d / 1000).toInt()
                val mm = (sec / 60).toString().padStart(2, '0')
                val ss = (sec % 60).toString().padStart(2, '0')
                holder.binding.duration.text = "$mm:$ss"
                holder.binding.thumbnail.load(item.uri) { videoFrameMillis(0) }
            } else {
                holder.binding.iconPlay.visibility = View.GONE
                holder.binding.duration.visibility = View.GONE
                holder.binding.thumbnail.load(item.uri) {}
            }
            holder.itemView.setOnClickListener { onClick(item) }
        }
        override fun getItemCount(): Int = items.size
    }

    private class SelectedThumbAdapter(
        private val uris: List<Uri>,
        private val onRemove: (Uri) -> Unit
    ) : RecyclerView.Adapter<SelectedThumbAdapter.VH>() {
        class VH(val binding: ItemSelectedThumbBinding) : RecyclerView.ViewHolder(binding.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemSelectedThumbBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val uri = uris[position]
            holder.binding.ivThumb.setImageURI(uri)
            holder.binding.btnRemove.setOnClickListener { onRemove(uri) }
        }
        override fun getItemCount(): Int = uris.size
    }
}
