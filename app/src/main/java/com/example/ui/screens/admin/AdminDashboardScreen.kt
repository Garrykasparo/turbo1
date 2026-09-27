package com.example.ui.screens.admin

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.ui.components.AppHeader
import com.example.ui.components.ConfirmDialog
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
import com.example.ui.theme.TurboRed
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    currentUser: User?,
    pendingUsers: List<User>,
    allUsers: List<User>,
    allEnvios: List<Envio>,
    allRepartidores: List<Repartidor>,
    countPendientes: Int,
    countActivos: Int,
    countEntregados: Int,
    countDisponibles: Int,
    countOcupados: Int,
    onApproveUser: (String) -> Unit,
    onRejectUser: (String) -> Unit,
    onAdvanceEnvio: (String, String) -> Unit,
    onLogout: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onOpenDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var userToReject by remember { mutableStateOf<User?>(null) }

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
                    text = "Centro de Operaciones",
                    color = TurboWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Control logístico global de Turbo Mandados • Mérida, Yucatán",
                    color = TurboGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // 5 KPIs Section
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
                        title = "Rep. Libres",
                        value = countDisponibles.toString(),
                        icon = Icons.Default.TwoWheeler,
                        accentColor = TurboGreen,
                        modifier = Modifier.weight(1f),
                        subtext = "Disponibles"
                    )
                    StatCard(
                        title = "Rep. Activos",
                        value = countOcupados.toString(),
                        icon = Icons.Default.LocalShipping,
                        accentColor = TurboOrange,
                        modifier = Modifier.weight(1f),
                        subtext = "En pedido"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tabs for navigation
                val tabs = listOf(
                    "Solicitudes (${pendingUsers.size})",
                    "Envíos (${allEnvios.size})",
                    "Repartidores (${allRepartidores.size})",
                    "Usuarios (${allUsers.size})"
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = TurboDarkCard,
                    contentColor = TurboOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = TurboOrange
                        )
                    },
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) TurboOrange else TurboGray,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("admin_tab_$index")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: Solicitudes de Usuarios Pendientes
                    if (pendingUsers.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.PendingActions,
                                title = "Sin solicitudes pendientes",
                                message = "No hay nuevos usuarios pendientes de aprobación en este momento. Todas las cuentas registradas están al día."
                            )
                        }
                    } else {
                        items(pendingUsers) { user ->
                            PendingUserCard(
                                user = user,
                                onApprove = { onApproveUser(user.id) },
                                onReject = { userToReject = user }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                1 -> {
                    // TAB 1: Envíos
                    if (allEnvios.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.DeliveryDining,
                                title = "No hay envíos registrados",
                                message = "Los envíos creados por proveedores aparecerán aquí."
                            )
                        }
                    } else {
                        items(allEnvios) { envio ->
                            EnvioCard(
                                envio = envio,
                                onClick = { onOpenDetail(envio.id) },
                                onStatusAdvance = { nextStatus ->
                                    onAdvanceEnvio(envio.id, nextStatus)
                                },
                                showAdminActions = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                2 -> {
                    // TAB 2: Repartidores
                    if (allRepartidores.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.TwoWheeler,
                                title = "Sin repartidores registrados",
                                message = "Los repartidores aprobados se mostrarán en esta lista."
                            )
                        }
                    } else {
                        items(allRepartidores) { rep ->
                            RepartidorCard(repartidor = rep)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                3 -> {
                    // TAB 3: Todos los usuarios
                    items(allUsers) { user ->
                        AllUsersCard(user = user)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Confirmation dialog before rejecting a user
    ConfirmDialog(
        show = userToReject != null,
        title = "¿Rechazar solicitud?",
        message = "El usuario '${userToReject?.fullName}' (${userToReject?.email}) no podrá ingresar al sistema como ${userToReject?.role}.",
        confirmText = "Rechazar Usuario",
        cancelText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            userToReject?.let { onRejectUser(it.id) }
            userToReject = null
        },
        onDismiss = { userToReject = null }
    )
}

@Composable
private fun PendingUserCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateStr = remember(user.fechaRegistro) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(user.fechaRegistro))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("pending_user_card_${user.id}")
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
                            .size(42.dp)
                            .background(TurboOrange.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = TurboOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = user.fullName,
                            color = TurboWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            color = TurboGray,
                            fontSize = 12.sp
                        )
                    }
                }
                StatusBadge(status = user.role)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF202020), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Teléfono: ${user.telefono}", color = TurboWhite, fontSize = 12.sp)
                    if (!user.nombreNegocio.isNullOrEmpty()) {
                        Text(text = "Negocio: ${user.nombreNegocio}", color = TurboOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(text = dateStr, color = TurboGray, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Aprobar & Rechazar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TurboRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(1.dp, TurboRed, RoundedCornerShape(10.dp))
                        .testTag("reject_user_button_${user.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TurboRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Rechazar", color = TurboRed, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = TurboGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("approve_user_button_${user.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Aprobar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AllUsersCard(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = user.fullName, color = TurboWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "${user.email} • ${user.telefono}", color = TurboGray, fontSize = 12.sp)
                if (!user.nombreNegocio.isNullOrEmpty()) {
                    Text(text = user.nombreNegocio, color = TurboOrange, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = user.role)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        user.rechazado -> "Rechazado"
                        user.aprobadoPorAdmin -> "Aprobado"
                        else -> "Pendiente"
                    },
                    color = when {
                        user.rechazado -> TurboRed
                        user.aprobadoPorAdmin -> TurboGreen
                        else -> TurboYellow
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
