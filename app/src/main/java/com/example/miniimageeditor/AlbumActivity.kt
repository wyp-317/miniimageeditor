package com.example.miniimageeditor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.activity.result.PickVisualMediaRequest
import coil.load
import com.example.miniimageeditor.databinding.ActivityAlbumBinding
import com.example.miniimageeditor.databinding.ItemMediaBinding
import com.example.miniimageeditor.media.MediaItem
import com.example.miniimageeditor.viewmodel.AlbumViewModel

class AlbumActivity : ComponentActivity() {
    private lateinit var binding: ActivityAlbumBinding
    private lateinit var vm: AlbumViewModel
    private val adapter = AlbumAdapter { item -> onItemClick(item) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.albumGrid.layoutManager = GridLayoutManager(this, 3)
        binding.albumGrid.adapter = adapter
        val space = resources.getDimensionPixelSize(R.dimen.space_sm)
        binding.albumGrid.addItemDecoration(com.example.miniimageeditor.ui.SpaceItemDecoration(space))

        vm = ViewModelProvider(this)[AlbumViewModel::class.java]
        if (hasPermission()) {
            load()
        } else {
            if (Build.VERSION.SDK_INT >= 33) {
                showPermissionGuide()
            } else {
                requestPerms()
            }
        }
        lifecycleScope.launchWhenStarted {
            vm.items.collect {
                binding.progress.visibility = View.GONE
                adapter.submit(it)
            }
        }
    }

    private val pickVisual = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            val i = Intent(this, EditorActivity::class.java)
            i.putExtra("uri", uri.toString())
            startActivity(i)
        }
    }

    private fun showPermissionGuide() {
        MaterialAlertDialogBuilder(this)
            .setTitle("照片/视频访问")
            .setMessage("为保证中文体验，我们提供两种方式：\n\n1. 仅选择要编辑的照片（无需授权）；\n2. 允许访问所有照片/视频（系统将弹出权限对话框）。")
            .setPositiveButton("允许全部") { _, _ -> requestPerms() }
            .setNeutralButton("仅选择照片") { _, _ ->
                pickVisual.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPerms() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO), 100)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("Album", "onRequestPermissionsResult code=" + requestCode + " grants=" + grantResults.joinToString())
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            load()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    private fun load() {
        binding.progress.visibility = View.VISIBLE
        vm.load()
    }

    private fun onItemClick(item: MediaItem) {
        if (item.isVideo) {
            val v = Intent(this, VideoPlayerActivity::class.java)
            v.putExtra("uri", item.uri.toString())
            startActivity(v)
        } else {
            val i = Intent(this, EditorActivity::class.java)
            i.putExtra("uri", item.uri.toString())
            startActivity(i)
        }
    }
}

class AlbumAdapter(private val onClick: (MediaItem) -> Unit) : RecyclerView.Adapter<AlbumVH>() {
    private val data = mutableListOf<MediaItem>()
    fun submit(list: List<MediaItem>) { data.clear(); data.addAll(list); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumVH {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumVH(binding)
    }
    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: AlbumVH, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onClick(item) }
    }
}

class AlbumVH(private val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MediaItem) {
        if (item.isVideo) {
            binding.iconPlay.visibility = View.VISIBLE
            binding.duration.visibility = View.VISIBLE
            val d = item.durationMs ?: 0L
            val sec = (d / 1000).toInt()
            val mm = (sec / 60).toString().padStart(2, '0')
            val ss = (sec % 60).toString().padStart(2, '0')
            binding.duration.text = "$mm:$ss"
        } else {
            binding.iconPlay.visibility = View.GONE
            binding.duration.visibility = View.GONE
        }
        binding.thumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
        if (item.isVideo) {
            binding.thumbnail.load(item.uri) {
                videoFrameMillis(0)
                crossfade(true)
            }
        } else {
            binding.thumbnail.load(item.uri) {
                crossfade(true)
            }
        }
    }
}
