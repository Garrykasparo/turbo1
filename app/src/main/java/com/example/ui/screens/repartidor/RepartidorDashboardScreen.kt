package com.example.ui.screens.repartidor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.ui.components.AppHeader
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.TurboBlue
import com.example.ui.theme.TurboDarkBg
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboRed
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow

@Composable
fun RepartidorDashboardScreen(
    currentUser: User?,
    repartidor: Repartidor?,
    activeEnvio: Envio?,
    onStatusChange: (String) -> Unit,
    onAdvanceEnvio: (String, String) -> Unit,
    onLogout: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onOpenDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TurboDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Header
        AppHeader(
            currentUser = currentUser,
            onLogout = onLogout,
            onSwitchUser = onSwitchUser
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Profile & ID Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TurboDarkCard, RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF383838), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(TurboOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = "Avatar Repartidor",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = repartidor?.nombreCompleto ?: currentUser?.fullName ?: "Repartidor",
                            color = TurboWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = repartidor?.visualCode ?: "TM-9999",
                                color = TurboOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ⭐ ${repartidor?.calificacionPromedio ?: 5.0}",
                                color = TurboYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${repartidor?.vehiculoTipo?.uppercase() ?: "MOTO"} ${repartidor?.vehiculoPlaca.orEmpty()}",
                            color = TurboGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Selector: Disponible / Ocupado / Pausa / Offline
            Text(
                text = "Mi Estado Operativo",
                color = TurboGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val currentEstado = repartidor?.estado ?: "offline"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "disponible" to "Disponible",
                    "en_pedido" to "Ocupado",
                    "pausa" to "Pausa",
                    "offline" to "Offline"
                ).forEach { (estadoKey, label) ->
                    val isSelected = currentEstado == estadoKey
                    val btnBg = when {
                        isSelected && estadoKey == "disponible" -> TurboGreen
                        isSelected && estadoKey == "en_pedido" -> TurboOrange
                        isSelected && estadoKey == "pausa" -> TurboYellow
                        isSelected -> Color(0xFF555555)
                        else -> TurboDarkCard
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(btnBg, RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSelected) btnBg else Color(0xFF383838), RoundedCornerShape(10.dp))
                            .clickable { onStatusChange(estadoKey) }
                            .padding(vertical = 10.dp)
                            .testTag("status_selector_$estadoKey"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TurboGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACTIVE ORDER SECTION
            if (activeEnvio != null) {
                Text(
                    text = "PEDIDO EN CURSO",
                    color = TurboOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TurboDarkCard, RoundedCornerShape(18.dp))
                        .border(2.dp, TurboOrange, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeEnvio.numeroSeguimiento,
                                color = TurboOrange,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            StatusBadge(status = activeEnvio.estado)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pickup point
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = TurboGray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Recoger en: ${activeEnvio.proveedorNombre}",
                                    color = TurboWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeEnvio.origenDireccion,
                                    color = TurboGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Delivery point
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Entregar a: ${activeEnvio.destinatarioNombre.ifEmpty { activeEnvio.clienteNombre }}",
                                    color = TurboWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeEnvio.destinoDireccion,
                                    color = TurboGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Payment & Collection Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF222222), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Monto a cobrar:", color = TurboGray, fontSize = 11.sp)
                                Text(
                                    text = "$${String.format("%.2f", activeEnvio.montoCobrar)}",
                                    color = TurboGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Forma de pago:", color = TurboGray, fontSize = 11.sp)
                                Text(
                                    text = activeEnvio.formaPago.uppercase(),
                                    color = TurboWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!activeEnvio.notas.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Notas: ${activeEnvio.notas}",
                                color = TurboYellow,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Call, Detalle & Advance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val phoneToCall = activeEnvio.destinatarioTelefono.ifEmpty { activeEnvio.origenTelefono ?: "" }
                                    if (phoneToCall.isNotEmpty()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("driver_call_client_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Llamar", color = TurboWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { onOpenDetail(activeEnvio.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("driver_view_detail_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = TurboBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Detalle", color = TurboWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            val (nextState, buttonText, buttonColor) = when (activeEnvio.estado) {
                                "asignado" -> Triple("recogiendo", "Ir x Pedido", TurboOrange)
                                "recogiendo" -> Triple("en_camino", "En Camino", TurboBlue)
                                "en_camino" -> Triple("entregado", "Entregar", TurboGreen)
                                else -> Triple("entregado", "Entregado", TurboGreen)
                            }

                            Button(
                                onClick = { onAdvanceEnvio(activeEnvio.id, nextState) },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("driver_advance_order_button")
                            ) {
                                Icon(
                                    imageVector = if (nextState == "entregado") Icons.Default.CheckCircle else Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                // NO ACTIVE ORDER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TurboDarkCard, RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFF383838), RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(TurboGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = TurboGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Sin pedidos activos",
                            color = TurboWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mantente disponible para recibir nuevas asignaciones automáticas y entregas en tu zona.",
                            color = TurboGray,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Operational Summary
            Text(
                text = "Resumen de Rendimiento",
                color = TurboGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(TurboDarkCard, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = "Entregas Exitosas", color = TurboGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${repartidor?.entregasExitosas ?: 0}",
                            color = TurboGreen,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(TurboDarkCard, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = "Total Asignadas", color = TurboGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${repartidor?.totalEntregas ?: 0}",
                            color = TurboWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
