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
    private val open = PriorityQueue(NodeComparator())
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

    private fun getNeighbors(node: Node): PriorityQueue<Node> {
        val neighbors = PriorityQueue<Node>(Comparator { o1, o2 ->
            if((o1.g + 1) < (o2.g + 1)) return@Comparator -1 else return@Comparator 1 })
        for (rowOffset in -1..1) {
            val colOffset = 0
            if (rowOffset == 0) continue // Skip current cell
            val newRow = node.row + rowOffset
            val newCol = node.col + colOffset

            if (isValidCell(newRow, newCol)) {
                val neighbor = matrix[newRow][newCol]
                if (neighbor.isWall) continue
                neighbors.add(neighbor)
            }
        }

        for (colOffset in -1..1) {
            val rowOffset = 0
            if (colOffset == 0) continue // Skip current cell
            val newRow = node.row + rowOffset
            val newCol = node.col + colOffset

            if (isValidCell(newRow, newCol)) {
                val neighbor = matrix[newRow][newCol]
                if (neighbor.isWall) continue
                neighbors.add(neighbor)
            }
        }
        return neighbors
    }

    private fun updateKey() {
        val altOpen = mutableListOf<Node>()
        altOpen.addAll(open)
//        altOpen.forEach {node ->
//            node.key1 = min(node.g, node.rhs) + node.h
//            node.key2 = min(node.g, node.rhs)
//        }

        open.clear()
        open.addAll(altOpen)
    }

    private fun updateState(node: Node) {
        if (!node.isVisited) node.g = Double.MAX_VALUE

        if (node != goal) {
            node.rhs = getNeighbors(node).peek().g + 1
        }

        if (node in open) open.remove(node)

        if (node.g != node.rhs) {
            node.key1 = min(node.g, node.rhs) + node.h
            node.key2 = min(node.g, node.rhs)
            open.add(node)
        }
    }

    private fun computeShortPath(): Boolean {
        while (open.peek().compareKey(start) == -1 || start.g != start.rhs) {
            val current = open.poll()
            current.isVisited = true

            if (current == start) return false

            val neightbors = getNeighbors(current)

            if (current.g > current.rhs) {
                current.g = current.rhs
            } else {
                current.g = Double.MAX_VALUE
                updateState(current)
            }

            neightbors.forEach {
                updateState(it)
            }

            updateKey()

            close.add(current)

            return true
        }
        return false
    }

    private fun initialize(start: Node, goal: Node) {
        //g: from s to goal
        start.g = Double.MAX_VALUE
        start.h = 0.0
        start.rhs = Double.MAX_VALUE
        this.start = start

        goal.g = Double.MAX_VALUE
        goal.h = heuristic(start, goal)
        goal.rhs = 0.0
        this.goal = goal

        open.add(goal)

        matrix.forEach {
            it.all { node ->
                node.h = heuristic(start, node)
                if (!node.isWall && node != goal && node != start) {
                    node.g = 0.0
                    node.key1 = min(node.g, node.rhs) + node.h
                    node.key2 = min(node.g, node.rhs)
                }
                true
            }
        }

        this.start.key1 = min(this.start.g, this.start.rhs) + this.start.h
        this.start.key2 = min(this.start.g, this.start.rhs)

        this.goal.key1 = min(this.goal.g, this.goal.rhs) + this.goal.h
        this.goal.key2 = min(this.goal.g, this.goal.rhs)
    }

    fun findPath(start: Node, goal: Node): List<Node> {
        initialize(start, goal)

        var continueLoop = true
        while (continueLoop) {
            continueLoop = computeShortPath()
            Log.d("LOOP", "CONTINUE")
        }
        Log.d("LOOP", "End Loop")
        var curPre = start.predecessors
        if (curPre.isEmpty()) return emptyList()
        path.addAll(listOf(start, curPre.first()))
        while (curPre.isNotEmpty()) {
            curPre = curPre.first().predecessors
            if (curPre.isNotEmpty()) path.add(curPre.first())
        }
        return path
    }
}