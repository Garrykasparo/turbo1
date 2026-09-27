package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TurboBlue
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboPurple
import com.example.ui.theme.TurboRed
import com.example.ui.theme.TurboYellow

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        // Envio states
        "pendiente" -> Triple(TurboYellow.copy(alpha = 0.18f), TurboYellow, "Pendiente")
        "asignado" -> Triple(TurboBlue.copy(alpha = 0.18f), TurboBlue, "Asignado")
        "aceptado" -> Triple(TurboBlue.copy(alpha = 0.22f), TurboBlue, "Aceptado")
        "en_recoleccion", "recogiendo" -> Triple(TurboOrange.copy(alpha = 0.18f), TurboOrange, "En Recolección")
        "recolectado" -> Triple(TurboPurple.copy(alpha = 0.18f), TurboPurple, "Recolectado")
        "en_camino" -> Triple(TurboPurple.copy(alpha = 0.18f), TurboPurple, "En Camino")
        "entregado" -> Triple(TurboGreen.copy(alpha = 0.18f), TurboGreen, "Entregado")
        "cancelado" -> Triple(TurboRed.copy(alpha = 0.18f), TurboRed, "Cancelado")
        "no_entregado" -> Triple(TurboGray.copy(alpha = 0.18f), TurboGray, "No Entregado")

        // Repartidor states
        "disponible" -> Triple(TurboGreen.copy(alpha = 0.18f), TurboGreen, "Disponible")
        "en_pedido", "ocupado" -> Triple(TurboOrange.copy(alpha = 0.18f), TurboOrange, "Ocupado")
        "pausa" -> Triple(TurboYellow.copy(alpha = 0.18f), TurboYellow, "En Pausa")
        "offline" -> Triple(TurboGray.copy(alpha = 0.18f), TurboGray, "Offline")

        // User approval states
        "aprobado" -> Triple(TurboGreen.copy(alpha = 0.18f), TurboGreen, "Aprobado")
        "rechazado" -> Triple(TurboRed.copy(alpha = 0.18f), TurboRed, "Rechazado")
        "pendiente_aprobacion" -> Triple(TurboYellow.copy(alpha = 0.18f), TurboYellow, "Pendiente de Aprobación")

        else -> Triple(Color(0xFF333333), Color.White, status.replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
