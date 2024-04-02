package com.bf.iotcontrol.ui.matrix

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import kotlin.math.sqrt

class MatrixFragment : Fragment() {
    private lateinit var binding: FragmentMatrixBinding
    private val connectionViewModel: ConnectionViewModel by activityViewModels()
    private val matrixViewModel: MatrixViewModel by viewModels()


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
    }
}