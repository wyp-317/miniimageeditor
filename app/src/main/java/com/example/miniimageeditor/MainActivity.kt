package com.example.miniimageeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniimageeditor.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val i = Intent(this, EditorActivity::class.java)
            i.putExtra("uri", uri.toString())
            startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zh-CN"))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnImport.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
        }
        binding.btnOpenAlbum.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
        }
        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.btnBeautify.setOnClickListener {
            startActivity(Intent(this, AlbumActivity::class.java))
        }

        binding.btnAiPortrait.setOnClickListener {
            startActivity(Intent(this, CollageSelectActivity::class.java))
        }

        val adapter = FeatureAdapter(listOf(
            FeatureItem("批量修图", R.drawable.ic_feature_batch),
            FeatureItem("画质超清", R.drawable.ic_feature_hd),
            FeatureItem("魔法消除", R.drawable.ic_feature_magic),
            FeatureItem("智能抠图", R.drawable.ic_feature_cutout),
            FeatureItem("AI修图", R.drawable.ic_feature_ai),
            FeatureItem("一键消除", R.drawable.ic_feature_erase),
            FeatureItem("瘦脸瘦身", R.drawable.ic_feature_face),
            FeatureItem("所有工具", R.drawable.ic_feature_all)
        ))
        binding.featureGrid.layoutManager = GridLayoutManager(this, 4)
        binding.featureGrid.adapter = adapter
        val space = resources.getDimensionPixelSize(R.dimen.space_sm)
        binding.featureGrid.addItemDecoration(com.example.miniimageeditor.ui.SpaceItemDecoration(space))
    }
}

data class FeatureItem(val label: String, val iconRes: Int)

class FeatureAdapter(private val features: List<FeatureItem>) : RecyclerView.Adapter<FeatureVH>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FeatureVH {
        val v = com.example.miniimageeditor.databinding.ItemFeatureBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return FeatureVH(v)
    }
    override fun getItemCount() = features.size
    override fun onBindViewHolder(holder: FeatureVH, position: Int) {
        val item = features[position]
        holder.binding.label.text = item.label
        holder.binding.icon.setImageResource(item.iconRes)
    }
}

class FeatureVH(val binding: com.example.miniimageeditor.databinding.ItemFeatureBinding) : RecyclerView.ViewHolder(binding.root)
