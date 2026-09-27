package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.data.model.VehiculoTipo
import com.example.ui.theme.TurboDarkBg
import com.example.ui.theme.TurboDarkCard
import com.example.ui.theme.TurboGray
import com.example.ui.theme.TurboOrange
import com.example.ui.theme.TurboRed
import com.example.ui.theme.TurboWhite
import java.util.UUID

@Composable
fun RegisterScreen(
    role: String,
    onRegisterSubmit: (User, Repartidor?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Proveedor specific
    var nombreNegocio by remember { mutableStateOf("") }

    // Repartidor specific
    var vehiculoTipo by remember { mutableStateOf(VehiculoTipo.MOTO.value) }
    var vehiculoPlaca by remember { mutableStateOf("") }
    var vehiculoMarca by remember { mutableStateOf("") }
    var vehiculoModelo by remember { mutableStateOf("") }
    var vehiculoColor by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val roleTitle = when (role) {
        "proveedor" -> "Registro de Proveedor"
        "repartidor" -> "Registro de Repartidor"
        "coordinador" -> "Registro de Coordinador"
        "asistente" -> "Registro de Asistente"
        else -> "Registro de Usuario"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TurboDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Back & Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("register_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TurboWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Paso 2 de 2",
                color = TurboGray,
                fontSize = 14.sp
            )
        }

        Text(
            text = roleTitle,
            color = TurboWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Ingresa tus datos para crear tu cuenta en Turbo Mandados.",
            color = TurboGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TurboRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, TurboRed, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = TurboRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Common Fields
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TurboOrange) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_fullname_input"),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TurboOrange) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input"),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono de Contacto") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TurboOrange) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_phone_input"),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Proveedor specific: Nombre del negocio
        if (role == "proveedor") {
            OutlinedTextField(
                value = nombreNegocio,
                onValueChange = { nombreNegocio = it },
                label = { Text("Nombre del Negocio (ej. Farmacia, Restaurante)") },
                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TurboOrange) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_business_input"),
                colors = customTextFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Repartidor specific fields
        if (role == "repartidor") {
            Text(
                text = "Datos del Vehículo",
                color = TurboOrange,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Vehicle type selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    VehiculoTipo.MOTO to "Moto",
                    VehiculoTipo.BICICLETA to "Bici",
                    VehiculoTipo.AUTO to "Auto",
                    VehiculoTipo.CAMIONETA to "Camioneta"
                ).forEach { (tipo, label) ->
                    val isSelected = vehiculoTipo == tipo.value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) TurboOrange else TurboDarkCard,
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, if (isSelected) TurboOrange else Color(0xFF444444), RoundedCornerShape(10.dp))
                            .clickable { vehiculoTipo = tipo.value }
                            .padding(vertical = 10.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = vehiculoPlaca,
                    onValueChange = { vehiculoPlaca = it },
                    label = { Text("Placa") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("register_plate_input"),
                    colors = customTextFieldColors()
                )
                OutlinedTextField(
                    value = vehiculoMarca,
                    onValueChange = { vehiculoMarca = it },
                    label = { Text("Marca") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("register_brand_input"),
                    colors = customTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = vehiculoModelo,
                    onValueChange = { vehiculoModelo = it },
                    label = { Text("Modelo / Año") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = customTextFieldColors()
                )
                OutlinedTextField(
                    value = vehiculoColor,
                    onValueChange = { vehiculoColor = it },
                    label = { Text("Color") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = customTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Passwords
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TurboOrange) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input"),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TurboOrange) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_confirm_password_input"),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                if (fullName.isBlank() || email.isBlank() || telefono.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor completa todos los campos obligatorios."
                    return@Button
                }
                if (!email.contains("@") || !email.contains(".")) {
                    errorMessage = "Ingresa un correo electrónico válido."
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Las contraseñas no coinciden."
                    return@Button
                }
                if (role == "proveedor" && nombreNegocio.isBlank()) {
                    errorMessage = "Por favor ingresa el nombre de tu negocio."
                    return@Button
                }

                val userId = UUID.randomUUID().toString()
                val user = User(
                    id = userId,
                    uid = UUID.randomUUID().toString(),
                    email = email.trim().lowercase(),
                    fullName = fullName.trim(),
                    telefono = telefono.trim(),
                    role = role,
                    passwordHash = password,
                    nombreNegocio = if (role == "proveedor") nombreNegocio.trim() else null,
                    aprobadoPorAdmin = false,
                    rechazado = false,
                    activo = true
                )

                val repartidorData = if (role == "repartidor") {
                    Repartidor(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        visualCode = "TM-${(1000..9999).random()}",
                        nombreCompleto = fullName.trim(),
                        telefono = telefono.trim(),
                        vehiculoTipo = vehiculoTipo,
                        vehiculoPlaca = vehiculoPlaca.trim(),
                        vehiculoMarca = vehiculoMarca.trim(),
                        vehiculoModelo = vehiculoModelo.trim(),
                        vehiculoColor = vehiculoColor.trim()
                    )
                } else null

                errorMessage = null
                onRegisterSubmit(user, repartidorData)
            },
            colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("register_submit_button")
        ) {
            Text(
                text = "Crear Cuenta",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TurboOrange,
    unfocusedBorderColor = Color(0xFF444444),
    focusedLabelColor = TurboOrange,
    unfocusedLabelColor = TurboGray,
    focusedTextColor = TurboWhite,
    unfocusedTextColor = TurboWhite,
    cursorColor = TurboOrange
)
