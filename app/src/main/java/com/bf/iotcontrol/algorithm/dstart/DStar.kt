package com.bf.iotcontrol.algorithm.dstart

import android.util.Log
import com.bf.iotcontrol.algorithm.Heuristics
import com.bf.iotcontrol.algorithm.Node
import com.bf.iotcontrol.algorithm.NodeComparator
import java.util.PriorityQueue
import kotlin.math.min

class DStar(
    private val matrix: List<List<Node>>
) {
    private val open = PriorityQueue<Node>(NodeComparator())
    private val close: MutableSet<Node> = HashSet()

    private lateinit var start: Node
    private lateinit var goal: Node

    private val path = mutableListOf<Node>()

    private fun heuristic(state: Node, goal: Node): Double {
        // Replace this heuristic with your actual heuristic function
        // In this example, I'll just return a simple Manhattan distance estimate
        val dx = (state.row - goal.row).toDouble()
        val dy = (state.col - goal.col).toDouble()
        return Heuristics.manhattan(dx, dy)
    }

    private fun isValidCell(row: Int, col: Int): Boolean {
        return row in matrix.indices && col in matrix[row].indices
    }

    private fun getNeighbors(node: Node): List<Node> {
        val neighbors = mutableListOf<Node>()
        val offsets = arrayOf(intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1))

        for (offset in offsets) {
            val newRow = node.row + offset[0]
            val newCol = node.col + offset[1]

            if (isValidCell(newRow, newCol)) {
                val neighbor = matrix[newRow][newCol]
                if (!neighbor.isWall) {
                    neighbors.add(neighbor)
                }
            }
        }
        return neighbors
    }

    private fun computeShortPath(): Boolean {
        if (open.isEmpty()) return false

        val current = open.poll()
        current.isVisited = true

        if (current == goal) return false

        for (neighbor in getNeighbors(current)) {
            if (!close.contains(neighbor)) {
                if (!open.contains(neighbor)) {
                    neighbor.predecessors = mutableListOf(current)
                    open.add(neighbor)
                } else {
                    neighbor.predecessors.add(current)
                }
            }
        }

        close.add(current)

        return true
    }

    private fun initialize(start: Node, goal: Node) {
        this.start = start
        this.goal = goal

        open.add(start)
    }

    fun findPath(start: Node, goal: Node): List<Node> {
        initialize(start, goal)

        var continueLoop = true
        while (continueLoop) {
            continueLoop = computeShortPath()
            Log.d("LOOP", "CONTINUE")
        }
        Log.d("LOOP", "End Loop")

        // Reconstruct path
        var current: Node? = goal
        while (current != null && current != start) {
            path.add(current)
            current = current.predecessors.firstOrNull()
        }
        path.add(start)
        path.reverse()

        return path
    }
}
