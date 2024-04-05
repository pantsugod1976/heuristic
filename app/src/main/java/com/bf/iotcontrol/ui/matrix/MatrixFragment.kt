package com.bf.iotcontrol.ui.matrix

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bf.iotcontrol.R
import com.bf.iotcontrol.common.model.Attribute
import com.bf.iotcontrol.common.model.Model
import com.bf.iotcontrol.databinding.FragmentMatrixBinding
import com.bf.iotcontrol.view_model.ConnectionViewModel
import com.bf.iotcontrol.view_model.MatrixViewModel
import com.bf.iotcontrol.view_model.ResultPathFinding
import java.io.IOException
import kotlin.math.sqrt

class MatrixFragment : Fragment() {
    private lateinit var binding: FragmentMatrixBinding
    private val connectionViewModel: ConnectionViewModel by activityViewModels()
    private val matrixViewModel: MatrixViewModel by viewModels()

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
            matrixViewModel.startAlgorithm()
        }

        matrixViewModel.liveResult.observe(viewLifecycleOwner) {
            if (it is ResultPathFinding.Success) {
                var start = it.path.first()
                val list = it.path.drop(1)
                socket = connectionViewModel.acceptConnection()
                list.forEach {node ->
                    if (start.first == node.first) {
                        val ch = if (start.second < node.second) 'R'
                        else 'L'
                        controlDevice(ch)
                    } else {
                        val ch = if (start.first < node.first) 'U'
                        else 'D'
                        controlDevice(ch)
                    }

                    start = node
                }
            }
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