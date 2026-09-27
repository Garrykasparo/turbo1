package com.example.ui.screens.proveedor

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.EnvioEstado
import com.example.data.model.FormaPago
import com.example.data.model.TipoServicio
import com.example.data.model.User
import com.example.ui.screens.customTextFieldColors
import com.example.ui.theme.TurboBlue
import com.example.ui.theme.TurboDarkBg
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboGreen
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboWhite
import com.example.ui.theme.TurboYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoEnvioScreen(
    currentUser: User?,
    onBack: () -> Unit,
    onCreateEnvio: (Envio, (Envio) -> Unit) -> Unit,
    onViewDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    // Service state
    var selectedTipoServicio by remember { mutableStateOf(TipoServicio.EXPRESS) }

    // Origin fields
    var origenNombre by remember {
        mutableStateOf(currentUser?.nombreNegocio ?: currentUser?.fullName ?: "")
    }
    var origenDireccion by remember {
        mutableStateOf(currentUser?.direccion ?: "Calle 60 #345 x 35 y 37, Centro, Mérida")
    }
    var origenTelefono by remember {
        mutableStateOf(currentUser?.telefono ?: "")
    }
    var origenReferencias by remember {
        mutableStateOf("")
    }

    // Destination fields
    var destinatarioNombre by remember { mutableStateOf("") }
    var destinatarioTelefono by remember { mutableStateOf("") }
    var destinoDireccion by remember { mutableStateOf("") }
    var destinoReferencias by remember { mutableStateOf("") }

    // Schedule
    var horarioTipo by remember { mutableStateOf("inmediato") } // "inmediato" | "programado"
    var franjaHoraria by remember { mutableStateOf("12:00 PM - 02:00 PM") }

    // Payment & billing
    var requiereCobro by remember { mutableStateOf(false) }
    var montoCobrar by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf(FormaPago.EFECTIVO.value) }

    // Notes
    var indicacionesRepartidor by remember { mutableStateOf("") }
    var notasInternas by remember { mutableStateOf("") }

    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdEnvioResult by remember { mutableStateOf<Envio?>(null) }

    val costoEnvio = selectedTipoServicio.tarifaBase
    val distanciaKm = when (selectedTipoServicio) {
        TipoServicio.BASICO -> 3.2
        TipoServicio.EXPRESS -> 4.5
        TipoServicio.PROGRAMADO -> 5.0
        TipoServicio.TURBO -> 9.8
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuevo Pedido",
                            color = TurboWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Turbo Mandados • Mérida",
                            color = TurboGray,
                            fontSize = 12.sp
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
                // Section 1: Service Type
                item {
                    FormSectionHeader(
                        title = "1. TIPO DE SERVICIO",
                        subtitle = "Selecciona la modalidad de entrega adecuada para tu mandado"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ServiceCardOption(
                            tipo = TipoServicio.BASICO,
                            icon = Icons.Default.AllInbox,
                            isSelected = selectedTipoServicio == TipoServicio.BASICO,
                            onClick = { selectedTipoServicio = TipoServicio.BASICO }
                        )

                        ServiceCardOption(
                            tipo = TipoServicio.EXPRESS,
                            icon = Icons.Default.Bolt,
                            isSelected = selectedTipoServicio == TipoServicio.EXPRESS,
                            onClick = { selectedTipoServicio = TipoServicio.EXPRESS }
                        )

                        ServiceCardOption(
                            tipo = TipoServicio.PROGRAMADO,
                            icon = Icons.Default.Schedule,
                            isSelected = selectedTipoServicio == TipoServicio.PROGRAMADO,
                            onClick = {
                                selectedTipoServicio = TipoServicio.PROGRAMADO
                                horarioTipo = "programado"
                            }
                        )

                        ServiceCardOption(
                            tipo = TipoServicio.TURBO,
                            icon = Icons.Default.RocketLaunch,
                            isSelected = selectedTipoServicio == TipoServicio.TURBO,
                            onClick = { selectedTipoServicio = TipoServicio.TURBO }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 2: Origin
                item {
                    FormSectionHeader(
                        title = "2. DATOS DE ORIGEN (RECOGIDA)",
                        subtitle = "Punto donde el repartidor recogerá los paquetes"
                    )

                    OutlinedTextField(
                        value = origenNombre,
                        onValueChange = { origenNombre = it },
                        label = { Text("Nombre del Negocio o Punto") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TurboOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("origen_nombre_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = origenDireccion,
                        onValueChange = { origenDireccion = it },
                        label = { Text("Dirección de Recogida (Mérida)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TurboOrange) },
                        modifier = Modifier.fillMaxWidth().testTag("origen_direccion_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = origenTelefono,
                            onValueChange = { origenTelefono = it },
                            label = { Text("Teléfono de Contacto") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TurboGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            colors = customTextFieldColors()
                        )

                        OutlinedTextField(
                            value = origenReferencias,
                            onValueChange = { origenReferencias = it },
                            label = { Text("Referencias origen") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = customTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 3: Destination
                item {
                    FormSectionHeader(
                        title = "3. DATOS DE DESTINO Y CLIENTE",
                        subtitle = "Información del destinatario para la entrega final"
                    )

                    OutlinedTextField(
                        value = destinatarioNombre,
                        onValueChange = { destinatarioNombre = it },
                        label = { Text("Nombre del Destinatario / Cliente *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TurboOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("destinatario_nombre_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinatarioTelefono,
                        onValueChange = { destinatarioTelefono = it },
                        label = { Text("Teléfono del Destinatario *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TurboOrange) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("destinatario_telefono_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinoDireccion,
                        onValueChange = { destinoDireccion = it },
                        label = { Text("Dirección de Entrega en Mérida *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TurboOrange) },
                        modifier = Modifier.fillMaxWidth().testTag("destino_direccion_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinoReferencias,
                        onValueChange = { destinoReferencias = it },
                        label = { Text("Referencias de Entrega (cruces, color de fachada, timbre)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 4: Schedule
                item {
                    FormSectionHeader(
                        title = "4. PROGRAMACIÓN DE HORARIO",
                        subtitle = "Indica si la recolección es inmediata o agendada"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScheduleTabButton(
                            title = "Inmediato",
                            subtitle = "Lo antes posible",
                            isSelected = horarioTipo == "inmediato",
                            onClick = { horarioTipo = "inmediato" },
                            modifier = Modifier.weight(1f)
                        )

                        ScheduleTabButton(
                            title = "Programado",
                            subtitle = "Seleccionar franja",
                            isSelected = horarioTipo == "programado",
                            onClick = { horarioTipo = "programado" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (horarioTipo == "programado") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Franja horaria para entrega:",
                            color = TurboGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("10:00 - 12:00", "12:00 - 14:00", "14:00 - 16:00", "17:00 - 19:00").forEach { franja ->
                                val isSel = franjaHoraria == franja
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) TurboOrange else TurboDarkCard, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (isSel) TurboOrange else Color(0xFF444444), RoundedCornerShape(8.dp))
                                        .clickable { franjaHoraria = franja }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = franja,
                                        color = if (isSel) Color.White else TurboGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 5: Cobro y Pago
                item {
                    FormSectionHeader(
                        title = "5. COBRO Y FORMA DE PAGO",
                        subtitle = "Especifica si el repartidor debe cobrar en destino"
                    )

                    // Switch requiere cobro
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TurboDarkCard, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF383838), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¿Cobrar al cliente en destino?",
                                    color = TurboWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (requiereCobro) "El repartidor cobrará el monto al entregar" else "Pedido ya pagado previamente o sin costo",
                                    color = TurboGray,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = requiereCobro,
                                onCheckedChange = {
                                    requiereCobro = it
                                    if (!it) metodoPago = FormaPago.SIN_COBRO.value
                                    else if (metodoPago == FormaPago.SIN_COBRO.value) metodoPago = FormaPago.EFECTIVO.value
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = TurboGreen,
                                    uncheckedThumbColor = TurboGray,
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }
                    }

                    if (requiereCobro) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = montoCobrar,
                            onValueChange = { montoCobrar = it },
                            label = { Text("Monto a Cobrar al Destinatario ($ MXN) *") },
                            leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = TurboGreen) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("monto_cobrar_input"),
                            colors = customTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Método de Pago:",
                        color = TurboGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val paymentMethods = listOf(
                            Triple(FormaPago.EFECTIVO.value, "Efectivo", Icons.Default.Payments),
                            Triple(FormaPago.TRANSFERENCIA.value, "Transf.", Icons.Default.CurrencyExchange),
                            Triple(FormaPago.TARJETA.value, "Tarjeta", Icons.Default.CreditCard),
                            Triple(FormaPago.SIN_COBRO.value, "Sin Cobro", Icons.Default.AllInbox)
                        )

                        paymentMethods.forEach { (value, label, icon) ->
                            val isSel = metodoPago == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) TurboGreen.copy(alpha = 0.2f) else TurboDarkCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isSel) TurboGreen else Color(0xFF444444), RoundedCornerShape(10.dp))
                                    .clickable {
                                        metodoPago = value
                                        if (value == FormaPago.SIN_COBRO.value) requiereCobro = false
                                        else requiereCobro = true
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSel) TurboGreen else TurboGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        color = if (isSel) TurboWhite else TurboGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 6: Instructions
                item {
                    FormSectionHeader(
                        title = "6. INDICACIONES Y NOTAS",
                        subtitle = "Instrucciones de entrega para el repartidor"
                    )

                    OutlinedTextField(
                        value = indicacionesRepartidor,
                        onValueChange = { indicacionesRepartidor = it },
                        label = { Text("Indicaciones al Repartidor (ej. Llevar cambio de $200)") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = TurboOrange) },
                        modifier = Modifier.fillMaxWidth().testTag("indicaciones_input"),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notasInternas,
                        onValueChange = { notasInternas = it },
                        label = { Text("Notas Internas (opcional, visible solo para ti)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 7: Summary Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222), RoundedCornerShape(16.dp))
                            .border(1.dp, TurboOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Resumen del Envío",
                                    color = TurboWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(TurboOrange.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = selectedTipoServicio.label,
                                        color = TurboOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SummaryRow(label = "Distancia estimada:", value = "~ $distanciaKm km")
                            SummaryRow(label = "Tarifa base de entrega:", value = "$${String.format("%.2f", costoEnvio)} MXN")
                            if (requiereCobro && montoCobrar.isNotBlank()) {
                                SummaryRow(
                                    label = "Cobro a cliente en destino:",
                                    value = "$${String.format("%.2f", montoCobrar.toDoubleOrNull() ?: 0.0)} MXN",
                                    valueColor = TurboGreen
                                )
                            }
                            SummaryRow(label = "Forma de pago de mercancía:", value = metodoPago.replaceFirstChar { it.uppercase() })
                            SummaryRow(label = "Tiempo estimado:", value = if (horarioTipo == "inmediato") "30-45 min" else franjaHoraria)
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF3A1A1A), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (destinatarioNombre.isBlank()) {
                                errorMessage = "Por favor ingresa el nombre del destinatario."
                                return@Button
                            }
                            if (destinatarioTelefono.isBlank()) {
                                errorMessage = "Por favor ingresa el teléfono del destinatario."
                                return@Button
                            }
                            if (destinoDireccion.isBlank()) {
                                errorMessage = "Por favor ingresa la dirección de entrega."
                                return@Button
                            }
                            if (origenDireccion.isBlank()) {
                                errorMessage = "Por favor ingresa la dirección de origen."
                                return@Button
                            }

                            errorMessage = null
                            isLoading = true

                            val trackingCode = Envio.generateTrackingNumber()
                            val parsedMonto = if (requiereCobro) (montoCobrar.toDoubleOrNull() ?: 0.0) else 0.0

                            val newEnvio = Envio(
                                numeroSeguimiento = trackingCode,
                                proveedorId = currentUser?.id ?: "unknown",
                                proveedorNombre = currentUser?.fullName ?: "Proveedor",
                                proveedorNegocio = currentUser?.nombreNegocio ?: currentUser?.fullName ?: "Mi Negocio",
                                creadoPorUserId = currentUser?.id ?: "unknown",
                                creadoPorRol = currentUser?.role ?: "proveedor",
                                origenNombre = origenNombre.ifBlank { currentUser?.nombreNegocio },
                                origenDireccion = origenDireccion.trim(),
                                origenReferencias = origenReferencias.ifBlank { null },
                                origenTelefono = origenTelefono.ifBlank { null },
                                destinatarioNombre = destinatarioNombre.trim(),
                                destinatarioTelefono = destinatarioTelefono.trim(),
                                destinoDireccion = destinoDireccion.trim(),
                                destinoReferencias = destinoReferencias.ifBlank { null },
                                clienteNombre = destinatarioNombre.trim(),
                                tipoServicio = selectedTipoServicio.value,
                                estado = EnvioEstado.PENDIENTE.value,
                                horarioTipo = horarioTipo,
                                franjaHoraria = if (horarioTipo == "programado") franjaHoraria else null,
                                requiereCobro = requiereCobro,
                                montoCobrar = parsedMonto,
                                metodoPago = metodoPago,
                                formaPago = metodoPago,
                                distanciaKm = distanciaKm,
                                costoEnvio = costoEnvio,
                                indicacionesRepartidor = indicacionesRepartidor.ifBlank { null },
                                notasInternas = notasInternas.ifBlank { null },
                                notas = indicacionesRepartidor.ifBlank { null }
                            )

                            onCreateEnvio(newEnvio) { created ->
                                isLoading = false
                                createdEnvioResult = created
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_nuevo_envio_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar y Generar Guía TM",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp).navigationBarsPadding())
                }
            }
        }
    }

    // Success Dialog with Generated Tracking Number
    if (createdEnvioResult != null) {
        val envio = createdEnvioResult!!
        AlertDialog(
            onDismissRequest = { /* require button */ },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(TurboGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TurboGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "¡Pedido Creado con Éxito!",
                        color = TurboWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tu número de seguimiento único e inmutable es:",
                        color = TurboGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .background(TurboOrange.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                            .border(1.dp, TurboOrange, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = envio.numeroSeguimiento,
                            color = TurboOrange,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "El pedido ha sido registrado en la colección central 'envios' y sincronizado con Firestore. El equipo de coordinación asignará un repartidor.",
                        color = TurboGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = envio.id
                        createdEnvioResult = null
                        onViewDetail(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ver Detalle del Pedido", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        createdEnvioResult = null
                        onBack()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Volver al Panel", color = TurboWhite)
                }
            },
            containerColor = TurboDarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FormSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)) {
        Text(
            text = title,
            color = TurboOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = subtitle,
            color = TurboGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ServiceCardOption(
    tipo: TipoServicio,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) TurboOrange.copy(alpha = 0.14f) else TurboDarkCard, RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) TurboOrange else Color(0xFF383838),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("service_card_${tipo.value}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (isSelected) TurboOrange else Color(0xFF262626),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TurboOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tipo.label,
                        color = TurboWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$${String.format("%.0f", tipo.tarifaBase)} MXN",
                        color = if (isSelected) TurboOrange else TurboGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tipo.descripcion,
                    color = TurboGray,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ScheduleTabButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isSelected) TurboOrange.copy(alpha = 0.18f) else TurboDarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) TurboOrange else Color(0xFF383838), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = if (isSelected) TurboOrange else TurboWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TurboGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SummaryRow(
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
        Text(text = label, color = TurboGray, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
