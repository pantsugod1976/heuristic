package com.bf.iotcontrol.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bf.iotcontrol.bluetooth_controller.AndroidBluetoothController
import com.bf.iotcontrol.bluetooth_controller.BluetoothDeviceDomain
import com.bf.iotcontrol.bluetooth_controller.BluetoothStateReceiver
import com.bf.iotcontrol.bluetooth_controller.ConnectionResult
import com.bf.iotcontrol.bluetooth_controller.toBluetoothDeviceDomain
import com.bf.iotcontrol.ui.matrix.GridAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

interface PermissionListener {
    fun requestPermission(permission: String)
}

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val bluetoothConnection: BluetoothConnection
) : ViewModel() {
    private val _pairedList = MutableLiveData<List<BluetoothDeviceDomain>>()
    val pairedList: LiveData<List<BluetoothDeviceDomain>> = _pairedList

    fun setupVariable(bluetoothAdapter: BluetoothAdapter) {
        bluetoothConnection.setupAdapter(bluetoothAdapter)
    }
    fun queryDevice(context: Context) {
        val list = bluetoothConnection.queryDevice(context)
        _pairedList.postValue(list)
    }

    fun acceptConnection() {
        bluetoothConnection.acceptConnection()
    }

    fun stopConnection() {
        bluetoothConnection.stopConnection()
    }

    fun changeListener(listener: PermissionListener) {
        bluetoothConnection.setupListener(listener)
    }

    fun clientSocket() = bluetoothConnection.getCurrentClientSocket()

    fun connectToDevice(bluetoothDevice: BluetoothDeviceDomain, context: Context): Flow<ConnectionResult> = bluetoothConnection.connectToDevice(bluetoothDevice, context)
}