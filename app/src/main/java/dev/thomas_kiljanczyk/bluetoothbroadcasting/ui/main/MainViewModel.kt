package dev.thomas_kiljanczyk.bluetoothbroadcasting.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.bluetoothbroadcasting.transport.TransportRequirements
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val requirements: TransportRequirements
) : ViewModel()
