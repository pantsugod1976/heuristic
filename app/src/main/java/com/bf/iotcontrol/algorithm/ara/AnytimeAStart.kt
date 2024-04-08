package com.bf.iotcontrol.algorithm.ara

import android.util.Log
import com.bf.iotcontrol.algorithm.Node
import java.util.PriorityQueue
import kotlin.math.pow
import kotlin.math.sqrt

class AnytimeAStar(
    private val matrix: List<List<Node>>
) {
    private lateinit var open: PriorityQueue<Node>
    private val closed = HashSet<Node>()
    private val incons = HashSet<Node>()

    private var EPSILON = 1.0

    private lateinit var startNode: Node

    private fun initialize(start: Node, goal: Node) {
        open = PriorityQueue(compareBy { it.f })

        start.g = Double.MAX_VALUE
        start.h = 0.0
        start.f = start.g + start.h * EPSILON
        startNode = start

        goal.g = 0.0
        goal.h = heuristic(startNode, goal)
        goal.f = goal.g + goal.h * EPSILON

        open.add(goal)
    }

    fun findPath(start: Node, goal: Node): List<Node> {
        initialize(start, goal)

        improvePath()

        while (EPSILON > 0) {
            decreaseEpsilon()
            moveInconsToOpen()
            updateKeys()
            closed.clear()
            improvePath()
        }

        val path = mutableListOf<Node>()
        var curPre = start.predecessors
        if (curPre.isEmpty()) {
            return emptyList()
        }
        path.addAll(listOf(start, curPre.first()))
        while (curPre.isNotEmpty()) {
            curPre = curPre.first().predecessors
            if (curPre.isNotEmpty()) path.add(curPre.first())
        }
        return path.toList()
    }

    private fun decreaseEpsilon() {
        // Decrease epsilon
        EPSILON -= 0.1
    }

    private fun moveInconsToOpen() {
        open.addAll(incons)
        incons.clear()
    }

    private fun updateKeys() {
        for (node in open) {
            node.f = node.g + EPSILON * node.h
        }
    }

    private fun improvePath() {
        while (open.isNotEmpty() && open.peek().f < startNode.f) {
            val current = open.poll()

            closed.add(current)

            for (rowOffset in -1..1) {
                for (colOffset in -1..1) {
                    if (rowOffset == 0 && colOffset == 0) continue // Skip current cell
                    val newRow = current.row + rowOffset
                    val newCol = current.col + colOffset

                    if (isValidCell(newRow, newCol)) {
                        val neighbor = matrix[newRow][newCol]
                        if (neighbor in closed || neighbor.isWall) continue

                        val tentativeGScore = current.g + 1 // Assuming uniform edge costs

                        if (tentativeGScore < neighbor.g) {
                            neighbor.predecessors.clear()
                            neighbor.predecessors.add(current) // Store predecessor
                            neighbor.g = tentativeGScore
                            neighbor.h = heuristic(startNode, neighbor)
                            neighbor.f = neighbor.g + EPSILON * neighbor.h

                            if (neighbor !in closed) {
                                open.add(neighbor)
                            } else {
                                incons.add(neighbor)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isValidCell(row: Int, col: Int): Boolean {
        return row in matrix.indices && col in matrix[row].indices
    }

    private fun heuristic(current: Node, goal: Node): Double {
        return sqrt(
            (current.row - goal.row).toDouble().pow(2) + (current.col - goal.col).toDouble().pow(2)
        )
    }
}
