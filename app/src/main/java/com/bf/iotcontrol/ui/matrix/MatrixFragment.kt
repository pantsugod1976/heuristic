package com.bf.iotcontrol.ui.matrix

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.bf.iotcontrol.R
import com.bf.iotcontrol.common.model.Attribute
import com.bf.iotcontrol.common.model.Model
import com.bf.iotcontrol.databinding.FragmentMatrixBinding
import com.bf.iotcontrol.view_model.ConnectionViewModel
import kotlin.math.sqrt

class MatrixFragment : Fragment(), ItemClickListener {
    private lateinit var binding: FragmentMatrixBinding
    private val viewModel: ConnectionViewModel by activityViewModels()

    private val list = List(64) {
        Model()
    }

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

        val layoutManager = GridLayoutManager(context, sqrt(list.size.toDouble()).toInt())
        val adapter = GridAdapter(requireContext(), sqrt(list.size.toDouble()).toInt(), list.toMutableList(), this)

        binding.recycleView.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
        }
    }

    override fun onClick(position: Int) {
    }
}