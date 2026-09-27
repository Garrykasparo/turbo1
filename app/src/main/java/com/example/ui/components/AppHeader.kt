package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.TurboDarkBg
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow

@Composable
fun AppHeader(
    currentUser: User?,
    onLogout: () -> Unit,
    onSwitchUser: (String) -> Unit,
    isFirebaseConnected: Boolean = true,
    firebaseStatusMessage: String = "Conectado a Firebase Firestore y Auth",
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSwitchDialog by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TurboDarkBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo & Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(TurboOrange, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricMoped,
                    contentDescription = "Turbo Mandados Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Turbo",
                        color = TurboWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mandados",
                        color = TurboOrange,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                if (currentUser != null) {
                    Text(
                        text = "${currentUser.fullName} (${currentUser.role.uppercase()})",
                        color = TurboGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        // Action Buttons: Cloud Status + Demo Switcher + Logout
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Firebase Cloud Sync Indicator
            IconButton(
                onClick = { showCloudDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("firebase_status_button")
            ) {
                Icon(
                    imageVector = if (isFirebaseConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                    contentDescription = "Estado de Firebase Firestore",
                    tint = if (isFirebaseConnected) TurboGreen else TurboYellow,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Demo account switcher button
            IconButton(
                onClick = { showSwitchDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("switch_user_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Cambiar cuenta de prueba",
                    tint = TurboOrange,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Logout Button
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = TurboGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // Cloud Status Dialog
    if (showCloudDialog) {
        AlertDialog(
            onDismissRequest = { showCloudDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFirebaseConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (isFirebaseConnected) TurboGreen else TurboYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Base de Datos y Auth",
                        fontWeight = FontWeight.Bold,
                        color = TurboWhite
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = firebaseStatusMessage,
                        color = TurboWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Colecciones Firestore activas:",
                        color = TurboGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• users (perfiles y roles de autenticación)\n• envios (pedidos y trazabilidad)\n• repartidores (flota y estados operativos)",
                        color = TurboOrange,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Arquitectura Híbrida: La app utiliza Room como motor de base de datos local ultra-rápido y Firestore en la nube para persistencia y sincronización en tiempo real.",
                        color = TurboGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSyncClick()
                        showCloudDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TurboOrange)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sincronizar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloudDialog = false }) {
                    Text("Cerrar", color = TurboGray)
                }
            },
            containerColor = Color(0xFF222222)
        )
    }

    if (showSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDialog = false },
            title = {
                Text(
                    text = "Cambiar Usuario (Pruebas Demo)",
                    fontWeight = FontWeight.Bold,
                    color = TurboWhite
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selecciona una cuenta para probar los diferentes roles y verificar los permisos:",
                        color = TurboGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val demoUsers = listOf(
                        Triple("admin@turbomandados.mx", "Administrador Central", "admin"),
                        Triple("sureno@turbomandados.mx", "Restaurante El Sureño", "proveedor"),
                        Triple("progreso@turbomandados.mx", "Farmacia Progreso", "proveedor"),
                        Triple("carlos@repartidor.mx", "Carlos Méndez (Moto)", "repartidor"),
                        Triple("sofia@repartidor.mx", "Sofía Chan (Bici)", "repartidor"),
                        Triple("coordinador@turbomandados.mx", "Valeria Morales", "coordinador"),
                        Triple("tacos.amigo@merida.mx", "Pedro Novelo (Pendiente)", "pendiente")
                    )

                    demoUsers.forEach { (email, name, role) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(TurboDarkCard, RoundedCornerShape(8.dp))
                                .clickable {
                                    showSwitchDialog = false
                                    onSwitchUser(email)
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = name, color = TurboWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = email, color = TurboGray, fontSize = 11.sp)
                                }
                                StatusBadge(status = role)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchDialog = false }) {
                    Text("Cerrar", color = TurboOrange)
                }
            },
            containerColor = Color(0xFF222222)
        )
    }
}
