package com.example.panicshield.ui.screen.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.panicshield.domain.model.Emergency
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.HeatmapLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.style

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    val emergenciesState by viewModel.emergenciesState.collectAsState()
    val mapFilter by viewModel.mapFilter.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadEmergencies()
    }

    // Actualizar datos cuando cambien las emergencias
    LaunchedEffect(emergenciesState) {
        mapboxMap?.let { map ->
            when (emergenciesState) {
                is EmergenciesUiState.Success -> {
                    updateMapData(map, (emergenciesState as EmergenciesUiState.Success).emergencies)
                }
                else -> { /* No hacer nada */ }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                val map = view.getMapboxMap()
                mapboxMap = map

                map.loadStyle(
                    style(Style.MAPBOX_STREETS) {
                        +projection(ProjectionName.GLOBE)
                    }
                ) { style ->
                    // Inicializar las capas vacías
                    initializeMapLayers(style)

                    // Si ya tenemos datos, cargarlos
                    when (emergenciesState) {
                        is EmergenciesUiState.Success -> {
                            updateMapData(map, (emergenciesState as EmergenciesUiState.Success).emergencies)
                        }
                        else -> { /* Mapa vacío inicialmente */ }
                    }
                }
            }
        )


        }

}

private fun initializeMapLayers(style: Style) {
    // Crear fuente vacía inicialmente
    val emptySource = geoJsonSource(EMERGENCIES_SOURCE_ID) {
        featureCollection(FeatureCollection.fromFeatures(emptyList()))
    }
    style.addSource(emptySource)

    // Crear capas
    val heatmapLayer = createHeatmapLayer()
    val circleLayer = createCircleLayer()

    // Añadir capas
    style.addLayer(heatmapLayer)
    style.addLayer(circleLayer)
}

private fun updateMapData(mapboxMap: MapboxMap, emergencies: List<Emergency>) {
    android.util.Log.d("MapScreen", "Actualizando mapa con ${emergencies.size} emergencias")

    mapboxMap.getStyle { style ->
        // Crear nueva fuente con datos actualizados
        val newSource = createEmergenciesSource(emergencies)

        // Actualizar la fuente existente
        style.getSource(EMERGENCIES_SOURCE_ID)?.let { existingSource ->
            if (existingSource is GeoJsonSource) {
                val featureCollection = createFeatureCollection(emergencies)
                existingSource.featureCollection(featureCollection)
                android.util.Log.d("MapScreen", "Fuente actualizada con ${featureCollection.features()?.size ?: 0} features")
            }
        }
    }
}

private fun createFeatureCollection(emergencies: List<Emergency>): FeatureCollection {
    val features = emergencies.mapNotNull { emergency ->
        try {
            val lat = emergency.latitude
            val lng = emergency.longitude

            if (lat != null && lng != null) {
                android.util.Log.d("MapScreen", "Creando feature para emergencia ${emergency.id}: ($lat, $lng)")

                val properties = JsonObject().apply {
                    addProperty("emergency_type", emergency.emergencyType)
                    addProperty("priority", emergency.priority ?: "medium")
                    addProperty("id", emergency.id)
                    addProperty("status", emergency.status)
                    addProperty("created_at", emergency.createdAt)

                    // Valor numérico para el mapa de calor
                    val priorityValue = when (emergency.priority?.lowercase()) {
                        "critical" -> 5
                        "high" -> 4
                        "medium" -> 3
                        "low" -> 2
                        else -> 3 // valor por defecto
                    }
                    addProperty("priority_value", priorityValue)
                }

                Feature.fromGeometry(
                    Point.fromLngLat(lng, lat),
                    properties
                )
            } else {
                android.util.Log.w("MapScreen", "Coordenadas nulas para emergencia ${emergency.id}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Error procesando emergencia ${emergency.id}: ${e.message}", e)
            null
        }
    }

    android.util.Log.d("MapScreen", "Features válidas creadas: ${features.size}")
    return FeatureCollection.fromFeatures(features)
}

private fun createEmergenciesSource(emergencies: List<Emergency>): GeoJsonSource {
    val featureCollection = createFeatureCollection(emergencies)
    return geoJsonSource(EMERGENCIES_SOURCE_ID) {
        featureCollection(featureCollection)
    }
}

private fun createHeatmapLayer(): HeatmapLayer {
    return heatmapLayer(
        HEATMAP_LAYER_ID,
        EMERGENCIES_SOURCE_ID
    ) {
        // Configuración de zoom para el heatmap
        maxZoom(15.0)
        minZoom(0.0)

        // Color del heatmap
        heatmapColor(
            interpolate {
                linear()
                heatmapDensity()
                stop {
                    literal(0)
                    rgba(33.0, 102.0, 172.0, 0.0) // Transparente
                }
                stop {
                    literal(0.2)
                    rgba(103.0, 169.0, 207.0, 0.6) // Azul claro
                }
                stop {
                    literal(0.4)
                    rgba(209.0, 229.0, 240.0, 0.7) // Azul muy claro
                }
                stop {
                    literal(0.6)
                    rgba(253.0, 219.0, 199.0, 0.8) // Naranja claro
                }
                stop {
                    literal(0.8)
                    rgba(239.0, 138.0, 98.0, 0.9) // Naranja
                }
                stop {
                    literal(1.0)
                    rgba(178.0, 24.0, 43.0, 1.0) // Rojo
                }
            }
        )

        // Peso basado en prioridad
        heatmapWeight(
            interpolate {
                linear()
                get { literal("priority_value") }
                stop {
                    literal(1)
                    literal(0.1)
                }
                stop {
                    literal(5)
                    literal(2.0)
                }
            }
        )

        // Intensidad del heatmap
        heatmapIntensity(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(0)
                    literal(1.0)
                }
                stop {
                    literal(15)
                    literal(5.0)
                }
            }
        )

        // Radio del heatmap
        heatmapRadius(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(0)
                    literal(10)
                }
                stop {
                    literal(15)
                    literal(50)
                }
            }
        )

        // Opacidad del heatmap (visible hasta zoom 12)
        heatmapOpacity(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(0)
                    literal(0.8)
                }
                stop {
                    literal(12)
                    literal(0.8)
                }
                stop {
                    literal(15)
                    literal(0.2)
                }
            }
        )
    }
}

private fun createCircleLayer(): CircleLayer {
    return circleLayer(
        CIRCLE_LAYER_ID,
        EMERGENCIES_SOURCE_ID
    ) {
        // Radio del círculo
        circleRadius(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(10)
                    interpolate {
                        linear()
                        get { literal("priority_value") }
                        stop {
                            literal(1)
                            literal(3)
                        }
                        stop {
                            literal(5)
                            literal(8)
                        }
                    }
                }
                stop {
                    literal(18)
                    interpolate {
                        linear()
                        get { literal("priority_value") }
                        stop {
                            literal(1)
                            literal(8)
                        }
                        stop {
                            literal(5)
                            literal(20)
                        }
                    }
                }
            }
        )

        // Color del círculo basado en prioridad
        circleColor(
            interpolate {
                linear()
                get { literal("priority_value") }
                stop {
                    literal(1)
                    rgb(33.0, 102.0, 172.0) // Azul - baja prioridad
                }
                stop {
                    literal(2)
                    rgb(102.0, 169.0, 207.0) // Azul claro - low
                }
                stop {
                    literal(3)
                    rgb(253.0, 219.0, 199.0) // Naranja claro - medium
                }
                stop {
                    literal(4)
                    rgb(239.0, 138.0, 98.0) // Naranja - high
                }
                stop {
                    literal(5)
                    rgb(178.0, 24.0, 43.0) // Rojo - critical
                }
            }
        )

        // Opacidad del círculo (visible desde zoom 10)
        circleOpacity(
            interpolate {
                linear()
                zoom()
                stop {
                    literal(8)
                    literal(0.0)
                }
                stop {
                    literal(10)
                    literal(0.8)
                }
                stop {
                    literal(18)
                    literal(1.0)
                }
            }
        )

        // Borde del círculo
        circleStrokeColor("white")
        circleStrokeWidth(1.5)
        circleStrokeOpacity(0.8)
    }
}

// Constantes
private const val EMERGENCIES_SOURCE_ID = "emergencies"
private const val HEATMAP_LAYER_ID = "emergencies-heat"
private const val CIRCLE_LAYER_ID = "emergencies-circle"