package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Repartidor
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow

@Composable
fun RepartidorCard(
    repartidor: Repartidor,
    modifier: Modifier = Modifier
) {
    val vehicleIcon = when (repartidor.vehiculoTipo.lowercase()) {
        "bicicleta" -> Icons.Default.DirectionsBike
        "auto" -> Icons.Default.DirectionsCar
        "camioneta" -> Icons.Default.LocalShipping
        else -> Icons.Default.ElectricMoped
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(TurboOrange.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vehicleIcon,
                            contentDescription = null,
                            tint = TurboOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = repartidor.nombreCompleto,
                            color = TurboWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = repartidor.visualCode,
                                color = TurboOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${repartidor.telefono}",
                                color = TurboGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                StatusBadge(status = repartidor.estado)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle specs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${repartidor.vehiculoTipo.uppercase()} ${repartidor.vehiculoMarca.orEmpty()} ${repartidor.vehiculoModelo.orEmpty()}",
                    color = TurboGray,
                    fontSize = 11.sp
                )
                if (!repartidor.vehiculoPlaca.isNullOrEmpty()) {
                    Text(
                        text = "Placa: ${repartidor.vehiculoPlaca}",
                        color = TurboWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = TurboYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${repartidor.calificacionPromedio}",
                        color = TurboWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Entregas exitosas: ${repartidor.entregasExitosas}/${repartidor.totalEntregas}",
                    color = TurboGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
