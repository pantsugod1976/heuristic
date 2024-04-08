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
    private val incons = HashSet<Node>()

    private var EPSILON = 1.0

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
        for (rowOffset in -1..1) {
            for (colOffset in -1..1) {
                if (rowOffset == 0 && colOffset == 0) continue // Skip current cell
                val newRow = node.row + rowOffset
                val newCol = node.col + colOffset

                if (isValidCell(newRow, newCol)) {
                    val neighbor = matrix[newRow][newCol]
                    if (neighbor.isWall || neighbor == goalNode) continue

                    neighbor.predecessors.clear()
                    neighbor.predecessors.add(node)
                    neighbors.add(neighbor)
                }
            }
        }
        return neighbors
    }

    private fun initialize(start: Node, goal: Node) {
        open = PriorityQueue(NodeComparator())

        start.g = Double.MAX_VALUE
        start.rhs = Double.MAX_VALUE
        start.h = 0.0
        startNode = start

        goal.g = Double.MAX_VALUE
        goal.h = heuristic(startNode, goal)
        goal.rhs = 0.0
        goalNode = goal

        startNode.apply {
            key1 = if (g > rhs) {
                min(g, rhs) + h * Node.epsilon
            } else min(g, rhs) + h

            key2 = min(g, rhs)
        }

        goalNode.apply {
            key1 = if (g > rhs) {
                min(g, rhs) + h * Node.epsilon
            } else min(g, rhs) + h

            key2 = min(g, rhs)
        }


        open.add(goal)

        matrix.forEach {
            it.forEach { node ->
                if (!node.isWall && node != goal && node != start) {
                    node.h = heuristic(start, node)
                }
            }
        }

        matrix.forEach {
            it.forEach { node ->
                if (!node.isWall && node != goal && node != start) {
                    val neighbors = getNeighbors(node)
                    if (neighbors.isNotEmpty()) node.rhs = neighbors.peek().g + 1
                }
            }
        }

        Node.epsilon = EPSILON
    }

    private fun updateState(node: Node) {
        if (node != goalNode) {
            node.rhs = getNeighbors(node).peek().g + 1
        }

        if (node in open) open.remove(node)

        if (node.g != node.rhs) {
            if (node !in closed) {
                node.h = heuristic(startNode, node)
                node.rhs = getNeighbors(node).peek().g + 1
                node.key1 = if (node.g > node.rhs) {
                    min(node.g, node.rhs) + node.h * Node.epsilon
                } else min(node.g, node.rhs) + node.h
                node.key2 = min(node.g, node.rhs)
                open.add(node)
            } else {
                incons.add(node)
            }
        }
    }

    private fun computeImprovePath(): Boolean {
        while ((open.isNotEmpty() && open.peek().compareKey(startNode) == -1) || startNode.rhs != startNode.g) {
            val current = open.poll()

            val neighbors = getNeighbors(current)
            if (current.g > current.rhs) {
                current.g = current.rhs
                closed.add(current)
                if (current == startNode) return false


            } else {
                current.g = Double.MAX_VALUE
                updateState(current)

            }

            neighbors.forEach {
                updateState(it)
            }

            return true
        }
        return false
    }

    private fun updateKey() {
        open.forEach {
            val node = it
            node.h = heuristic(startNode, node)
            node.rhs = getNeighbors(node).peek().g + 1
            node.key1 = if (node.g > node.rhs) {
                min(node.g, node.rhs) + node.h * Node.epsilon
            } else min(node.g, node.rhs) + node.h
            node.key2 = min(node.g, node.rhs)
        }

        val altOpen = PriorityQueue(NodeComparator())
        altOpen.addAll(open)

        open = altOpen
    }

    fun findPath(start: Node, goal: Node): List<Node> {
        initialize(start, goal)

        computeImprovePath()

        var continueLoop = true
        while (continueLoop) {
            if (EPSILON > 0.1) {
                decreaseEpsilon()
            }

            moveInconsToOpen()

            updateKey()

            closed.clear()

            continueLoop = computeImprovePath()
        }

        var curPre = start.predecessors
        if (curPre.isEmpty()) return emptyList()
        val path = mutableListOf<Node>()
        path.addAll(listOf(start, curPre.first()))
        while (curPre.isNotEmpty()) {
            curPre = curPre.first().predecessors
            if (curPre.isNotEmpty()) path.add(curPre.first())
        }
        return path
    }

    private fun decreaseEpsilon() {
        // Decrease epsilon
        EPSILON -= 0.1
        Node.epsilon = EPSILON
    }

    private fun moveInconsToOpen() {
        open.addAll(incons)
        incons.clear()
    }
}