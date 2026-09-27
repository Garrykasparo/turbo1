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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Envio
import com.example.data.model.FormaPago
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
fun EditarEnvioScreen(
    envio: Envio?,
    onBack: () -> Unit,
    onSaveUpdate: (Envio) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

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

    var destinatarioNombre by remember { mutableStateOf(envio.destinatarioNombre.ifEmpty { envio.clienteNombre }) }
    var destinatarioTelefono by remember { mutableStateOf(envio.destinatarioTelefono) }
    var destinoDireccion by remember { mutableStateOf(envio.destinoDireccion) }
    var destinoReferencias by remember { mutableStateOf(envio.destinoReferencias ?: "") }
    var requiereCobro by remember { mutableStateOf(envio.requiereCobro || envio.montoCobrar > 0) }
    var montoCobrar by remember { mutableStateOf(if (envio.montoCobrar > 0) envio.montoCobrar.toString() else "") }
    var metodoPago by remember { mutableStateOf(envio.metodoPago) }
    var indicacionesRepartidor by remember { mutableStateOf(envio.indicacionesRepartidor ?: envio.notas ?: "") }
    var notasInternas by remember { mutableStateOf(envio.notasInternas ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Editar Pedido",
                            color = TurboWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = envio.numeroSeguimiento,
                            color = TurboOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
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
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TurboBlue.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .border(1.dp, TurboBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Puedes actualizar los datos del cliente, dirección e instrucciones mientras el pedido esté en estado 'Pendiente'.",
                            color = TurboWhite,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DATOS DE DESTINO",
                        color = TurboOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = destinatarioNombre,
                        onValueChange = { destinatarioNombre = it },
                        label = { Text("Nombre del Destinatario") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TurboOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinatarioTelefono,
                        onValueChange = { destinatarioTelefono = it },
                        label = { Text("Teléfono de Contacto") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TurboOrange) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinoDireccion,
                        onValueChange = { destinoDireccion = it },
                        label = { Text("Dirección de Entrega") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TurboOrange) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinoReferencias,
                        onValueChange = { destinoReferencias = it },
                        label = { Text("Referencias de Entrega (fachada, cruces)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "COBRO Y PAGO",
                        color = TurboGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
                                Text(text = "¿Requiere cobro al cliente?", color = TurboWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Monto a cobrar por el repartidor", color = TurboGray, fontSize = 12.sp)
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
                                    checkedTrackColor = TurboGreen
                                )
                            )
                        }
                    }

                    if (requiereCobro) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = montoCobrar,
                            onValueChange = { montoCobrar = it },
                            label = { Text("Monto a Cobrar ($ MXN)") },
                            leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = TurboGreen) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = customTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Método de Pago:", color = TurboGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(FormaPago.EFECTIVO.value, "Efectivo", Icons.Default.Payments),
                            Triple(FormaPago.TRANSFERENCIA.value, "Transf.", Icons.Default.CurrencyExchange),
                            Triple(FormaPago.TARJETA.value, "Tarjeta", Icons.Default.CreditCard),
                            Triple(FormaPago.SIN_COBRO.value, "Sin Cobro", Icons.Default.AllInbox)
                        ).forEach { (value, label, icon) ->
                            val isSel = metodoPago == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) TurboGreen.copy(alpha = 0.2f) else TurboDarkCard, RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isSel) TurboGreen else Color(0xFF444444), RoundedCornerShape(10.dp))
                                    .clickable {
                                        metodoPago = value
                                        requiereCobro = value != FormaPago.SIN_COBRO.value
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(icon, contentDescription = null, tint = if (isSel) TurboGreen else TurboGray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label, color = if (isSel) TurboWhite else TurboGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "INDICACIONES Y NOTAS",
                        color = TurboYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = indicacionesRepartidor,
                        onValueChange = { indicacionesRepartidor = it },
                        label = { Text("Indicaciones al Repartidor") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = TurboOrange) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notasInternas,
                        onValueChange = { notasInternas = it },
                        label = { Text("Notas Internas") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = errorMessage ?: "", color = Color(0xFFEF4444), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (destinatarioNombre.isBlank()) {
                                errorMessage = "El nombre del destinatario no puede estar vacío."
                                return@Button
                            }
                            if (destinoDireccion.isBlank()) {
                                errorMessage = "La dirección de entrega no puede estar vacía."
                                return@Button
                            }

                            val parsedMonto = if (requiereCobro) (montoCobrar.toDoubleOrNull() ?: 0.0) else 0.0

                            val updated = envio.copy(
                                destinatarioNombre = destinatarioNombre.trim(),
                                destinatarioTelefono = destinatarioTelefono.trim(),
                                clienteNombre = destinatarioNombre.trim(),
                                destinoDireccion = destinoDireccion.trim(),
                                destinoReferencias = destinoReferencias.ifBlank { null },
                                requiereCobro = requiereCobro,
                                montoCobrar = parsedMonto,
                                metodoPago = metodoPago,
                                formaPago = metodoPago,
                                indicacionesRepartidor = indicacionesRepartidor.ifBlank { null },
                                notasInternas = notasInternas.ifBlank { null },
                                notas = indicacionesRepartidor.ifBlank { null },
                                updatedAt = System.currentTimeMillis()
                            )
                            onSaveUpdate(updated)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(60.dp).navigationBarsPadding())
                }
            }
        }
    }
}
