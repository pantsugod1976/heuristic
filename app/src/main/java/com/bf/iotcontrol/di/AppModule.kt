package com.bf.iotcontrol.di

import android.content.Context
import com.bf.iotcontrol.bluetooth_controller.AndroidBluetoothController
import com.bf.iotcontrol.bluetooth_controller.BluetoothController
import com.bf.iotcontrol.view_model.BluetoothConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("Controller")
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController {
        return AndroidBluetoothController(context)
    }

    @Provides
    @Singleton
    fun provideBluetoothObject() = BluetoothConnection()
}