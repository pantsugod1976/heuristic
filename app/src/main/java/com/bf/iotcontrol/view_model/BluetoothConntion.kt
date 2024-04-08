package com.bf.iotcontrol.view_model

import android.bluetooth.BluetoothSocket
import javax.inject.Singleton

@Singleton
class BluetoothConnection {
    var socket: BluetoothSocket? = null
}