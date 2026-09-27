package com.example.ui.screens.proveedor

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.User
import com.example.ui.components.AppHeader
import com.example.ui.components.CancelarEnvioDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.EnvioCard
import com.example.ui.components.StatCard
import com.example.ui.screens.customTextFieldColors
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
fun ProveedorDashboardScreen(
    currentUser: User?,
    envios: List<Envio>,
    onNavigateToNuevoEnvio: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onEditEnvio: (String) -> Unit,
    onCancelEnvio: (String, String) -> Unit,
    onLogout: () -> Unit,
    onSwitchUser: (String) -> Unit,
    isFirebaseConnected: Boolean = true,
    firebaseStatusMessage: String = "",
    onSyncClick: () -> Unit = {},
    onCreateEnvio: (Envio) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") } // TODOS, PENDIENTES, ACTIVOS, ENTREGADOS, CANCELADOS
    var envioToCancel by remember { mutableStateOf<Envio?>(null) }

    val businessName = currentUser?.nombreNegocio ?: currentUser?.fullName ?: "Mi Negocio"

    val activeStates = listOf("pendiente", "asignado", "aceptado", "en_recoleccion", "recolectado", "recogiendo", "en_camino")

    val filteredEnvios = envios.filter { envio ->
        val matchesFilter = when (selectedFilter) {
            "PENDIENTES" -> envio.estado == "pendiente"
            "ACTIVOS" -> envio.estado in activeStates && envio.estado != "pendiente"
            "ENTREGADOS" -> envio.estado == "entregado"
            "CANCELADOS" -> envio.estado == "cancelado"
            else -> true
        }

        val cleanQuery = searchQuery.trim().lowercase()
        val matchesSearch = if (cleanQuery.isEmpty()) true else {
            envio.numeroSeguimiento.lowercase().contains(cleanQuery) ||
                envio.destinatarioNombre.lowercase().contains(cleanQuery) ||
                envio.clienteNombre.lowercase().contains(cleanQuery) ||
                envio.destinoDireccion.lowercase().contains(cleanQuery)
        }

        matchesFilter && matchesSearch
    }

    val totalHoy = envios.size
    val pendientes = envios.count { it.estado == "pendiente" }
    val enRuta = envios.count { it.estado in listOf("en_camino", "en_recoleccion", "recogiendo") }
    val entregados = envios.count { it.estado == "entregado" }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNuevoEnvio,
                containerColor = TurboOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("new_order_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Envío", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = TurboDarkBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // App Header with Cloud Status
            AppHeader(
                currentUser = currentUser,
                onLogout = onLogout,
                onSwitchUser = onSwitchUser,
                isFirebaseConnected = isFirebaseConnected,
                firebaseStatusMessage = firebaseStatusMessage,
                onSyncClick = onSyncClick
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hola, $businessName",
                        color = TurboWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Panel de control de pedidos y envíos locales en Mérida",
                        color = TurboGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // 4 KPIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Envíos",
                            value = totalHoy.toString(),
                            icon = Icons.Default.Receipt,
                            accentColor = TurboOrange,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pendientes",
                            value = pendientes.toString(),
                            icon = Icons.Default.HourglassEmpty,
                            accentColor = TurboYellow,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "En Curso",
                            value = enRuta.toString(),
                            icon = Icons.Default.Navigation,
                            accentColor = TurboBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Entregados",
                            value = entregados.toString(),
                            icon = Icons.Default.CheckCircle,
                            accentColor = TurboGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action: New Order Button
                    Button(
                        onClick = onNavigateToNuevoEnvio,
                        colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("quick_new_order_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Crear Nuevo Pedido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por guía TM-XXXX, cliente o dirección...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TurboOrange) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TurboGray)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("search_pedidos_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filter tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterTabs = listOf(
                            "TODOS" to "Todos",
                            "PENDIENTES" to "Pendientes",
                            "ACTIVOS" to "En Curso",
                            "ENTREGADOS" to "Entregados",
                            "CANCELADOS" to "Cancelados"
                        )

                        filterTabs.forEach { (key, label) ->
                            val isSel = selectedFilter == key
                            val activeBg = when (key) {
                                "CANCELADOS" -> TurboRed
                                "ENTREGADOS" -> TurboGreen
                                "PENDIENTES" -> TurboYellow
                                else -> TurboOrange
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) activeBg else TurboDarkCard, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isSel) activeBg else Color(0xFF383838), RoundedCornerShape(8.dp))
                                    .clickable { selectedFilter = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else TurboGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Shipments List
                if (filteredEnvios.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.DeliveryDining,
                            title = if (searchQuery.isNotEmpty()) "Sin resultados" else "Sin pedidos en esta sección",
                            message = if (searchQuery.isNotEmpty()) {
                                "No se encontraron pedidos con '$searchQuery'. Prueba con otro término o borra la búsqueda."
                            } else {
                                "Aún no tienes pedidos registrados en '${selectedFilter.lowercase()}'. Pulsa 'Crear Nuevo Pedido' para generar tu primer envío."
                            },
                            actionText = "Crear Nuevo Pedido",
                            onActionClick = onNavigateToNuevoEnvio
                        )
                    }
                } else {
                    items(filteredEnvios, key = { it.id }) { envio ->
                        EnvioCard(
                            envio = envio,
                            onClick = { onOpenDetail(envio.id) },
                            onEditClick = if (envio.estado == "pendiente") {
                                { onEditEnvio(envio.id) }
                            } else null,
                            onCancelClick = if (envio.estado == "pendiente") {
                                { envioToCancel = envio }
                            } else null
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Cancel Dialog
    if (envioToCancel != null) {
        val envio = envioToCancel!!
        CancelarEnvioDialog(
            numeroSeguimiento = envio.numeroSeguimiento,
            onDismiss = { envioToCancel = null },
            onConfirmCancel = { motivo ->
                val id = envio.id
                envioToCancel = null
                onCancelEnvio(id, motivo)
            }
        )
    }
}
