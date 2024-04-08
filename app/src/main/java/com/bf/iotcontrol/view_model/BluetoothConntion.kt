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
import com.bf.iotcontrol.bluetooth_controller.AndroidBluetoothController
import com.bf.iotcontrol.bluetooth_controller.BluetoothDeviceDomain
import com.bf.iotcontrol.bluetooth_controller.BluetoothStateReceiver
import com.bf.iotcontrol.bluetooth_controller.ConnectionResult
import com.bf.iotcontrol.bluetooth_controller.toBluetoothDeviceDomain
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Singleton

@Singleton
class BluetoothConnection {
    private var listener: PermissionListener? = null

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    fun setupAdapter(bluetoothAdapter: BluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter
    }

    fun setupListener(listener: PermissionListener) {
        this.listener = listener
    }

    @SuppressLint("MissingPermission")
    fun queryDevice(context: Context): List<BluetoothDeviceDomain> {
        if (!hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            listener?.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
            return emptyList()
        }
        val list = bluetoothAdapter?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
        return list ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(
        device: BluetoothDeviceDomain,
        context: Context
    ): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                listener?.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    //Default UUID
//                    UUID.fromString(AndroidBluetoothController.SERVICE_UUID)

                    //UUID for SSP connect device
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                )

            Log.d("UUID", AndroidBluetoothController.SERVICE_UUID)

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error(e.message ?: "Interrupt connection"))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
        listener = null
        bluetoothAdapter = null
    }

    fun acceptConnection() {
        currentServerSocket?.accept()

    }

    fun stopConnection() = currentClientSocket?.close()

    fun release(context: Context) {
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }
}