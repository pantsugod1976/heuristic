package com.bf.iotcontrol.ui.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bf.iotcontrol.bluetooth_controller.BluetoothDevice
import com.bf.iotcontrol.bluetooth_controller.BluetoothDeviceDomain
import com.bf.iotcontrol.databinding.ItemDeviceInfoBinding
import com.bf.iotcontrol.ui.matrix.ItemClickListener

class LinearAdapter(
    private var devices: List<BluetoothDeviceDomain>,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<LinearAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemDeviceInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.device = devices[position]

            binding.root.setOnClickListener {
                listener.onClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceInfoBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun changeDataSet(list: List<BluetoothDeviceDomain>) {
        this.devices = list
    }
}