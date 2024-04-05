package com.bf.iotcontrol.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bf.iotcontrol.algorithm.AStar
import com.bf.iotcontrol.algorithm.Node

sealed class ResultPathFinding {
    data object Error : ResultPathFinding()
    class Success(val path: List<Pair<Int, Int>>): ResultPathFinding()
}

class MatrixViewModel : ViewModel() {
    val matrix = listOf(
        listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        listOf(1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1),
        listOf(1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1),
        listOf(0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1),
        listOf(1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1),
        listOf(1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1),
        listOf(1, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1),
        listOf(1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 1),
        listOf(1, 1, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1),
        listOf(1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1),
        listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    )

    private val _liveResult = MutableLiveData<ResultPathFinding>()
    val liveResult: LiveData<ResultPathFinding> get() = _liveResult

    private val aviNode = mutableListOf<String>()
    private var nodeMatrix: List<List<Node>> = listOf()

    private var heuristics: Int = 1
    private var goalLocation = "6, 0"

    private fun createStateMatrix(matrix: List<List<Int>>): List<List<Node>> {
        return matrix.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, value ->
                Node(rowIndex, colIndex, value == 1)
            }
        }
    }

    fun setHeuristics(choice: Int) {
        heuristics = choice
    }

    fun setGoalLocation(pos: Int) {
        goalLocation = aviNode[pos]
    }

    fun getAvailableLocation(): MutableList<String> {
        nodeMatrix = createStateMatrix(matrix)
        nodeMatrix.forEach { list ->
            val slice = list.partition { it.isWall }
            aviNode.addAll(slice.second.map { it.getDirection() })
        }
        aviNode.removeIf { it == "6, 0" }
        return aviNode
    }

    fun startAlgorithm() {
        val algorithm = AStar(nodeMatrix, heuristics)
        val startNode = nodeMatrix[6][0]
        val location = goalLocation.split(", ").toMutableList()
        location.forEach {
            it.apply {
                this.replace("(", "")
            }
        }
        val goalNode = nodeMatrix[location.first().toInt()][location.last().toInt()]
        val isFound = algorithm.computeShortestPath(startNode, goalNode)
        if (isFound) {
            _liveResult.postValue(ResultPathFinding.Success(algorithm.showPath(startNode, goalNode)))
        } else _liveResult.postValue(ResultPathFinding.Error)
    }
}