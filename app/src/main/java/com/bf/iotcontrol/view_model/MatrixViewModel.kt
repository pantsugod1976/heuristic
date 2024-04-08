package com.bf.iotcontrol.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bf.iotcontrol.algorithm.astart.AStar
import com.bf.iotcontrol.algorithm.Node
import com.bf.iotcontrol.algorithm.ara.AnytimeAStar
import com.bf.iotcontrol.algorithm.dstart.DStar
import com.bf.iotcontrol.algorithm.dynamic_a_start.DynamicAStart
import kotlinx.coroutines.launch

sealed class ResultPathFinding {
    data object Error : ResultPathFinding()
    class Success(val path: List<Pair<Int, Int>>): ResultPathFinding()
}

class MatrixViewModel : ViewModel() {
    //4-8 3-10 5-10
    val matrix = listOf(
        listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        listOf(1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1),
        listOf(1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1),
        listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1),
        listOf(1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
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

    private var algorithms: Int = 1
    private var goalLocation = "6, 0"

    fun createStateMatrix(matrix: List<List<Int>>): List<List<Node>> {
        return matrix.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, value ->
                Node(rowIndex, colIndex, value == 1)
            }
        }
    }

    fun setAlgorithms(choice: Int) {
        algorithms = choice
    }

    fun setGoalLocation(pos: Int) {
        goalLocation = aviNode[pos]
    }

    fun getAvailableLocation(): MutableList<String> {
        val nodeMatrix = createStateMatrix(matrix)
        nodeMatrix.forEach { list ->
            val slice = list.partition { it.isWall }
            aviNode.addAll(slice.second.map { it.getDirection() })
        }
        aviNode.removeIf { it == "6, 0" }
        return aviNode
    }

    fun startAlgorithm() {
        viewModelScope.launch {
            when (algorithms) {
                1 -> {
                    startAStart()
                }

                2 -> {
                    startDStar()
                }

                3 -> {
                    startAnytimeAStart()
                }

                else -> {
                    startAD()
                }
            }
        }
    }

    private suspend fun startAStart() {
        val copyMatrix = createStateMatrix(matrix)
        val algorithm = AStar(copyMatrix)
        val startNode = copyMatrix[6][0]
        val location = goalLocation.split(", ").toMutableList()
        location.forEach {
            it.apply {
                this.replace("(", "")
            }
        }
        val goalNode = copyMatrix[location.first().toInt()][location.last().toInt()]
        val isFound = algorithm.computeShortestPath(startNode, goalNode)
        if (isFound) {
            _liveResult.postValue(ResultPathFinding.Success(algorithm.showPath(startNode, goalNode)))
        } else _liveResult.postValue(ResultPathFinding.Error)
    }

    private suspend fun startAnytimeAStart() {
        val copyMatrix = createStateMatrix(matrix)
        val algorithm = AnytimeAStar(copyMatrix)
        val startNode = copyMatrix[6][0]
        val location = goalLocation.split(", ").toMutableList()
        location.forEach {
            it.apply {
                this.replace("(", "")
            }
        }
        val goalNode = copyMatrix[location.first().toInt()][location.last().toInt()]

        val pathToLocation = algorithm.findPath(startNode, goalNode)

        if (pathToLocation.isNotEmpty()) {
            val pairList = pathToLocation.map {
                Pair(it.row, it.col)
            }
            _liveResult.postValue(ResultPathFinding.Success(pairList))
        } else _liveResult.postValue(ResultPathFinding.Error)
    }

    private suspend fun startDStar() {
        val copyMatrix = createStateMatrix(matrix)
        val algorithm = DStar(copyMatrix)

        val startNode = copyMatrix[6][0]
        val location = goalLocation.split(", ").toMutableList()
        location.forEach {
            it.apply {
                this.replace("(", "")
            }
        }
        val goalNode = copyMatrix[location.first().toInt()][location.last().toInt()]
        val pathToLocation = algorithm.findPath(startNode, goalNode)
        if (pathToLocation.isNotEmpty()) {
            val pairList = pathToLocation.map {
                Pair(it.row, it.col)
            }
            _liveResult.postValue(ResultPathFinding.Success(pairList))
        } else _liveResult.postValue(ResultPathFinding.Error)
    }

    private suspend fun startAD() {
        val copyMatrix = createStateMatrix(matrix)
        val algorithm = DynamicAStart(copyMatrix)

        val startNode = copyMatrix[6][0]
        val location = goalLocation.split(", ").toMutableList()
        location.forEach {
            it.apply {
                this.replace("(", "")
            }
        }
        val goalNode = copyMatrix[location.first().toInt()][location.last().toInt()]
        val pathToLocation = algorithm.findPath(startNode, goalNode)
        if (pathToLocation.isNotEmpty()) {
            val pairList = pathToLocation.map {
                Pair(it.row, it.col)
            }
            _liveResult.postValue(ResultPathFinding.Success(pairList))
        } else _liveResult.postValue(ResultPathFinding.Error)
    }
}