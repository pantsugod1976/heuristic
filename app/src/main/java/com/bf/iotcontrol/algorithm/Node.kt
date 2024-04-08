package com.bf.iotcontrol.algorithm

import kotlin.math.min

class Node(val row: Int, val col: Int, var isWall: Boolean) {
    var g: Double = Double.MAX_VALUE // Cost from start node to current node
    var h: Double = 0.0 // Heuristic value (estimated cost from current node to goal node)
    var f: Double = 0.0 // f = g + h
    var predecessors: MutableList<Node> = mutableListOf()


    override fun toString(): String {
        return if (isWall) "#" else "."
    }

    fun getDirection() = "$row, $col"

    //Dynamic D*, AD
    var rhs: Double = Double.MAX_VALUE
    var key1: Double = Double.MAX_VALUE
    var key2: Double = Double.MAX_VALUE
    fun compareKey(other: Node): Int {
        return if (key1 < other.key1 || (key1 == other.key1 && key2 <= other.key2)) {
            -1
        } else {
            1
        }
    }

    companion object {
        var epsilon = 1.0
    }
}

class NodeComparator : Comparator<Node> {
    override fun compare(node1: Node, node2: Node): Int {
        return node1.compareKey(node2)
    }
}