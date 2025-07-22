package com.example.panicshield.ui.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panicshield.domain.usecase.EmergencyHistory
import com.example.panicshield.domain.model.EmergencyStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadEmergencyHistory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.selectedEmergency != null) {
            EmergencyDetailsView(
                emergency = uiState.selectedEmergency!!,
                onBackClick = { viewModel.clearSelectedEmergency() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            HistoryListView(
                uiState = uiState,
                onEmergencyClick = { emergency -> viewModel.selectEmergency(emergency) },
                onFilterChange = { filter -> viewModel.setTimeFilter(filter) },
                onSearchChange = { query -> viewModel.setSearchQuery(query) },
                onRefresh = { viewModel.loadEmergencyHistory() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
@Composable
fun getDetailCardColor(status: EmergencyStatus, priority: String?): Color {
    return when {
        status == EmergencyStatus.CANCELLED -> Color(0xFF757575)    // ✅ CANCELLED = GRIS SIEMPRE
        !priority.isNullOrBlank() -> getPriorityColor(priority)      // ✅ LUEGO PRIORIDAD
        else -> when (status) {                                      // ✅ FINALMENTE ESTADO
            EmergencyStatus.ACTIVE -> Color(0xFFE53935)
            EmergencyStatus.COMPLETED -> Color(0xFF4CAF50)
            EmergencyStatus.PENDING -> Color(0xFFFF9800)
            EmergencyStatus.CANCELLING -> Color(0xFF9C27B0)
            else -> Color(0xFF757575)
        }
    }
}

@Composable
fun HistoryListView(
    uiState: HistoryUIState,
    onEmergencyClick: (EmergencyHistory) -> Unit,
    onFilterChange: (HistoryTimeFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        // ✅ BARRA DE BÚSQUEDA
        SearchBar(
            searchQuery = uiState.searchQuery,
            onSearchChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // ✅ FILTROS DE TIEMPO
        TimeFilterRow(
            selectedFilter = uiState.currentFilter,
            onFilterChange = onFilterChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ CONTENIDO PRINCIPAL
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.primary // Usa el color primario del tema
                    )
                }
            }

            uiState.emergencyHistory.isEmpty() && !uiState.isLoading -> {
                EmptyHistoryState(
                    onRefreshClick = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                HistoryListContent(
                    emergencyHistory = uiState.filteredEmergencies,
                    onEmergencyClick = onEmergencyClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ✅ MOSTRAR ERROR SI EXISTE
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.errorContainer // ✅ USAR COLOR DE ERROR NORMAL
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = colors.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = colors.error,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Buscar emergencias...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(25.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            containerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true
    )
}
// 1. FUNCIÓN NUEVA: Obtener color según prioridad
@Composable
fun getPriorityColor(priority: String?): Color {
    return when (priority?.uppercase()) {
        "CRITICAL" -> Color(0xFFE53935) // Rojo para crítica
        "HIGH" -> Color(0xFFFFC107)     // Amarillo para moderada/alta
        else -> Color(0xFF9E9E9E)       // Gris para otros casos
    }
}

@Composable
fun TimeFilterRow(
    selectedFilter: HistoryTimeFilter,
    onFilterChange: (HistoryTimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryTimeFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        text = filter.displayName,
                        fontSize = 14.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun HistoryListContent(
    emergencyHistory: List<EmergencyHistory>,
    onEmergencyClick: (EmergencyHistory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ✅ AGRUPAR POR PERÍODO
        val groupedHistory = emergencyHistory.groupBy { emergency ->
            getTimePeriod(emergency.createdAt)
        }

        groupedHistory.forEach { (period, emergencies) ->
            item {
                // ✅ HEADER DEL PERÍODO
                Text(
                    text = period,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(
                items = emergencies,
                key = { it.id ?: 0 }
            ) { emergency ->
                EmergencyHistoryItem(
                    emergency = emergency,
                    onClick = { onEmergencyClick(emergency) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ✅ Espaciado inferior
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun EmergencyHistoryItem(
    emergency: EmergencyHistory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ ICONO DE ESTADO
            EmergencyStatusIcon(
                status = emergency.status,
                priority = emergency.priority,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // ✅ INFORMACIÓN PRINCIPAL
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatDate(emergency.createdAt),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ALARMA ${getPriorityDisplayText(emergency.priority)}",
                    fontSize = 12.sp,
                    color = getPriorityColor(emergency.priority),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatTime(emergency.createdAt),
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )

                emergency.address?.let { address ->
                    Text(
                        text = address,
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ✅ FLECHA INDICADOR
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun getPriorityDisplayText(priority: String?): String {
    return when (priority?.uppercase()) {
        "CRITICAL" -> "CRÍTICA"
        "HIGH" -> "MODERADA"
        "MEDIUM" -> "MEDIA"
        "LOW" -> "BAJA"
        else -> "NORMAL"
    }
}
@Composable
fun EmergencyStatusIcon(
    modifier: Modifier = Modifier,
    status: EmergencyStatus,
    priority: String? = null // ✅ NUEVO PARÁMETRO
) {
    val colorScheme = MaterialTheme.colorScheme

    // ✅ JERARQUÍA: CANCELLED SIEMPRE GRIS, LUEGO PRIORIDAD
    val color = when {
        status == EmergencyStatus.CANCELLED -> colorScheme.outline // ✅ CANCELLED = GRIS SIEMPRE
        !priority.isNullOrBlank() -> getPriorityColor(priority)     // ✅ LUEGO PRIORIDAD
        else -> when (status) {                                     // ✅ FINALMENTE ESTADO
            EmergencyStatus.ACTIVE -> colorScheme.error
            EmergencyStatus.COMPLETED -> colorScheme.primary
            EmergencyStatus.PENDING -> colorScheme.tertiary
            EmergencyStatus.CANCELLING -> colorScheme.secondary
            else -> colorScheme.inversePrimary
        }
    }

    val icon = when (status) {
        EmergencyStatus.ACTIVE -> Icons.Default.Warning
        EmergencyStatus.COMPLETED -> Icons.Default.CheckCircle
        EmergencyStatus.CANCELLED -> Icons.Default.Cancel
        EmergencyStatus.PENDING -> Icons.Default.Schedule
        EmergencyStatus.CANCELLING -> Icons.Default.HourglassEmpty
        else -> Icons.Default.Info
    }

    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
fun EmptyHistoryState(
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Sin historial",
            tint = Color.Gray,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay emergencias registradas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu historial de emergencias aparecerá aquí",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRefreshClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Actualizar",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EmergencyDetailsView(
    emergency: EmergencyHistory,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White)
    ) {
        // ✅ HEADER CON BOTÓN ATRÁS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = Color.Black
                )
            }

            Text(
                text = "Detalles de alarma",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        // ✅ CARD PRINCIPAL DE DETALLES
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = getDetailCardColor(emergency.status, emergency.priority) // ✅ USAR JERARQUÍA CORRECTA
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ FECHA Y DÍA
                Text(
                    text = getDayName(emergency.createdAt),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = getMonthYear(emergency.createdAt),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ ICONO DE EMERGENCIA
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergencia",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ INFORMACIÓN DE LA EMERGENCIA
                EmergencyDetailRow(
                    label = "Tipo de alarma",
                    value = emergency.priority?.uppercase() ?: "NORMAL" // ✅ MOSTRAR PRIORIDAD
                )

                EmergencyDetailRow(
                    label = "Hora",
                    value = formatDetailTime(emergency.createdAt)
                )


                EmergencyDetailRow(
                    label = "Estado",
                    value = getStatusDisplayName(emergency.status)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ INFORMACIÓN ADICIONAL
        AdditionalDetailsSection(
            emergency = emergency,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun EmergencyDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, false)
        )
    }
}

@Composable
fun AdditionalDetailsSection(
    emergency: EmergencyHistory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Información Adicional",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailInfoRow(
                icon = Icons.Default.Schedule,
                label = "Hora de creación",
                value = formatDetailDateTime(emergency.createdAt)
            )

            emergency.updatedAt?.let { updatedAt ->
                DetailInfoRow(
                    icon = Icons.Default.Update,
                    label = "Última actualización",
                    value = formatDetailDateTime(updatedAt)
                )
            }

            DetailInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Coordenadas",
                value = "${emergency.latitude}, ${emergency.longitude}"
            )

            emergency.message?.let { message ->
                DetailInfoRow(
                    icon = Icons.Default.Message,
                    label = "Mensaje",
                    value = message
                )
            }

            emergency.priority?.let { priority ->
                DetailInfoRow(
                    icon = Icons.Default.PriorityHigh,
                    label = "Prioridad",
                    value = priority
                )
            }

            emergency.deviceInfo?.let { deviceInfo ->
                DetailInfoRow(
                    icon = Icons.Default.Smartphone,
                    label = "Información del dispositivo",
                    value = deviceInfo
                )
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

// ===== EXTENSIÓN PARA HistoryTimeFilter =====

val HistoryTimeFilter.displayName: String
    get() = when (this) {
        HistoryTimeFilter.THIS_WEEK -> "Esta Semana"
        HistoryTimeFilter.THIS_MONTH -> "Este Mes"
        HistoryTimeFilter.LAST_MONTH -> "Mes Anterior"
        HistoryTimeFilter.ALL -> "Todos"
    }

// ===== FUNCIONES HELPER =====

// ✅ FUNCIÓN: Formatear fecha completa con día de la semana
private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEEE dd/MM/yyyy", Locale("es", "ES"))
    return formatter.format(Date(timestamp)).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

// ✅ FUNCIÓN: Formatear solo la hora
private fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// ✅ FUNCIÓN: Formatear hora con segundos para detalles
private fun formatDetailTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// ✅ FUNCIÓN: Formatear fecha y hora completa para detalles
private fun formatDetailDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// ✅ FUNCIÓN: Obtener solo el día para el card de detalles
private fun getDayName(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// ✅ FUNCIÓN: Obtener mes y año para el card de detalles
private fun getMonthYear(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    return formatter.format(Date(timestamp)).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

private fun getTimePeriod(timestamp: Long): String {
    val emergencyDate = Date(timestamp)
    val today = Date()

    val emergencyCalendar = Calendar.getInstance().apply { time = emergencyDate }
    val todayCalendar = Calendar.getInstance().apply { time = today }

    // ✅ Verificar si es hoy
    if (isSameDay(emergencyCalendar, todayCalendar)) {
        return "Hoy"
    }

    // ✅ Verificar si es ayer
    val yesterdayCalendar = Calendar.getInstance().apply {
        time = today
        add(Calendar.DAY_OF_MONTH, -1)
    }
    if (isSameDay(emergencyCalendar, yesterdayCalendar)) {
        return "Ayer"
    }

    // ✅ Verificar si es esta semana
    if (isSameWeek(emergencyCalendar, todayCalendar)) {
        return "Esta Semana"
    }

    // ✅ Verificar si es este mes
    if (isSameMonth(emergencyCalendar, todayCalendar)) {
        return "Este Mes"
    }

    // ✅ Verificar si es el mes pasado
    val lastMonthCalendar = Calendar.getInstance().apply {
        time = today
        add(Calendar.MONTH, -1)
    }
    if (isSameMonth(emergencyCalendar, lastMonthCalendar)) {
        return "Mes Anterior"
    }

    // ✅ Para fechas más antiguas, mostrar mes y año
    val formatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    return formatter.format(emergencyDate).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

// ===== FUNCIONES HELPER AUXILIARES =====

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}

private fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

private fun getStatusDisplayName(status: EmergencyStatus): String {
    return when (status) {
        EmergencyStatus.ACTIVE -> "ACTIVA"
        EmergencyStatus.COMPLETED -> "COMPLETADA"
        EmergencyStatus.CANCELLED -> "CANCELADA"
        EmergencyStatus.PENDING -> "PENDIENTE"
        EmergencyStatus.CANCELLING -> "CANCELANDO"
        EmergencyStatus.INACTIVE -> "INACTIVA"
    }
}