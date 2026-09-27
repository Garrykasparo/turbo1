package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.ui.theme.TurboBlue
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboPurple
import com.example.ui.theme.TurboRed
import com.example.ui.theme.TurboWhite

@Composable
fun EnvioCard(
    envio: Envio,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onStatusAdvance: ((String) -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onCallClick: (() -> Unit)? = null,
    showAdminActions: Boolean = false
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("envio_card_${envio.numeroSeguimiento}")
    } else {
        modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("envio_card_${envio.numeroSeguimiento}")
    }

    Box(modifier = cardModifier) {
        Column {
            // Header: Tracking Number & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(TurboOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = envio.numeroSeguimiento,
                            color = TurboOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = envio.tipoServicio.uppercase(),
                        color = TurboGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                StatusBadge(status = envio.estado)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Origin / Supplier
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = TurboGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = envio.origenNombre ?: envio.proveedorNombre.ifEmpty { "Proveedor" },
                    color = TurboWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = envio.origenDireccion,
                color = TurboGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Destination / Customer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TurboOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${envio.destinatarioNombre.ifEmpty { envio.clienteNombre }}${if (envio.destinatarioTelefono.isNotEmpty()) " (${envio.destinatarioTelefono})" else ""}",
                    color = TurboWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = envio.destinoDireccion,
                color = TurboGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
            if (!envio.destinoReferencias.isNullOrBlank()) {
                Text(
                    text = "Ref: ${envio.destinoReferencias}",
                    color = Color(0xFFAAAAAA),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: Repartidor + Amounts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF202020), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = TurboBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = envio.repartidorNombre?.let { "Rep: $it" } ?: "Sin asignar",
                        color = if (envio.repartidorNombre != null) TurboWhite else TurboGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (envio.requiereCobro || envio.montoCobrar > 0) {
                            Text(
                                text = "Cobrar: $${String.format("%.2f", envio.montoCobrar)}",
                                color = TurboGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Sin cobro",
                                color = TurboGray,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Tarifa: $${String.format("%.2f", envio.costoEnvio)} (${envio.metodoPago})",
                            color = TurboGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Cancellation Notice if Cancelled
            if (envio.estado == "cancelado") {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TurboRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Cancelado: ${envio.motivoCancelacion ?: "Por usuario"} ${envio.canceladoPor?.let { "($it)" } ?: ""}",
                        color = TurboRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onClick != null) {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detalle", color = TurboWhite, fontSize = 12.sp)
                    }
                }

                if (onEditClick != null && envio.estado == "pendiente") {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = TurboBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", color = TurboBlue, fontSize = 12.sp)
                    }
                }

                if (onCancelClick != null && envio.estado == "pendiente") {
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TurboRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar", color = TurboRed, fontSize = 12.sp)
                    }
                }

                if (onStatusAdvance != null && envio.estado != "entregado" && envio.estado != "cancelado") {
                    val nextStatus = when (envio.estado) {
                        "pendiente" -> "asignado"
                        "asignado" -> "en_recoleccion"
                        "aceptado" -> "en_recoleccion"
                        "en_recoleccion", "recogiendo" -> "en_camino"
                        "recolectado" -> "en_camino"
                        "en_camino" -> "entregado"
                        else -> null
                    }

                    if (nextStatus != null) {
                        val buttonText = when (nextStatus) {
                            "asignado" -> "Asignar"
                            "en_recoleccion" -> "Recolectar"
                            "en_camino" -> "En Camino"
                            "entregado" -> "Entregar"
                            else -> "Avanzar"
                        }

                        Button(
                            onClick = { onStatusAdvance(nextStatus) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .testTag("advance_status_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (nextStatus == "entregado") TurboGreen else TurboOrange
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (nextStatus == "entregado") Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = buttonText,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
