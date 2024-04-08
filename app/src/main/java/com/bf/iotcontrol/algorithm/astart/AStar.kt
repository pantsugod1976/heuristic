package com.bf.iotcontrol.algorithm.astart

import com.bf.iotcontrol.algorithm.Heuristics
import com.bf.iotcontrol.algorithm.Node
import java.util.PriorityQueue

class AStar(private val stateMatrix: List<List<Node>>) {
    private val open: PriorityQueue<Node> = PriorityQueue(compareBy { it.f })
    private val closed: MutableSet<Node> = HashSet()

    fun computeShortestPath(start: Node, goal: Node): Boolean {
        start.g = 0.0
        start.h = heuristic(start, goal)
        start.f = start.g + start.h
        open.add(start)

        while (open.isNotEmpty()) {
            val current = open.poll()

            if (current == goal) {
                println("Shortest path found!")
                return true
            }

            closed.add(current)

            for (rowOffset in -1..1) {
                for (colOffset in -1..1) {
                    if (rowOffset == 0 && colOffset == 0) continue // Skip current cell
                    val newRow = current.row + rowOffset
                    val newCol = current.col + colOffset

                    if (isValidCell(newRow, newCol)) {
                        val neighbor = stateMatrix[newRow][newCol]
                        if (neighbor in closed || neighbor.isWall) continue

                        val tentativeGScore = current.g + 1 // Assuming uniform edge costs

                        if (tentativeGScore < neighbor.g) {
                            neighbor.predecessors.clear()
                            neighbor.predecessors.add(current) // Store predecessor
                            neighbor.g = tentativeGScore
                            neighbor.h = heuristic(neighbor, goal)
                            neighbor.f = neighbor.g + neighbor.h

                            if (neighbor !in open) {
                                open.add(neighbor)
                            }
                        }
                    }
                }
            }
        }

        println("No path found from start to goal!")
        return false
    }

    fun showPath(start: Node, goal: Node): List<Pair<Int, Int>> {
        var current: Node? = goal
        val path = mutableListOf<Node>()

        while (current != null && current != start) {
            path.add(current)
            if (current.predecessors.isEmpty()) {
                return emptyList()
            }
            current = current.predecessors.first()
        }
        path.add(start)
        path.reverse()

        val map = path.map {
            Pair(it.row, it.col)
        }
        return map
    }

    private fun heuristic(state: Node, goal: Node): Double {
        // Replace this heuristic with your actual heuristic function
        // In this example, I'll just return a simple Manhattan distance estimate
        val dx = (state.row - goal.row).toDouble()
        val dy = (state.col - goal.col).toDouble()
        return Heuristics.manhattan(dx, dy)
    }

    private fun isValidCell(row: Int, col: Int): Boolean {
        return row in stateMatrix.indices && col in stateMatrix[row].indices
    }
}