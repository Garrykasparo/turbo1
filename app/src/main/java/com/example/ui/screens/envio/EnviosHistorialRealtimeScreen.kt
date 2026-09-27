package com.example.ui.screens.envio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.User
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.screens.customTextFieldColors
import com.example.ui.theme.TurboBlue
import com.example.ui.theme.TurboDarkBg
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboPurple
import com.example.ui.theme.TurboRed
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviosHistorialRealtimeScreen(
    currentUser: User?,
    allEnvios: List<Envio>,
    isFirebaseConnected: Boolean,
    firebaseStatusMessage: String,
    lastSyncTimestamp: Long,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onRefreshRealtime: () -> Unit,
    onAdvanceEstado: (String, String) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Envíos Activos, 1 = Historial de Pedidos
    var searchQuery by remember { mutableStateOf("") }
    var activeSubFilter by remember { mutableStateOf("TODOS") } // TODOS, EN_CAMINO, RECOLECCION, ASIGNADO, PENDIENTE
    var historySubFilter by remember { mutableStateOf("TODOS") } // TODOS, ENTREGADOS, CANCELADOS
    var selectedProveedorFilter by remember { mutableStateOf<String?>(null) } // null = Todos

    val userRole = currentUser?.role?.lowercase() ?: "proveedor"
    val isProvider = userRole == "proveedor"
    val isAdminOrCoord = userRole == "admin" || userRole == "coordinador" || userRole == "asistente"

    // Base scoping: if provider, default to their orders
    val scopedEnvios = remember(allEnvios, currentUser) {
        if (isProvider && currentUser != null) {
            allEnvios.filter { it.proveedorId == currentUser.id }
        } else {
            allEnvios
        }
    }

    // Active stages
    val activeStates = listOf("pendiente", "asignado", "aceptado", "en_recoleccion", "recolectado", "recogiendo", "en_camino")
    val historyStates = listOf("entregado", "cancelado", "no_entregado")

    // Filtered lists
    val activeEnvios = remember(scopedEnvios, searchQuery, activeSubFilter, selectedProveedorFilter) {
        scopedEnvios.filter { envio ->
            val isActive = envio.estado in activeStates
            val matchesSub = when (activeSubFilter) {
                "EN_CAMINO" -> envio.estado == "en_camino"
                "RECOLECCION" -> envio.estado in listOf("en_recoleccion", "recolectado", "recogiendo")
                "ASIGNADO" -> envio.estado in listOf("asignado", "aceptado")
                "PENDIENTE" -> envio.estado == "pendiente"
                else -> true
            }
            val matchesProv = selectedProveedorFilter == null || envio.proveedorNombre == selectedProveedorFilter
            val cleanQuery = searchQuery.trim().lowercase()
            val matchesQuery = cleanQuery.isEmpty() ||
                envio.numeroSeguimiento.lowercase().contains(cleanQuery) ||
                envio.destinatarioNombre.lowercase().contains(cleanQuery) ||
                envio.destinoDireccion.lowercase().contains(cleanQuery) ||
                (envio.repartidorNombre?.lowercase()?.contains(cleanQuery) == true)

            isActive && matchesSub && matchesProv && matchesQuery
        }
    }

    val historyEnvios = remember(scopedEnvios, searchQuery, historySubFilter, selectedProveedorFilter) {
        scopedEnvios.filter { envio ->
            val isHistory = envio.estado in historyStates
            val matchesSub = when (historySubFilter) {
                "ENTREGADOS" -> envio.estado == "entregado"
                "CANCELADOS" -> envio.estado in listOf("cancelado", "no_entregado")
                else -> true
            }
            val matchesProv = selectedProveedorFilter == null || envio.proveedorNombre == selectedProveedorFilter
            val cleanQuery = searchQuery.trim().lowercase()
            val matchesQuery = cleanQuery.isEmpty() ||
                envio.numeroSeguimiento.lowercase().contains(cleanQuery) ||
                envio.destinatarioNombre.lowercase().contains(cleanQuery) ||
                envio.destinoDireccion.lowercase().contains(cleanQuery) ||
                (envio.repartidorNombre?.lowercase()?.contains(cleanQuery) == true)

            isHistory && matchesSub && matchesProv && matchesQuery
        }
    }

    // Dynamic metrics
    val totalActivos = scopedEnvios.count { it.estado in activeStates }
    val totalEnCamino = scopedEnvios.count { it.estado == "en_camino" }
    val totalRecoleccion = scopedEnvios.count { it.estado in listOf("en_recoleccion", "recolectado", "recogiendo") }
    val totalPendientes = scopedEnvios.count { it.estado == "pendiente" }
    val montoPorCobrarRuta = scopedEnvios
        .filter { it.estado in activeStates && it.requiereCobro }
        .sumOf { it.montoCobrar }

    val totalEntregados = scopedEnvios.count { it.estado == "entregado" }
    val totalCancelados = scopedEnvios.count { it.estado in listOf("cancelado", "no_entregado") }
    val montoTotalRecaudado = scopedEnvios
        .filter { it.estado == "entregado" && it.requiereCobro }
        .sumOf { it.montoCobrar }

    // Pulsing animation for Firestore real-time indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val dateFormat = remember { SimpleDateFormat("dd/MM/yy • hh:mm a", Locale.getDefault()) }
    val syncFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Envíos & Historial",
                                color = TurboWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Live radar badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isFirebaseConnected) TurboGreen.copy(alpha = 0.2f) else Color(0xFF333333),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isFirebaseConnected) TurboGreen.copy(alpha = 0.5f) else Color(0xFF555555),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .alpha(if (isFirebaseConnected) pulseAlpha else 0.5f)
                                            .background(if (isFirebaseConnected) TurboGreen else TurboGray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isFirebaseConnected) "En Vivo Firestore" else "Local",
                                        color = if (isFirebaseConnected) TurboGreen else TurboGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isProvider) "${currentUser?.nombreNegocio ?: "Mi Negocio"} • Mérida" else "Centro de Control Operativo",
                            color = TurboGray,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TurboWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshRealtime) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar Firestore",
                            tint = TurboOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TurboDarkBg
                )
            )
        },
        containerColor = TurboDarkBg,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 740.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Real-time Firestore Sync Bar
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E222A), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF2C3240), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isFirebaseConnected) TurboGreen else TurboYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFirebaseConnected) "Listener Firestore activo • Colección 'envios'" else "Modo sin conexión",
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Sinc: ${syncFormat.format(Date(lastSyncTimestamp))}",
                                color = TurboGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Primary Tab Row: "Envíos Activos" vs "Historial de Pedidos"
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = TurboDarkCard,
                        contentColor = TurboOrange,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = TurboOrange
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TurboDarkCard, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF383838), RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Activos ($totalActivos)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Historial (${totalEntregados + totalCancelados})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (selectedTab == 0) "Buscar en activos por guía, cliente o repartidor..."
                                else "Buscar en historial por guía, cliente o repartidor..."
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TurboOrange) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TurboGray)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("realtime_search_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Provider selector filter (if admin or coord)
                if (isAdminOrCoord) {
                    val distinctProviders = scopedEnvios.map { it.proveedorNombre.ifEmpty { "Proveedor" } }.distinct()
                    if (distinctProviders.size > 1) {
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedProveedorFilter == null,
                                        onClick = { selectedProveedorFilter = null },
                                        label = { Text("Todos los negocios", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TurboOrange,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                                items(distinctProviders) { prov ->
                                    FilterChip(
                                        selected = selectedProveedorFilter == prov,
                                        onClick = { selectedProveedorFilter = prov },
                                        label = { Text(prov, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TurboOrange,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // TAB 0: ENVIOS ACTIVOS EN TIEMPO REAL
                if (selectedTab == 0) {
                    // KPI Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill(
                                label = "En Camino",
                                value = totalEnCamino.toString(),
                                accentColor = TurboPurple,
                                icon = Icons.Default.Navigation,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Por Recoger",
                                value = totalRecoleccion.toString(),
                                accentColor = TurboBlue,
                                icon = Icons.Default.Store,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Pendientes",
                                value = totalPendientes.toString(),
                                accentColor = TurboYellow,
                                icon = Icons.Default.HourglassEmpty,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (montoPorCobrarRuta > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TurboGreen.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .border(1.dp, TurboGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = TurboGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Total a cobrar en calle:", color = TurboWhite, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = "$${String.format("%.2f", montoPorCobrarRuta)} MXN",
                                        color = TurboGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subfilters for Active
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val subTabs = listOf(
                                "TODOS" to "Todos ($totalActivos)",
                                "EN_CAMINO" to "En Camino",
                                "RECOLECCION" to "Recolección",
                                "ASIGNADO" to "Asignados",
                                "PENDIENTE" to "Pendientes"
                            )

                            subTabs.forEach { (key, label) ->
                                val isSel = activeSubFilter == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) TurboOrange else TurboDarkCard, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSel) TurboOrange else Color(0xFF383838), RoundedCornerShape(8.dp))
                                        .clickable { activeSubFilter = key }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSel) Color.White else TurboGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (activeEnvios.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.DeliveryDining,
                                title = if (searchQuery.isNotEmpty()) "Sin coincidencias" else "Sin envíos activos",
                                message = if (searchQuery.isNotEmpty()) {
                                    "No se encontraron envíos activos que coincidan con '$searchQuery'."
                                } else {
                                    "No tienes pedidos activos en tránsito en este momento. Todos los pedidos se encuentran entregados o cancelados."
                                }
                            )
                        }
                    } else {
                        items(activeEnvios, key = { it.id }) { envio ->
                            ActiveEnvioRealtimeCard(
                                envio = envio,
                                pulseAlpha = pulseAlpha,
                                onClick = { onOpenDetail(envio.id) },
                                onAdvanceEstado = if (isAdminOrCoord) {
                                    { next -> onAdvanceEstado(envio.id, next) }
                                } else null,
                                onCallDestinatario = {
                                    if (envio.destinatarioTelefono.isNotEmpty()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${envio.destinatarioTelefono}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            onShowToast("Llamar: ${envio.destinatarioTelefono}")
                                        }
                                    }
                                },
                                onCopyTracking = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Guía", envio.numeroSeguimiento)
                                    clipboard.setPrimaryClip(clip)
                                    onShowToast("Guía ${envio.numeroSeguimiento} copiada")
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // TAB 1: HISTORIAL DE PEDIDOS
                if (selectedTab == 1) {
                    item {
                        // History summary KPI
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricPill(
                                label = "Entregados",
                                value = totalEntregados.toString(),
                                accentColor = TurboGreen,
                                icon = Icons.Default.CheckCircle,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Cancelados",
                                value = totalCancelados.toString(),
                                accentColor = TurboRed,
                                icon = Icons.Default.Close,
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                label = "Total Cobrado",
                                value = "$${String.format("%.0f", montoTotalRecaudado)}",
                                accentColor = TurboOrange,
                                icon = Icons.Default.MonetizationOn,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subfilters for History
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "TODOS" to "Todo el Historial (${totalEntregados + totalCancelados})",
                                "ENTREGADOS" to "Solo Entregados ($totalEntregados)",
                                "CANCELADOS" to "Cancelados ($totalCancelados)"
                            ).forEach { (key, label) ->
                                val isSel = historySubFilter == key
                                val activeColor = when (key) {
                                    "CANCELADOS" -> TurboRed
                                    "ENTREGADOS" -> TurboGreen
                                    else -> TurboOrange
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) activeColor else TurboDarkCard, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSel) activeColor else Color(0xFF383838), RoundedCornerShape(8.dp))
                                        .clickable { historySubFilter = key }
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

                    if (historyEnvios.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.History,
                                title = "Sin pedidos en el historial",
                                message = if (searchQuery.isNotEmpty()) {
                                    "No se encontraron pedidos en el historial con '$searchQuery'."
                                } else {
                                    "Aún no cuentas con pedidos finalizados o cancelados registrados."
                                }
                            )
                        }
                    } else {
                        items(historyEnvios, key = { it.id }) { envio ->
                            HistoryEnvioCard(
                                envio = envio,
                                dateFormat = dateFormat,
                                onClick = { onOpenDetail(envio.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp).navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TurboDarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, color = TurboGray, fontSize = 11.sp)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TurboWhite, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ActiveEnvioRealtimeCard(
    envio: Envio,
    pulseAlpha: Float,
    onClick: () -> Unit,
    onAdvanceEstado: ((String) -> Unit)?,
    onCallDestinatario: () -> Unit,
    onCopyTracking: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            // Live Header with Pulsing status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(pulseAlpha)
                            .background(
                                when (envio.estado) {
                                    "en_camino" -> TurboPurple
                                    "en_recoleccion", "recogiendo" -> TurboOrange
                                    "asignado" -> TurboBlue
                                    else -> TurboYellow
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(TurboOrange.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = envio.numeroSeguimiento,
                            color = TurboOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = envio.tipoServicio.uppercase(),
                        color = TurboGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusBadge(status = envio.estado)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Origin
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = TurboGray, modifier = Modifier.size(15.dp))
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
                modifier = Modifier.padding(start = 23.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Destination
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${envio.destinatarioNombre.ifEmpty { envio.clienteNombre }} (${envio.destinatarioTelefono})",
                    color = TurboWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = envio.destinoDireccion,
                color = TurboGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 23.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Driver & Payment bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(TurboBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = null,
                            tint = TurboBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = envio.repartidorNombre ?: "En espera de repartidor",
                            color = if (envio.repartidorNombre != null) TurboWhite else TurboGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (envio.repartidorId != null) "Asignado en ruta" else "Disponible para despachar",
                            color = TurboGray,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (envio.requiereCobro || envio.montoCobrar > 0) {
                        Text(
                            text = "Cobrar: $${String.format("%.2f", envio.montoCobrar)}",
                            color = TurboGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Text(text = "Sin cobro", color = TurboGray, fontSize = 12.sp)
                    }
                    Text(
                        text = "Tarifa: $${String.format("%.2f", envio.costoEnvio)}",
                        color = TurboGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Detalle", color = TurboWhite, fontSize = 12.sp)
                }

                if (envio.destinatarioTelefono.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onCallDestinatario,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = TurboGreen, modifier = Modifier.size(15.dp))
                    }
                }

                if (onAdvanceEstado != null) {
                    val nextStatus = when (envio.estado) {
                        "pendiente" -> "asignado"
                        "asignado" -> "en_recoleccion"
                        "en_recoleccion", "recogiendo" -> "en_camino"
                        "en_camino" -> "entregado"
                        else -> null
                    }
                    if (nextStatus != null) {
                        Button(
                            onClick = { onAdvanceEstado(nextStatus) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (nextStatus == "entregado") TurboGreen else TurboOrange
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Text(
                                text = when (nextStatus) {
                                    "asignado" -> "Asignar"
                                    "en_recoleccion" -> "Recolectar"
                                    "en_camino" -> "En Camino"
                                    "entregado" -> "Entregar"
                                    else -> "Avanzar"
                                },
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

@Composable
private fun HistoryEnvioCard(
    envio: Envio,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val isEntregado = envio.estado == "entregado"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, if (isEntregado) Color(0xFF2E3D2E) else Color(0xFF3D2A2A), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
                            .background(TurboOrange.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = envio.numeroSeguimiento,
                            color = TurboOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = envio.tipoServicio.uppercase(),
                        color = TurboGray,
                        fontSize = 11.sp
                    )
                }

                StatusBadge(status = envio.estado)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Destination and customer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${envio.destinatarioNombre.ifEmpty { envio.clienteNombre }} (${envio.destinatarioTelefono})",
                    color = TurboWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = envio.destinoDireccion,
                color = TurboGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 23.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Resolution info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEntregado) {
                                "Entregado por: ${envio.repartidorNombre ?: "Repartidor"}"
                            } else {
                                "Cancelado: ${envio.motivoCancelacion ?: "Cancelación registrada"}"
                            },
                            color = if (isEntregado) TurboWhite else TurboRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Fecha: ${dateFormat.format(Date(envio.fechaEntrega ?: envio.fechaCancelacion ?: envio.updatedAt))}",
                            color = TurboGray,
                            fontSize = 11.sp
                        )
                    }

                    if (isEntregado && (envio.requiereCobro || envio.montoCobrar > 0)) {
                        Text(
                            text = "Cobrado: $${String.format("%.2f", envio.montoCobrar)}",
                            color = TurboGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
