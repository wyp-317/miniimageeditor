package com.example.miniimageeditor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.miniimageeditor.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : ComponentActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val uriString = intent.getStringExtra("uri") ?: return
        initPlayer(Uri.parse(uriString))
    }

    private fun initPlayer(uri: Uri) {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        player!!.setMediaItem(MediaItem.fromUri(uri))
        player!!.prepare()
        player!!.play()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
