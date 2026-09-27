package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseManager
import com.example.data.local.AppDatabase
import com.example.data.model.Envio
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import com.example.data.repository.DatabaseSeeder
import com.example.data.repository.EnvioRepository
import com.example.data.repository.RepartidorRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val firebaseManager = FirebaseManager.getInstance(application)

    val userRepo = UserRepository(database, firebaseManager)
    val repartidorRepo = RepartidorRepository(database, firebaseManager)
    val envioRepo = EnvioRepository(database, firebaseManager)
    val authRepo = AuthRepository(database, firebaseManager)

    val authState: StateFlow<AuthState> = authRepo.authState
    val isFirebaseConnected: StateFlow<Boolean> = firebaseManager.isFirebaseConnected
    val firebaseStatusMessage: StateFlow<String> = firebaseManager.firebaseStatusMessage
    val lastRealtimeSync: StateFlow<Long> = firebaseManager.lastRealtimeSync

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Navigation and screen management
    // Screens: WELCOME, ROLE_SELECTION, REGISTER, LOGIN, PENDING, REJECTED, DASHBOARD
    private val _currentScreen = MutableStateFlow("WELCOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedRole = MutableStateFlow("proveedor")
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    val pendingUsers: StateFlow<List<User>> = userRepo.pendingUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = userRepo.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEnvios: StateFlow<List<Envio>> = envioRepo.allEnvios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRepartidores: StateFlow<List<Repartidor>> = repartidorRepo.allRepartidores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countPendientes: StateFlow<Int> = envioRepo.countPendientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val countActivos: StateFlow<Int> = envioRepo.countActivos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val countEntregados: StateFlow<Int> = envioRepo.countEntregados
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val countDisponibles: StateFlow<Int> = repartidorRepo.disponiblesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val countOcupados: StateFlow<Int> = repartidorRepo.ocupadosCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current driver profile if logged in as repartidor
    val currentRepartidor: StateFlow<Repartidor?> = authState.flatMapLatest { state ->
        if (state is AuthState.Authenticated && state.user.role == "repartidor") {
            repartidorRepo.observeRepartidorByUserId(state.user.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current driver's active delivery
    val currentRepartidorActiveEnvio: StateFlow<Envio?> = currentRepartidor.flatMapLatest { rep ->
        if (rep != null) {
            envioRepo.getActiveEnvioForRepartidor(rep.id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedEnvioId = MutableStateFlow<String?>(null)
    val selectedEnvioId: StateFlow<String?> = _selectedEnvioId.asStateFlow()

    val selectedEnvio: StateFlow<Envio?> = _selectedEnvioId.flatMapLatest { id ->
        if (id != null) {
            envioRepo.getEnvioById(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow("TODOS")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedIfNeeded(database)
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun openEnvioDetalle(envioId: String) {
        _selectedEnvioId.value = envioId
        _currentScreen.value = "DETALLE_ENVIO"
    }

    fun openEditarEnvio(envioId: String) {
        _selectedEnvioId.value = envioId
        _currentScreen.value = "EDITAR_ENVIO"
    }

    fun openNuevoEnvio() {
        _currentScreen.value = "NUEVO_ENVIO"
    }

    fun openEnviosHistorial() {
        _currentScreen.value = "ENVIOS_HISTORIAL"
    }

    fun refreshRealtimeEnvios() {
        firebaseManager.fetchEnviosFromFirestoreNow()
        emitToast("Sincronización en tiempo real solicitada a Firestore")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }

    fun selectRole(role: String) {
        _selectedRole.value = role
        _currentScreen.value = "REGISTER"
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = authRepo.login(email, pass)
            result.onSuccess { user ->
                emitToast("¡Bienvenido ${user.fullName}!")
                when {
                    user.rechazado -> _currentScreen.value = "REJECTED"
                    !user.aprobadoPorAdmin && user.role != "admin" -> _currentScreen.value = "PENDING"
                    else -> {
                        _currentScreen.value = "DASHBOARD"
                        onSuccess()
                    }
                }
            }.onFailure { err ->
                emitToast(err.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun loginWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authRepo.loginWithGoogle(activity)
            result.onSuccess { user ->
                emitToast("Autenticado con Google: ${user.fullName}")
                when {
                    user.rechazado -> _currentScreen.value = "REJECTED"
                    !user.aprobadoPorAdmin && user.role != "admin" -> _currentScreen.value = "PENDING"
                    else -> _currentScreen.value = "DASHBOARD"
                }
            }.onFailure { err ->
                emitToast("Google Sign-In: ${err.message ?: "Cancelado o no configurado"}")
            }
        }
    }

    fun register(
        user: User,
        repartidor: Repartidor? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepo.registerUser(user, repartidor)
            result.onSuccess {
                emitToast("Cuenta creada exitosamente en Firestore. Esperando aprobación.")
                _currentScreen.value = "PENDING"
                onSuccess()
            }.onFailure { err ->
                emitToast(err.message ?: "Error en el registro")
            }
        }
    }

    fun approveUser(userId: String) {
        viewModelScope.launch {
            userRepo.approveUser(userId)
            authRepo.refreshCurrentSession()
            emitToast("Usuario aprobado en Firestore y sistema")
        }
    }

    fun rejectUser(userId: String) {
        viewModelScope.launch {
            userRepo.rejectUser(userId)
            authRepo.refreshCurrentSession()
            emitToast("Usuario rechazado en Firestore")
        }
    }

    fun updateRepartidorEstado(nuevoEstado: String) {
        val auth = authState.value
        if (auth is AuthState.Authenticated) {
            viewModelScope.launch {
                repartidorRepo.updateEstado(auth.user.id, nuevoEstado)
                emitToast("Estado cambiado a: $nuevoEstado")
            }
        }
    }

    fun createEnvio(envio: Envio, onSuccess: (Envio) -> Unit = {}) {
        viewModelScope.launch {
            val created = envioRepo.createEnvio(envio)
            emitToast("Envío ${created.numeroSeguimiento} registrado exitosamente")
            onSuccess(created)
        }
    }

    fun updateEnvio(envio: Envio, onSuccess: () -> Unit = {}) {
        val auth = authState.value
        val userName = if (auth is AuthState.Authenticated) auth.user.fullName else "Proveedor"
        viewModelScope.launch {
            envioRepo.updateEnvio(envio, userName)
            emitToast("Envío ${envio.numeroSeguimiento} actualizado")
            onSuccess()
        }
    }

    fun cancelEnvio(envioId: String, motivo: String, onSuccess: () -> Unit = {}) {
        val auth = authState.value
        val userName = if (auth is AuthState.Authenticated) auth.user.fullName else "Usuario"
        viewModelScope.launch {
            envioRepo.cancelEnvio(envioId, motivo, userName)
            emitToast("Envío cancelado")
            onSuccess()
        }
    }

    fun updateEnvioEstado(envioId: String, nuevoEstado: String) {
        val auth = authState.value
        val userName = if (auth is AuthState.Authenticated) auth.user.fullName else "Operador"
        viewModelScope.launch {
            envioRepo.updateEstado(envioId, nuevoEstado, actorNombre = userName)
            emitToast("Estado actualizado a: $nuevoEstado")
        }
    }

    fun assignRepartidor(envioId: String, repartidorId: String, repartidorNombre: String) {
        viewModelScope.launch {
            envioRepo.assignRepartidor(envioId, repartidorId, repartidorNombre)
            emitToast("Repartidor $repartidorNombre asignado en Firestore")
        }
    }

    fun logout() {
        authRepo.logout()
        _currentScreen.value = "WELCOME"
        emitToast("Sesión cerrada")
    }

    fun switchDemoUser(email: String) {
        viewModelScope.launch {
            authRepo.switchDemoAccount(email)
            val auth = authRepo.authState.value
            when (auth) {
                is AuthState.Authenticated -> {
                    _currentScreen.value = "DASHBOARD"
                    emitToast("Cambiado a: ${auth.user.fullName} (${auth.user.role})")
                }
                is AuthState.PendingApproval -> {
                    _currentScreen.value = "PENDING"
                    emitToast("Cambiado a: ${auth.user.fullName} (Pendiente)")
                }
                is AuthState.Rejected -> {
                    _currentScreen.value = "REJECTED"
                    emitToast("Cambiado a: ${auth.user.fullName} (Rechazado)")
                }
                else -> {
                    _currentScreen.value = "LOGIN"
                }
            }
        }
    }

    fun syncAllWithFirestore() {
        firebaseManager.syncAllLocalDataToFirestore()
        emitToast("Sincronización con Firestore iniciada")
    }

    fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }
}
