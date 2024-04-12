package com.bf.iotcontrol.algorithm.dynamic_a_start

import com.bf.iotcontrol.algorithm.Heuristics
import com.bf.iotcontrol.algorithm.Node
import com.bf.iotcontrol.algorithm.NodeComparator
import java.util.PriorityQueue
import kotlin.math.min

class DynamicAStart(
    private val matrix: List<List<Node>>
) {
    private lateinit var open: PriorityQueue<Node>
    private val closed = HashSet<Node>()

    private lateinit var startNode: Node
    private lateinit var goalNode: Node

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

    private fun getNeighbors(node: Node): PriorityQueue<Node> {
        val neighbors = PriorityQueue(NodeComparator())
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

    private fun initialize(start: Node, goal: Node) {
        open = PriorityQueue(NodeComparator())

        start.g = 0.0
        start.h = heuristic(start, goal)
        startNode = start

        goalNode = goal

        open.add(start)
    }

    private fun updateState(node: Node) {
        val neighbors = getNeighbors(node)

        for (neighbor in neighbors) {
            val tentativeGScore = node.g + 1 // Assuming each step cost is 1

            if (tentativeGScore < neighbor.g) {
                neighbor.predecessors.clear()
                neighbor.predecessors.add(node)
                neighbor.g = tentativeGScore
                neighbor.h = heuristic(neighbor, goalNode)
                if (neighbor !in open) {
                    open.add(neighbor)
                }
            }
        }
    }

    private fun computePath(): Boolean {
        if (open.isEmpty()) return false

        val current = open.poll()
        if (current == goalNode) return false

        closed.add(current)

        updateState(current)

        return true
    }

    fun findPath(start: Node, goal: Node): List<Node> {
        initialize(start, goal)

        var continueLoop = true
        while (continueLoop) {
            continueLoop = computePath()
        }

        // Reconstruct path
        val path = mutableListOf<Node>()
        var current: Node? = goalNode
        while (current != null && current != startNode) {
            path.add(current)
            current = current.predecessors.firstOrNull()
        }
        path.add(startNode)
        path.reverse()

        return path
    }
}
