package com.example.panicshield.ui.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PanicButtonContent(
    onPanicButtonClick: () -> Unit = { /* TODO: Manejar botón de pánico */ }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Botón de pánico principal
        PanicButton(onClick = onPanicButtonClick)

        Spacer(modifier = Modifier.height(32.dp))

        // Texto de instrucciones
        InstructionText()
    }
}

@Composable
private fun PanicButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(200.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE53E3E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PanicButtonContent()
        }
    }
}

@Composable
private fun PanicButtonContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_call),
            contentDescription = "Botón de pánico",
            modifier = Modifier.size(60.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "EMERGENCIA",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Presione en caso de",
            color = Color.White,
            fontSize = 12.sp
        )

        Text(
            text = "peligro",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InstructionText() {
    Text(
        text = "Mantén presionado el botón para activar la alerta",
        fontSize = 14.sp,
        color = Color.Gray
    )
}