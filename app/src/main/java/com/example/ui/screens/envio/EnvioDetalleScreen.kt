package com.example.ui.screens.envio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.HistorialItem
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.ui.components.CancelarEnvioDialog
import com.example.ui.components.StatusBadge
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
fun EnvioDetalleScreen(
    envio: Envio?,
    currentUser: User?,
    repartidores: List<Repartidor>,
    onBack: () -> Unit,
    onEditEnvio: (String) -> Unit,
    onCancelEnvio: (envioId: String, motivo: String) -> Unit,
    onAdvanceEstado: (envioId: String, nuevoEstado: String) -> Unit,
    onAssignRepartidor: (envioId: String, repId: String, repNombre: String) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }

    if (envio == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TurboDarkBg)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Envío no encontrado", color = TurboWhite, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = TurboOrange)) {
                    Text("Volver")
                }
            }
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val historialList = remember(envio.historialJson) { envio.parseHistorial() }

    val userRole = currentUser?.role?.lowercase() ?: "proveedor"
    val isOwner = envio.proveedorId == currentUser?.id
    val isAdminOrCoord = userRole == "admin" || userRole == "coordinador" || userRole == "asistente"
    val canEdit = (isOwner || isAdminOrCoord) && envio.estado == "pendiente"
    val canCancel = (isOwner || isAdminOrCoord) && envio.estado != "entregado" && envio.estado != "cancelado"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = envio.numeroSeguimiento,
                            color = TurboOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Guía Turbo Mandados", envio.numeroSeguimiento)
                                clipboard.setPrimaryClip(clip)
                                onShowToast("Número de seguimiento copiado al portapapeles")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar número de guía",
                                tint = TurboGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                    if (canEdit) {
                        IconButton(onClick = { onEditEnvio(envio.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TurboBlue)
                        }
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
                    .widthIn(max = 720.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Status Header Card
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
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
                                Column {
                                    Text(
                                        text = "Estado del Servicio",
                                        color = TurboGray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Servicio ${envio.tipoServicio.uppercase()}",
                                        color = TurboWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                StatusBadge(status = envio.estado)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Creado el ${dateFormat.format(Date(envio.createdAt))}",
                                color = TurboGray,
                                fontSize = 12.sp
                            )

                            if (envio.horarioTipo == "programado") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TurboYellow, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Entrega programada: ${envio.franjaHoraria ?: "Horario pactado"}",
                                        color = TurboYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Cancellation Banner
                    if (envio.estado == "cancelado") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TurboRed.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, TurboRed, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = TurboRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Pedido Cancelado", color = TurboRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "Motivo: ${envio.motivoCancelacion ?: "Cancelación registrada"}",
                                        color = TurboWhite,
                                        fontSize = 13.sp
                                    )
                                    if (envio.canceladoPor != null) {
                                        Text(
                                            text = "Cancelado por: ${envio.canceladoPor} ${envio.fechaCancelacion?.let { "• ${dateFormat.format(Date(it))}" } ?: ""}",
                                            color = TurboGray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Origin Card
                item {
                    SectionCard(
                        title = "ORIGEN / RECOGIDA",
                        icon = Icons.Default.Store,
                        iconColor = TurboOrange
                    ) {
                        Text(
                            text = envio.origenNombre ?: envio.proveedorNombre.ifEmpty { "Proveedor" },
                            color = TurboWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = envio.origenDireccion,
                            color = TurboGray,
                            fontSize = 13.sp
                        )
                        if (!envio.origenReferencias.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Referencias: ${envio.origenReferencias}",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }
                        if (!envio.origenTelefono.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = TurboGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = envio.origenTelefono ?: "", color = TurboWhite, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Destination Card
                item {
                    SectionCard(
                        title = "DESTINO / ENTREGA",
                        icon = Icons.Default.LocationOn,
                        iconColor = TurboOrange
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = envio.destinatarioNombre.ifEmpty { envio.clienteNombre },
                                    color = TurboWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (envio.destinatarioTelefono.isNotEmpty()) {
                                    Text(
                                        text = envio.destinatarioTelefono,
                                        color = TurboGray,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            if (envio.destinatarioTelefono.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${envio.destinatarioTelefono}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            onShowToast("Llamar a ${envio.destinatarioTelefono}")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = TurboOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Llamar", color = TurboWhite, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = envio.destinoDireccion,
                            color = TurboWhite,
                            fontSize = 13.sp
                        )

                        if (!envio.destinoReferencias.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Referencias: ${envio.destinoReferencias}",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Assigned Delivery Driver
                item {
                    SectionCard(
                        title = "REPARTIDOR ASIGNADO",
                        icon = Icons.Default.DeliveryDining,
                        iconColor = TurboBlue
                    ) {
                        if (envio.repartidorId != null && envio.repartidorNombre != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(TurboBlue.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = TurboBlue, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = envio.repartidorNombre ?: "Repartidor",
                                            color = TurboWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ID: ${envio.repartidorId}",
                                            color = TurboGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (isAdminOrCoord) {
                                    TextButton(onClick = { showAssignDialog = true }) {
                                        Text("Reasignar", color = TurboOrange, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(0xFF2A2A2A), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = TurboGray, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Sin repartidor asignado",
                                            color = TurboWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Pendiente de confirmación operativa",
                                            color = TurboGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (isAdminOrCoord) {
                                    Button(
                                        onClick = { showAssignDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = TurboBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Asignar", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Billing and Pricing Card
                item {
                    SectionCard(
                        title = "COBRO Y TARIFA",
                        icon = Icons.Default.MonetizationOn,
                        iconColor = TurboGreen
                    ) {
                        DetailRow(label = "Tarifa del servicio:", value = "$${String.format("%.2f", envio.costoEnvio)} MXN")
                        DetailRow(label = "Distancia estimada:", value = "~ ${envio.distanciaKm ?: 3.5} km")
                        DetailRow(label = "¿Requiere cobro al cliente?", value = if (envio.requiereCobro || envio.montoCobrar > 0) "SÍ" else "NO")

                        if (envio.requiereCobro || envio.montoCobrar > 0) {
                            DetailRow(
                                label = "Monto a cobrar en destino:",
                                value = "$${String.format("%.2f", envio.montoCobrar)} MXN",
                                valueColor = TurboGreen
                            )
                        }
                        DetailRow(
                            label = "Forma de pago:",
                            value = envio.metodoPago.replaceFirstChar { it.uppercase() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Instructions & Notes Card
                if (!envio.indicacionesRepartidor.isNullOrBlank() || !envio.notasInternas.isNullOrBlank() || !envio.notas.isNullOrBlank()) {
                    item {
                        SectionCard(
                            title = "INDICACIONES Y NOTAS",
                            icon = Icons.Default.Notes,
                            iconColor = TurboYellow
                        ) {
                            if (!envio.indicacionesRepartidor.isNullOrBlank()) {
                                Text(text = "Indicaciones para el repartidor:", color = TurboGray, fontSize = 12.sp)
                                Text(
                                    text = envio.indicacionesRepartidor ?: "",
                                    color = TurboWhite,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            if (!envio.notasInternas.isNullOrBlank()) {
                                Text(text = "Notas internas:", color = TurboGray, fontSize = 12.sp)
                                Text(
                                    text = envio.notasInternas ?: "",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Historical Timeline (Historial de Estados)
                item {
                    SectionCard(
                        title = "HISTORIAL DE ESTADOS",
                        icon = Icons.Default.History,
                        iconColor = TurboPurple
                    ) {
                        if (historialList.isEmpty()) {
                            Text(
                                text = "Sin cambios registrados aún",
                                color = TurboGray,
                                fontSize = 13.sp
                            )
                        } else {
                            Column {
                                historialList.forEachIndexed { index, item ->
                                    val isLast = index == historialList.size - 1
                                    TimelineItemView(
                                        item = item,
                                        isLast = isLast,
                                        timeFormat = dateFormat
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Actions Card based on Role and Status
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Admin / Coord status progression
                        if (isAdminOrCoord && envio.estado != "entregado" && envio.estado != "cancelado") {
                            val nextState = when (envio.estado) {
                                "pendiente" -> "asignado"
                                "asignado" -> "en_recoleccion"
                                "aceptado" -> "en_recoleccion"
                                "en_recoleccion", "recogiendo" -> "en_camino"
                                "recolectado" -> "en_camino"
                                "en_camino" -> "entregado"
                                else -> null
                            }

                            if (nextState != null) {
                                val buttonLabel = when (nextState) {
                                    "asignado" -> "Asignar a Repartidor"
                                    "en_recoleccion" -> "Marcar en Recolección"
                                    "en_camino" -> "Marcar en Camino a Destino"
                                    "entregado" -> "Confirmar Entrega Final"
                                    else -> "Avanzar Estado"
                                }

                                Button(
                                    onClick = { onAdvanceEstado(envio.id, nextState) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (nextState == "entregado") TurboGreen else TurboOrange
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(
                                        imageVector = if (nextState == "entregado") Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(buttonLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        // Provider Edit button if pendiente
                        if (canEdit) {
                            Button(
                                onClick = { onEditEnvio(envio.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = TurboBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editar Datos del Pedido", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Cancel button
                        if (canCancel) {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TurboRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancelar Pedido", color = TurboRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp).navigationBarsPadding())
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog && envio != null) {
        CancelarEnvioDialog(
            numeroSeguimiento = envio.numeroSeguimiento,
            onDismiss = { showCancelDialog = false },
            onConfirmCancel = { motivo ->
                showCancelDialog = false
                onCancelEnvio(envio.id, motivo)
            }
        )
    }

    // Assign Driver Dialog for Admin / Coord
    if (showAssignDialog && envio != null) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = {
                Text(text = "Asignar Repartidor", color = TurboWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Selecciona el repartidor que atenderá el pedido ${envio.numeroSeguimiento}:",
                        color = TurboGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (repartidores.isEmpty()) {
                        Text(text = "No hay repartidores registrados en el sistema.", color = TurboRed, fontSize = 13.sp)
                    } else {
                        repartidores.forEach { rep ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFF262626), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF383838), RoundedCornerShape(10.dp))
                                    .clickable {
                                        onAssignRepartidor(envio.id, rep.id, rep.nombreCompleto)
                                        showAssignDialog = false
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = rep.nombreCompleto, color = TurboWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "${rep.visualCode} • ${rep.vehiculoTipo.uppercase()} • Tel: ${rep.telefono}", color = TurboGray, fontSize = 12.sp)
                                    }
                                    StatusBadge(status = rep.estado)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cerrar", color = TurboGray)
                }
            },
            containerColor = TurboDarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TurboDarkCard, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = iconColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TurboWhite
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TurboGray, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TimelineItemView(
    item: HistorialItem,
    isLast: Boolean,
    timeFormat: SimpleDateFormat
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(TurboOrange, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(Color(0xFF444444))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.estado.uppercase(),
                    color = TurboWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeFormat.format(Date(item.fecha)),
                    color = TurboGray,
                    fontSize = 11.sp
                )
            }
            if (!item.descripcion.isNullOrBlank()) {
                Text(
                    text = item.descripcion,
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (!item.usuarioNombre.isNullOrBlank()) {
                Text(
                    text = "Por: ${item.usuarioNombre}",
                    color = TurboGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
