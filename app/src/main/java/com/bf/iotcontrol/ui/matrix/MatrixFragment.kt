package com.bf.iotcontrol.ui.matrix

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bf.iotcontrol.R
import com.bf.iotcontrol.algorithm.Node
import com.bf.iotcontrol.common.model.Attribute
import com.bf.iotcontrol.common.model.Model
import com.bf.iotcontrol.databinding.FragmentMatrixBinding
import com.bf.iotcontrol.view_model.ConnectionViewModel
import com.bf.iotcontrol.view_model.MatrixViewModel
import com.bf.iotcontrol.view_model.ResultPathFinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.math.sqrt

@AndroidEntryPoint
class MatrixFragment : Fragment() {
    private lateinit var binding: FragmentMatrixBinding
    private val connectionViewModel: ConnectionViewModel by viewModels()

    private val matrixViewModel: MatrixViewModel by viewModels()

    private lateinit var matrixAdapter: GridAdapter
    private lateinit var recycleMatrix: List<List<Node>>
    private var socket: BluetoothSocket? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMatrixBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = matrixViewModel

        binding.btnReset.setOnClickListener {
            recycleMatrix = matrixViewModel.createStateMatrix(matrixViewModel.matrix)
        }

        controlDevice('S')

        val adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, matrixViewModel.getAvailableLocation())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                matrixViewModel.setGoalLocation(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.btnStart.setOnClickListener {
            recycleMatrix = matrixViewModel.createStateMatrix(matrixViewModel.matrix)
            matrixAdapter.changeList(recycleMatrix)
            matrixViewModel.startAlgorithm()
        }

        matrixViewModel.liveResult.observe(viewLifecycleOwner) {
            if (it is ResultPathFinding.Success) {
                val start = it.path.first()
                val node1 = recycleMatrix[start.first][start.second]
                node1.isStart = true

                val goal = it.path.last()
                val node2 = recycleMatrix[goal.first][goal.second]
                node2.isGoal = true

                matrixAdapter.changeData(node1, node1.row, node1.col)
                matrixAdapter.changeData(node2, node2.row, node2.col)

                var list = it.path.drop(1)
                //list = list.dropLast(1)
                connectionViewModel.acceptConnection()
                socket = connectionViewModel.clientSocket()
                var current = start

                //hướng đầu xe theo matrix
                // 0: Mặc định trái sang phải
                // 1: Oy tăng hướng lên trên
                // -1: Oy giảm hướng xuống dưới
                // 2: Ox giảm hướng phải sang trái
                var head = 0
                val leftDelayTime = 1200L
                val rightDelayTime = 1400L
                var delayTime = rightDelayTime
                lifecycleScope.launch {
                    for (node in list) {
                        delay(2000)
                        //control phải cập nhật theo đầu xe quay hướng nào =))
                        when(head) {
                            0 -> {
                                if (current.first == node.first) {
                                    // y1 < y2 ? L : R
                                    //cập nhật lại head
                                    val ch = if (current.second < node.second) {
                                        head = 1
                                        delayTime = leftDelayTime
                                        'R'
                                    }
                                    else {
                                        head = -1
                                        delayTime = rightDelayTime
                                        'L'
                                    }
                                    controlDevice(ch)
                                    delay(delayTime)
                                    controlDevice('F')
                                } else {

                                    val ch = if (current.first < current.first) 'F'
                                    else 'F'
                                    controlDevice(ch)
                                }
                            }

                            -1 -> {
                                if (current.second == node.second) {
                                    //x1 < x2 ? L : R
                                    val ch = if (current.first < node.first) {
                                        head = 0
                                        delayTime = leftDelayTime
                                        'R'
                                    }
                                    else {
                                        head = 2
                                        delayTime = rightDelayTime
                                        'L'
                                    }
                                    controlDevice(ch)
                                    delay(delayTime)
                                    controlDevice('F')
                                } else {
                                    val ch = if (current.second < node.second) {
                                        'F'
                                    }
                                    else {
                                        'F'
                                    }
                                    controlDevice(ch)
                                }
                            }

                            1 -> {
                                if (current.second == node.second) {
                                    //x1 < x2 ? R : L
                                    val ch = if (current.first < node.first) {
                                        head = 0
                                        delayTime = rightDelayTime
                                        'L'
                                    }
                                    else {
                                        head = 2
                                        delayTime = leftDelayTime
                                        'R'
                                    }
                                    controlDevice(ch)
                                    delay(delayTime)
                                    controlDevice('F')
                                } else {
                                    val ch = if (current.second < node.second) {
                                        'F'
                                    }
                                    else {
                                        'F'
                                    }
                                    controlDevice(ch)
                                }
                            }

                            2 -> {
                                if (current.first == node.first) {
                                    // y1 < y2 ? R : L
                                    //cập nhật lại head
                                    val ch = if (current.second < node.second) {
                                        head = 1
                                        delayTime = rightDelayTime
                                        'L'
                                    }
                                    else {
                                        head = -1
                                        delayTime = leftDelayTime
                                        'R'
                                    }
                                    controlDevice(ch)
                                    delay(delayTime)
                                    controlDevice('F')
                                } else {
                                    //tiến lùi không cần cập nhật trừ khi thằng loz để lùi là quay đầu xe xoas ddi dme m =))

                                    val ch = if (current.first < current.first) 'F'
                                    else 'F'
                                    controlDevice(ch)
                                }
                            }
                        }
                        current = node

                        //visualize
                        val item = recycleMatrix[node.first][node.second]
                        item.isVisited = true
                        matrixAdapter.changeData(item, item.row, item.col)
                    }
                    controlDevice('S')
                }
                controlDevice('S')
            }
        }

        recycleMatrix = matrixViewModel.createStateMatrix(matrixViewModel.matrix)
        matrixAdapter = GridAdapter(requireContext(), recycleMatrix)

        binding.recycleView.apply {
            setHasFixedSize(true)
            this.adapter = matrixAdapter
        }
    }

    private fun controlDevice(ch: Char) {
        val os = socket?.outputStream
        os?.let {
            try {
                // Convert your data to bytes (e.g., if it's a String)
                val dataBytes = ch.toString().toByteArray()

                // Write data to the OutputStream
                os.write(dataBytes)
                os.flush() // Flush the OutputStream to ensure data is sent immediately
                // Optionally, you can call os.close() to close the OutputStream after sending data
            } catch (e: IOException) {
                // Handle IOException
                Toast.makeText(this.context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}