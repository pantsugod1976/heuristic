package com.bf.iotcontrol.ui.matrix

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bf.iotcontrol.common.model.Attribute
import com.bf.iotcontrol.common.model.Model
import com.bf.iotcontrol.R
import com.bf.iotcontrol.algorithm.Node
import com.bf.iotcontrol.databinding.ItemLayoutBinding
import com.bf.iotcontrol.databinding.ItemRecycleLayoutBinding

interface ItemClickListener {
    fun onClick(position: Int)
}

class ItemAdapter(
    private val context: Context,
    private var list: List<Node>
): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val color =
                if (list[position].isWall) context.resources.getColor(R.color.gray, null)
                else if (list[position].isVisited) context.resources.getColor(R.color.yellow, null)
                else if (list[position].isStart) context.resources.getColor(R.color.green, null)
                else if (list[position].isGoal) context.resources.getColor(R.color.red, null)
                else context.resources.getColor(R.color.white, null)

            binding.root.setBackgroundColor(color)
        }
    }

    fun changeList(new: Node, position: Int) {
        list.toMutableList()[position] = new
        this.notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val width = context.resources.displayMetrics.widthPixels / 15
        holder.itemView.layoutParams.apply {
            this.width = width
            this.height = width
        }

        holder.bind(position)
    }
}

class GridAdapter(
    private val context: Context,
    private var images: List<List<Node>>
) : RecyclerView.Adapter<GridAdapter.ImageViewHolder>() {
    private val adapters = mutableListOf<ItemAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRecycleLayoutBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.itemView.layoutParams.apply {
            height = context.resources.displayMetrics.widthPixels / 15
        }
        holder.bind(position)
    }

    fun changeData(new: Node, row: Int, col: Int) {
        adapters[row].changeList(new, col)
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(private val binding: ItemRecycleLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val adapter = ItemAdapter(context, images[position])
            adapters.add(adapter)

            binding.recycleView.setHasFixedSize(true)
            binding.recycleView.adapter = adapter
        }
    }
}