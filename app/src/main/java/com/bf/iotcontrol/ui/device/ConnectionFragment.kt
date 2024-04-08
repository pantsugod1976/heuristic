package com.bf.iotcontrol.ui.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bf.iotcontrol.bluetooth_controller.BluetoothDevice
import com.bf.iotcontrol.bluetooth_controller.BluetoothDeviceDomain
import com.bf.iotcontrol.bluetooth_controller.ConnectionResult
import com.bf.iotcontrol.databinding.FragmentConnectionBinding
import com.bf.iotcontrol.ui.matrix.ItemClickListener
import com.bf.iotcontrol.view_model.BluetoothConnection
import com.bf.iotcontrol.view_model.ConnectionViewModel
import com.bf.iotcontrol.view_model.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionFragment : Fragment(), ItemClickListener, PermissionListener {
    private lateinit var binding: FragmentConnectionBinding
    private val viewModel: ConnectionViewModel by activityViewModels()


    private val bluetoothManager by lazy { requireContext().getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager.adapter }

    private var list: List<BluetoothDeviceDomain> = emptyList()
    private lateinit var adapter: LinearAdapter

    @Inject
    lateinit var obj: BluetoothConnection

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                viewModel.queryDevice(requireContext())
            }
        }

    private var socket: BluetoothSocket? = null

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

        binding.btnDiscover.setOnClickListener {
            val requestCode = 1;
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, requestCode)
        }

        binding.btnStart.setOnClickListener {
            viewModel.acceptConnection()
        }

        binding.btnStop.setOnClickListener {
            controlDevice('S')
            viewModel.stopConnection()
        }

        binding.control.apply {
            btnDown.setOnClickListener {
                controlDevice('D')
            }

            btnLeft.setOnClickListener {
                controlDevice('L')
            }

            btnRight.setOnClickListener {
                controlDevice('R')
            }

            btnUp.setOnClickListener {
                controlDevice('U')
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

    override fun onClick(position: Int) {
        val resultFlow = viewModel.connectToDevice(list[position], requireContext())
        lifecycleScope.launch {
            resultFlow.collect {
                when (it) {
                    is ConnectionResult.Error -> {
                        Toast.makeText(
                            this@ConnectionFragment.context,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        viewModel.acceptConnection()
                        // findNavController().navigate(ConnectionFragmentDirections.actionConnectionFragmentToMatrixFragment())
                        Toast.makeText(
                            this@ConnectionFragment.context,
                            "Connect success",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun requestPermission(permission: String) {
        Toast.makeText(
            this.context,
            "App need your permission for function to work properly",
            Toast.LENGTH_SHORT
        ).show()
        requestPermission.launch(permission)
    }
}