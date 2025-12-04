package com.example.miniimageeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.miniimageeditor.databinding.ActivityCollageModeBinding

class CollageModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollageModeBinding
    private lateinit var uris: ArrayList<Uri>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollageModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uris = intent.getParcelableArrayListExtra("uris") ?: arrayListOf()
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnHorizontal.setOnClickListener {
            val i = Intent(this, CollageLinearActivity::class.java)
            i.putParcelableArrayListExtra("uris", uris)
            i.putExtra("orientation", "horizontal")
            startActivity(i)
        }
        binding.btnVertical.setOnClickListener {
            val i = Intent(this, CollageLinearActivity::class.java)
            i.putParcelableArrayListExtra("uris", uris)
            i.putExtra("orientation", "vertical")
            startActivity(i)
        }
        binding.btnFree.text = "四宫格"
        binding.btnFree.setOnClickListener {
            val i = Intent(this, CollageGridActivity::class.java)
            i.putParcelableArrayListExtra("uris", uris)
            i.putExtra("grid", 2)
            startActivity(i)
        }
        // 新增九宫格按钮
        val btn9 = android.widget.Button(this).apply {
            text = "九宫格"
            layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 48 * resources.displayMetrics.density.toInt()).apply {
                topMargin = (8 * resources.displayMetrics.density).toInt()
            }
            setOnClickListener {
                val i = Intent(this@CollageModeActivity, CollageGridActivity::class.java)
                i.putParcelableArrayListExtra("uris", uris)
                i.putExtra("grid", 3)
                startActivity(i)
            }
        }
        (binding.root as android.widget.LinearLayout).addView(btn9)
    }
}
