package com.example.panicshield.ui.screen.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.domain.model.Emergency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val emergencyRepository: EmergencyRepository
) : ViewModel() {

    private val _emergenciesState = MutableStateFlow<EmergenciesUiState>(EmergenciesUiState.Loading)
    val emergenciesState: StateFlow<EmergenciesUiState> = _emergenciesState.asStateFlow()

    private val _mapFilter = MutableStateFlow<MapFilter>(MapFilter.ALL)
    val mapFilter: StateFlow<MapFilter> = _mapFilter.asStateFlow()

    fun loadEmergencies() {
        viewModelScope.launch {
            _emergenciesState.value = EmergenciesUiState.Loading
            Log.d("MapViewModel", "Iniciando carga de emergencias...")

            try {
                // Cargar todas las emergencias para el mapa de calor
                emergencyRepository.getAllEmergencies()
                    .onSuccess { emergencies ->
                        Log.d("MapViewModel", "Emergencias cargadas exitosamente: ${emergencies.size} emergencias")
                        emergencies.forEachIndexed { index, emergency ->
                            Log.d("MapViewModel", "Emergencia $index: ID=${emergency.id}, Type=${emergency.emergencyType}, Priority=${emergency.priority}, Lat=${emergency.latitude}, Lng=${emergency.longitude}")
                        }
                        _emergenciesState.value = EmergenciesUiState.Success(emergencies)
                    }
                    .onFailure { exception ->
                        Log.e("MapViewModel", "Error al cargar emergencias: ${exception.message}", exception)
                        _emergenciesState.value = EmergenciesUiState.Error(
                            message = exception.message ?: "Error desconocido",
                            exception = exception
                        )
                    }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Excepción no capturada: ${e.message}", e)
                _emergenciesState.value = EmergenciesUiState.Error(
                    message = "Excepción no capturada: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun loadEmergenciesByStatus(status: String) {
        viewModelScope.launch {
            _emergenciesState.value = EmergenciesUiState.Loading
            Log.d("MapViewModel", "Cargando emergencias con estado: $status")

            try {
                emergencyRepository.getEmergenciesByStatus(status)
                    .onSuccess { emergencies ->
                        Log.d("MapViewModel", "Emergencias filtradas cargadas: ${emergencies.size}")
                        _emergenciesState.value = EmergenciesUiState.Success(emergencies)
                    }
                    .onFailure { exception ->
                        Log.e("MapViewModel", "Error al cargar emergencias filtradas: ${exception.message}", exception)
                        _emergenciesState.value = EmergenciesUiState.Error(
                            message = exception.message ?: "Error desconocido",
                            exception = exception
                        )
                    }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Excepción no capturada: ${e.message}", e)
                _emergenciesState.value = EmergenciesUiState.Error(
                    message = "Excepción no capturada: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun setMapFilter(filter: MapFilter) {
        _mapFilter.value = filter
        when (filter) {
            MapFilter.ALL -> loadEmergencies()
            MapFilter.ACTIVE -> loadEmergenciesByStatus("active")
            MapFilter.RESOLVED -> loadEmergenciesByStatus("resolved")
            MapFilter.HIGH_PRIORITY -> loadHighPriorityEmergencies()
        }
    }

    private fun loadHighPriorityEmergencies() {
        viewModelScope.launch {
            _emergenciesState.value = EmergenciesUiState.Loading

            try {
                emergencyRepository.getAllEmergencies()
                    .onSuccess { allEmergencies ->
                        val highPriorityEmergencies = allEmergencies.filter {
                            it.priority.equals("high", ignoreCase = true) ||
                                    it.priority.equals("critical", ignoreCase = true)
                        }
                        Log.d("MapViewModel", "Emergencias de alta prioridad: ${highPriorityEmergencies.size}")
                        _emergenciesState.value = EmergenciesUiState.Success(highPriorityEmergencies)
                    }
                    .onFailure { exception ->
                        Log.e("MapViewModel", "Error al cargar emergencias de alta prioridad: ${exception.message}", exception)
                        _emergenciesState.value = EmergenciesUiState.Error(
                            message = exception.message ?: "Error desconocido",
                            exception = exception
                        )
                    }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Excepción no capturada: ${e.message}", e)
                _emergenciesState.value = EmergenciesUiState.Error(
                    message = "Excepción no capturada: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun refreshEmergencies() {
        Log.d("MapViewModel", "Refrescando emergencias...")
        when (_mapFilter.value) {
            MapFilter.ALL -> loadEmergencies()
            MapFilter.ACTIVE -> loadEmergenciesByStatus("active")
            MapFilter.RESOLVED -> loadEmergenciesByStatus("resolved")
            MapFilter.HIGH_PRIORITY -> loadHighPriorityEmergencies()
        }
    }
}

sealed class EmergenciesUiState {
    object Loading : EmergenciesUiState()
    data class Success(val emergencies: List<Emergency>) : EmergenciesUiState()
    data class Error(val message: String, val exception: Throwable? = null) : EmergenciesUiState()
}

enum class MapFilter(val displayName: String) {
    ALL("Todas"),
    ACTIVE("Activas"),
    RESOLVED("Resueltas"),
    HIGH_PRIORITY("Alta Prioridad")
}