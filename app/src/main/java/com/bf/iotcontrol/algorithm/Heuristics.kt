package com.bf.iotcontrol.algorithm

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class Heuristics {
    companion object {
        fun manhattan(dx: Double, dy: Double): Double {
            return abs(dx) + abs(dy)
        }

        fun euclidean(dx: Double, dy: Double): Double {
            return sqrt(dx * dx + dy * dy)
        }

        fun octile(dx: Double, dy: Double): Double {
            val F = sqrt(2.0) - 1
            return if (dx < dy) F * dx + dy else F * dy + dx
        }

        fun chebyshev(dx: Double, dy: Double): Double {
            return max(dx, dy)
        }
    }
}