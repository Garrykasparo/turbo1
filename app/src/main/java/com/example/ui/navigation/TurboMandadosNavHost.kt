package com.example.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.repository.AuthState
import com.example.ui.MainViewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegistrationPendingScreen
import com.example.ui.screens.RegistrationRejectedScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.coordinador.CoordinadorDashboardScreen
import com.example.ui.screens.envio.EnvioDetalleScreen
import com.example.ui.screens.proveedor.EditarEnvioScreen
import com.example.ui.screens.proveedor.NuevoEnvioScreen
import com.example.ui.screens.proveedor.ProveedorDashboardScreen
import com.example.ui.screens.repartidor.RepartidorDashboardScreen
import com.example.ui.theme.TurboDarkBg
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TurboMandadosNavHost(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()

    val pendingUsers by viewModel.pendingUsers.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allEnvios by viewModel.allEnvios.collectAsState()
    val allRepartidores by viewModel.allRepartidores.collectAsState()

    val countPendientes by viewModel.countPendientes.collectAsState()
    val countActivos by viewModel.countActivos.collectAsState()
    val countEntregados by viewModel.countEntregados.collectAsState()
    val countDisponibles by viewModel.countDisponibles.collectAsState()
    val countOcupados by viewModel.countOcupados.collectAsState()

    val currentRepartidor by viewModel.currentRepartidor.collectAsState()
    val currentActiveEnvio by viewModel.currentRepartidorActiveEnvio.collectAsState()
    val selectedEnvio by viewModel.selectedEnvio.collectAsState()

    val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsState()
    val firebaseStatusMessage by viewModel.firebaseStatusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TurboDarkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        val currentUser = (authState as? AuthState.Authenticated)?.user

        when (currentScreen) {
            "WELCOME" -> {
                WelcomeScreen(
                    onStartClick = { viewModel.navigateTo("ROLE_SELECTION") },
                    onLoginClick = { viewModel.navigateTo("LOGIN") },
                    modifier = screenModifier
                )
            }

            "ROLE_SELECTION" -> {
                RoleSelectionScreen(
                    onRoleSelected = { role ->
                        viewModel.selectRole(role)
                    },
                    onBack = { viewModel.navigateTo("WELCOME") },
                    modifier = screenModifier
                )
            }

            "REGISTER" -> {
                RegisterScreen(
                    role = selectedRole,
                    onRegisterSubmit = { user, repartidor ->
                        viewModel.register(user, repartidor)
                    },
                    onBack = { viewModel.navigateTo("ROLE_SELECTION") },
                    modifier = screenModifier
                )
            }

            "LOGIN" -> {
                LoginScreen(
                    onLoginSubmit = { email, pass ->
                        viewModel.login(email, pass)
                    },
                    onGoogleSignInClick = {
                        if (activity != null) {
                            viewModel.loginWithGoogle(activity)
                        } else {
                            viewModel.emitToast("Google Sign-In requiere actividad activa")
                        }
                    },
                    onNavigateToRegister = { viewModel.navigateTo("ROLE_SELECTION") },
                    onBack = { viewModel.navigateTo("WELCOME") },
                    modifier = screenModifier
                )
            }

            "PENDING" -> {
                val user = when (val state = authState) {
                    is AuthState.PendingApproval -> state.user
                    is AuthState.Authenticated -> state.user
                    else -> null
                }
                RegistrationPendingScreen(
                    user = user,
                    onLogout = { viewModel.logout() },
                    onRefreshStatus = {
                        viewModel.login(user?.email ?: "", user?.passwordHash ?: "123456")
                    },
                    modifier = screenModifier
                )
            }

            "REJECTED" -> {
                val user = when (val state = authState) {
                    is AuthState.Rejected -> state.user
                    else -> null
                }
                RegistrationRejectedScreen(
                    user = user,
                    onLogout = { viewModel.logout() },
                    modifier = screenModifier
                )
            }

            "NUEVO_ENVIO" -> {
                NuevoEnvioScreen(
                    currentUser = currentUser,
                    onBack = { viewModel.navigateTo("DASHBOARD") },
                    onCreateEnvio = { envio, callback ->
                        viewModel.createEnvio(envio) { created ->
                            callback(created)
                        }
                    },
                    onViewDetail = { id ->
                        viewModel.openEnvioDetalle(id)
                    },
                    modifier = screenModifier
                )
            }

            "DETALLE_ENVIO" -> {
                EnvioDetalleScreen(
                    envio = selectedEnvio,
                    currentUser = currentUser,
                    repartidores = allRepartidores,
                    onBack = { viewModel.navigateTo("DASHBOARD") },
                    onEditEnvio = { id -> viewModel.openEditarEnvio(id) },
                    onCancelEnvio = { id, motivo -> viewModel.cancelEnvio(id, motivo) },
                    onAdvanceEstado = { id, next -> viewModel.updateEnvioEstado(id, next) },
                    onAssignRepartidor = { id, repId, repName -> viewModel.assignRepartidor(id, repId, repName) },
                    onShowToast = { msg -> viewModel.emitToast(msg) },
                    modifier = screenModifier
                )
            }

            "EDITAR_ENVIO" -> {
                EditarEnvioScreen(
                    envio = selectedEnvio,
                    onBack = {
                        if (viewModel.selectedEnvioId.value != null) {
                            viewModel.navigateTo("DETALLE_ENVIO")
                        } else {
                            viewModel.navigateTo("DASHBOARD")
                        }
                    },
                    onSaveUpdate = { updated ->
                        viewModel.updateEnvio(updated) {
                            viewModel.openEnvioDetalle(updated.id)
                        }
                    },
                    modifier = screenModifier
                )
            }

            "DASHBOARD" -> {
                // Role-based protected screen routing
                when (val auth = authState) {
                    is AuthState.Authenticated -> {
                        when (auth.user.role.lowercase()) {
                            "admin" -> {
                                AdminDashboardScreen(
                                    currentUser = auth.user,
                                    pendingUsers = pendingUsers,
                                    allUsers = allUsers,
                                    allEnvios = allEnvios,
                                    allRepartidores = allRepartidores,
                                    countPendientes = countPendientes,
                                    countActivos = countActivos,
                                    countEntregados = countEntregados,
                                    countDisponibles = countDisponibles,
                                    countOcupados = countOcupados,
                                    onApproveUser = { userId -> viewModel.approveUser(userId) },
                                    onRejectUser = { userId -> viewModel.rejectUser(userId) },
                                    onAdvanceEnvio = { id, next -> viewModel.updateEnvioEstado(id, next) },
                                    onLogout = { viewModel.logout() },
                                    onSwitchUser = { email -> viewModel.switchDemoUser(email) },
                                    onOpenDetail = { id -> viewModel.openEnvioDetalle(id) },
                                    modifier = screenModifier
                                )
                            }

                            "proveedor" -> {
                                val provEnvios = allEnvios.filter { it.proveedorId == auth.user.id }
                                ProveedorDashboardScreen(
                                    currentUser = auth.user,
                                    envios = provEnvios,
                                    onNavigateToNuevoEnvio = { viewModel.openNuevoEnvio() },
                                    onOpenDetail = { id -> viewModel.openEnvioDetalle(id) },
                                    onEditEnvio = { id -> viewModel.openEditarEnvio(id) },
                                    onCancelEnvio = { id, motivo -> viewModel.cancelEnvio(id, motivo) },
                                    onCreateEnvio = { envio -> viewModel.createEnvio(envio) },
                                    onLogout = { viewModel.logout() },
                                    onSwitchUser = { email -> viewModel.switchDemoUser(email) },
                                    isFirebaseConnected = isFirebaseConnected,
                                    firebaseStatusMessage = firebaseStatusMessage,
                                    onSyncClick = { viewModel.syncAllWithFirestore() },
                                    modifier = screenModifier
                                )
                            }

                            "repartidor" -> {
                                RepartidorDashboardScreen(
                                    currentUser = auth.user,
                                    repartidor = currentRepartidor,
                                    activeEnvio = currentActiveEnvio,
                                    onStatusChange = { nuevoEstado ->
                                        viewModel.updateRepartidorEstado(nuevoEstado)
                                    },
                                    onAdvanceEnvio = { id, next ->
                                        viewModel.updateEnvioEstado(id, next)
                                    },
                                    onLogout = { viewModel.logout() },
                                    onSwitchUser = { email -> viewModel.switchDemoUser(email) },
                                    onOpenDetail = { id -> viewModel.openEnvioDetalle(id) },
                                    modifier = screenModifier
                                )
                            }

                            "coordinador", "asistente" -> {
                                CoordinadorDashboardScreen(
                                    currentUser = auth.user,
                                    allEnvios = allEnvios,
                                    allRepartidores = allRepartidores,
                                    countPendientes = countPendientes,
                                    countActivos = countActivos,
                                    countEntregados = countEntregados,
                                    countDisponibles = countDisponibles,
                                    countOcupados = countOcupados,
                                    onAssignRepartidor = { id, repId, repName ->
                                        viewModel.assignRepartidor(id, repId, repName)
                                    },
                                    onAdvanceEnvio = { id, next ->
                                        viewModel.updateEnvioEstado(id, next)
                                    },
                                    onLogout = { viewModel.logout() },
                                    onSwitchUser = { email -> viewModel.switchDemoUser(email) },
                                    onOpenDetail = { id -> viewModel.openEnvioDetalle(id) },
                                    modifier = screenModifier
                                )
                            }

                            else -> {
                                WelcomeScreen(
                                    onStartClick = { viewModel.navigateTo("ROLE_SELECTION") },
                                    onLoginClick = { viewModel.navigateTo("LOGIN") },
                                    modifier = screenModifier
                                )
                            }
                        }
                    }

                    is AuthState.PendingApproval -> {
                        RegistrationPendingScreen(
                            user = auth.user,
                            onLogout = { viewModel.logout() },
                            onRefreshStatus = {
                                viewModel.login(auth.user.email, auth.user.passwordHash)
                            },
                            modifier = screenModifier
                        )
                    }

                    is AuthState.Rejected -> {
                        RegistrationRejectedScreen(
                            user = auth.user,
                            onLogout = { viewModel.logout() },
                            modifier = screenModifier
                        )
                    }

                    else -> {
                        WelcomeScreen(
                            onStartClick = { viewModel.navigateTo("ROLE_SELECTION") },
                            onLoginClick = { viewModel.navigateTo("LOGIN") },
                            modifier = screenModifier
                        )
                    }
                }
            }

            else -> {
                WelcomeScreen(
                    onStartClick = { viewModel.navigateTo("ROLE_SELECTION") },
                    onLoginClick = { viewModel.navigateTo("LOGIN") },
                    modifier = screenModifier
                )
            }
        }
    }
}
