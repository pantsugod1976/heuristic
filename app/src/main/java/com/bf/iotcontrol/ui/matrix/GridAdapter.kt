package com.bf.iotcontrol.ui.matrix

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bf.iotcontrol.common.model.Attribute
import com.bf.iotcontrol.common.model.Model
import com.bf.iotcontrol.R
import com.bf.iotcontrol.databinding.ItemLayoutBinding

interface ItemClickListener {
    fun onClick(position: Int)
}

class GridAdapter(
    private val context: Context,
    private val span: Int,
    private var images: MutableList<Model>,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<GridAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.itemView.layoutParams.apply {
            height = context.resources.displayMetrics.widthPixels / span
        }
        holder.bind(position)
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val colorRes = when (images[position].attr) {
                Attribute.WAY -> {
                    R.color.white
                }

                Attribute.START -> {
                    R.color.yellow
                }

                Attribute.WALL -> {
                    R.color.gray
                }

                else -> {
                    R.color.red
                }
            }
            binding.root.setBackgroundColor(colorRes)
            binding.root.setOnClickListener {
                val old = images.indexOfFirst { it.attr == Attribute.GOAL }
                images[old] = images[old].copy(attr = Attribute.WALL)
                images[position] = images[position].copy(attr = Attribute.GOAL)

                notifyItemChanged(old)
                notifyItemChanged(position)
            }
        }
    }
}