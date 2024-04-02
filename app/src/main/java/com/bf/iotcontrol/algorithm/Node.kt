package com.bf.iotcontrol.algorithm

class Node(val row: Int, val col: Int, var isWall: Boolean) {
    var g: Double = Double.MAX_VALUE // Cost from start node to current node
    var h: Double = 0.0 // Heuristic value (estimated cost from current node to goal node)
    var f: Double = 0.0 // f = g + h
    var predecessors: MutableList<Node> = mutableListOf()

    override fun toString(): String {
        return if (isWall) "#" else "."
    }

    fun getDirection() = "$row, $col"
}