package com.example.miniimageeditor

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BackgroundPickerDialog(private val context: Context, private val onPick: (Int) -> Unit) {
    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_background_picker, null)
        val rv = view.findViewById<RecyclerView>(R.id.rvBg)
        rv.layoutManager = GridLayoutManager(context, 4)
        val data = listOf(R.drawable.bg_paper, R.drawable.bg_gray)
        rv.adapter = BgAdapter(data) { resId ->
            onPick(resId)
        }
        AlertDialog.Builder(context).setTitle("选择画布背景").setView(view)
            .setNegativeButton("取消", null).show()
    }

    private class BgAdapter(private val items: List<Int>, private val onClick: (Int) -> Unit) :
        RecyclerView.Adapter<BgVH>() {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): BgVH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_bg_thumb, parent, false)
            return BgVH(v)
        }
        override fun onBindViewHolder(holder: BgVH, position: Int) {
            val resId = items[position]
            holder.iv.setImageResource(resId)
            holder.itemView.setOnClickListener { onClick(resId) }
        }
        override fun getItemCount(): Int = items.size
    }

    private class BgVH(v: android.view.View) : RecyclerView.ViewHolder(v) {
        val iv: android.widget.ImageView = v.findViewById(R.id.ivBg)
    }
}

