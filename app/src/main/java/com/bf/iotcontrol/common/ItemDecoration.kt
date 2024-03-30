package com.bf.iotcontrol.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration(
    context: Context,
    private val lineWidth: Int,
    @ColorInt private val lineColor: Int
) : RecyclerView.ItemDecoration() {

    private val linePaint = Paint()

    init {
        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = lineWidth.toFloat()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val column = position % spanCount
            val row = position / spanCount

            // Draw lines around the image
            val left = child.left.toFloat()
            val top = child.top.toFloat()
            val right = child.right.toFloat()
            val bottom = child.bottom.toFloat()

            // Draw vertical lines except for the last column
            if (column < spanCount - 1) {
                val startX = right
                val startY = top
                val endX = right
                val endY = bottom
                c.drawLine(startX, startY, endX, endY, linePaint)
            }

            // Draw horizontal lines except for the last row
            if (row < (parent.adapter?.itemCount ?: 0) / spanCount) {
                val startX = left
                val startY = bottom
                val endX = right
                val endY = bottom
                c.drawLine(startX, startY, endX, endY, linePaint)
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        val spacing = lineWidth

        // Offset for vertical lines
        outRect.right = spacing

        // Offset for horizontal lines
        val itemCount = parent.adapter?.itemCount ?: 0
        if (position < itemCount - spanCount) {
            outRect.bottom = spacing
        }
    }
}