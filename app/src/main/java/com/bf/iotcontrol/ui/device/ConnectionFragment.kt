package com.bf.iotcontrol.ui.device

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bf.iotcontrol.bluetooth_controller.BluetoothDevice
import com.bf.iotcontrol.bluetooth_controller.BluetoothDeviceDomain
import com.bf.iotcontrol.databinding.FragmentConnectionBinding
import com.bf.iotcontrol.ui.matrix.ItemClickListener
import com.bf.iotcontrol.view_model.ConnectionViewModel
import com.bf.iotcontrol.view_model.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class ConnectionFragment : Fragment(), ItemClickListener, PermissionListener {
    private lateinit var binding: FragmentConnectionBinding
    private val viewModel: ConnectionViewModel by viewModels()

    private val bluetoothManager by lazy { requireContext().getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager.adapter }

    private var list: List<BluetoothDeviceDomain> = emptyList()
    private lateinit var adapter: LinearAdapter

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            viewModel.queryDevice(requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setupVariable(bluetoothAdapter, this)

        val devices = listOf<BluetoothDevice>()
        adapter = LinearAdapter(devices, this)

        binding.recycleView.apply {
            setHasFixedSize(true)
            this.adapter = adapter
        }

        viewModel.pairedList.observe(viewLifecycleOwner) { list ->
            this.list = list
            adapter.changeDataSet(this.list)
            binding.recycleView.adapter = adapter
        }

        if (context?.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            viewModel.queryDevice(requireContext())
        } else {
            requestPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    override fun onClick(position: Int) {
        viewModel.connectToDevice(list[position], requireContext())
    }

    override fun requestPermission(permission: String) {
        Toast.makeText(this.context, "App need your permission for function to work properly", Toast.LENGTH_SHORT).show()
        requestPermission.launch(permission)
    }
}