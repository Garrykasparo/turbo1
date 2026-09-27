package com.example.ui.screens.coordinador

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.ui.components.AppHeader
import com.example.ui.components.EmptyState
import com.example.ui.components.EnvioCard
import com.example.ui.components.RepartidorCard
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.TurboBlue
import com.example.ui.theme.TurboDarkBg
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow

@Composable
fun CoordinadorDashboardScreen(
    currentUser: User?,
    allEnvios: List<Envio>,
    allRepartidores: List<Repartidor>,
    countPendientes: Int,
    countActivos: Int,
    countEntregados: Int,
    countDisponibles: Int,
    countOcupados: Int,
    onAssignRepartidor: (String, String, String) -> Unit,
    onAdvanceEnvio: (String, String) -> Unit,
    onLogout: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onOpenDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var envioToAssign by remember { mutableStateOf<Envio?>(null) }
    var selectedSection by remember { mutableStateOf("ENVIOS") } // ENVIOS, REPARTIDORES, PROXIMAMENTE

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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Coordinación Operativa",
                    color = TurboWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Supervisión de pedidos, asignaciones y flota en Mérida",
                    color = TurboGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // 5 KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Pendientes",
                        value = countPendientes.toString(),
                        icon = Icons.Default.HourglassBottom,
                        accentColor = TurboYellow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "En Ruta",
                        value = countActivos.toString(),
                        icon = Icons.Default.DeliveryDining,
                        accentColor = TurboBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Entregados",
                        value = countEntregados.toString(),
                        icon = Icons.Default.DoneAll,
                        accentColor = TurboGreen,
                        modifier = Modifier.weight(1f),
                        subtext = "Hoy"
                    )
                    StatCard(
                        title = "Disponibles",
                        value = countDisponibles.toString(),
                        icon = Icons.Default.TwoWheeler,
                        accentColor = TurboGreen,
                        modifier = Modifier.weight(1f),
                        subtext = "Repartidores"
                    )
                    StatCard(
                        title = "Activos",
                        value = countOcupados.toString(),
                        icon = Icons.Default.LocalShipping,
                        accentColor = TurboOrange,
                        modifier = Modifier.weight(1f),
                        subtext = "En entrega"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ENVIOS" to "Envíos",
                        "REPARTIDORES" to "Repartidores",
                        "MODULOS" to "Módulos Avanzados"
                    ).forEach { (secKey, label) ->
                        val isSel = selectedSection == secKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) TurboOrange else TurboDarkCard, RoundedCornerShape(10.dp))
                                .border(1.dp, if (isSel) TurboOrange else Color(0xFF383838), RoundedCornerShape(10.dp))
                                .clickable { selectedSection = secKey }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else TurboGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when (selectedSection) {
                "ENVIOS" -> {
                    if (allEnvios.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.DeliveryDining,
                                title = "Sin envíos operativos",
                                message = "No hay envíos registrados para supervisar."
                            )
                        }
                    } else {
                        items(allEnvios) { envio ->
                            EnvioCard(
                                envio = envio,
                                onClick = { onOpenDetail(envio.id) },
                                onStatusAdvance = { nextStatus ->
                                    if (envio.estado == "pendiente") {
                                        envioToAssign = envio
                                    } else {
                                        onAdvanceEnvio(envio.id, nextStatus)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                "REPARTIDORES" -> {
                    if (allRepartidores.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.TwoWheeler,
                                title = "Sin repartidores",
                                message = "Los repartidores disponibles aparecerán en esta sección."
                            )
                        }
                    } else {
                        items(allRepartidores) { rep ->
                            RepartidorCard(repartidor = rep)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                "MODULOS" -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            UpcomingModuleCard(
                                title = "Seguimiento Satelital en Tiempo Real",
                                description = "Visualización de telemetría GPS, velocidad y geocercas activas de la flota en Mérida."
                            )
                            UpcomingModuleCard(
                                title = "Asignación Automática Inteligente",
                                description = "Algoritmo para despachar el repartidor óptimo basado en distancia y tipo de vehículo."
                            )
                            UpcomingModuleCard(
                                title = "Reportes y Liquidaciones Operativas",
                                description = "Cálculo de cobros en efectivo, comisiones por entrega y transferencias."
                            )
                            UpcomingModuleCard(
                                title = "Chat y Mensajería WhatsApp",
                                description = "Canal directo entre proveedor, coordinador y repartidor."
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Dialog to assign a driver to an order
    if (envioToAssign != null) {
        val current = envioToAssign!!
        AlertDialog(
            onDismissRequest = { envioToAssign = null },
            title = {
                Text(
                    text = "Asignar Repartidor a ${current.numeroSeguimiento}",
                    color = TurboWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selecciona un repartidor disponible para realizar este pedido:",
                        color = TurboGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    allRepartidores.forEach { rep ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(TurboDarkCard, RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                                .clickable {
                                    onAssignRepartidor(current.id, rep.id, rep.nombreCompleto)
                                    envioToAssign = null
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = rep.nombreCompleto, color = TurboWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${rep.vehiculoTipo.uppercase()} • ${rep.visualCode}", color = TurboGray, fontSize = 11.sp)
                                }
                                StatusBadge(status = rep.estado)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { envioToAssign = null }) {
                    Text("Cancelar", color = TurboGray)
                }
            },
            containerColor = TurboDarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun UpcomingModuleCard(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TurboWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(TurboOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "Próximamente", color = TurboOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, color = TurboGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
