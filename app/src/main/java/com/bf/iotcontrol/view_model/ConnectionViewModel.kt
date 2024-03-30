package com.bf.iotcontrol.view_model

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

interface PermissionListener {
    fun requestPermission(permission: String)
}

class ConnectionViewModel : ViewModel() {
    private val _pairedList = MutableLiveData<List<BluetoothDeviceDomain>>()
    val pairedList: LiveData<List<BluetoothDeviceDomain>> = _pairedList

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

    fun setupVariable(bluetoothAdapter: BluetoothAdapter, permissionListener: PermissionListener) {
        this.bluetoothAdapter = bluetoothAdapter
        listener = permissionListener
    }

    @SuppressLint("MissingPermission")
    fun queryDevice(context: Context) {
        if (!hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            listener?.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }
        val list = bluetoothAdapter?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
        _pairedList.postValue(list!!)
    }

    @SuppressLint("MissingPermission")
    fun startBluetoothServer(
        context: Context
    ): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                listener?.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(AndroidBluetoothController.SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
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
                    UUID.fromString(AndroidBluetoothController.SERVICE_UUID)
                )

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
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
    }

    fun release(context: Context) {
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }
}