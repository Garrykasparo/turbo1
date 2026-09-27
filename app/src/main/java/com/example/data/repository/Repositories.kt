package com.example.data.repository

import android.app.Activity
import com.example.data.firebase.FirebaseManager
import com.example.data.local.AppDatabase
import com.example.data.model.Envio
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UserRepository(
    private val database: AppDatabase,
    private val firebaseManager: FirebaseManager? = null
) {
    val allUsers: Flow<List<User>> = database.userDao().getAllUsers()
    val pendingUsers: Flow<List<User>> = database.userDao().getPendingUsers()

    suspend fun getUserById(id: String): User? = database.userDao().getUserById(id)
    suspend fun getUserByEmail(email: String): User? = database.userDao().getUserByEmail(email)
    fun observeUserByEmail(email: String): Flow<User?> = database.userDao().observeUserByEmail(email)

    suspend fun insertUser(user: User) {
        database.userDao().insertUser(user)
        firebaseManager?.saveUser(user)
    }

    suspend fun updateUser(user: User) {
        database.userDao().updateUser(user)
        firebaseManager?.saveUser(user)
    }

    suspend fun approveUser(id: String) {
        database.userDao().approveUser(id)
        firebaseManager?.approveUser(id)
    }

    suspend fun rejectUser(id: String) {
        database.userDao().rejectUser(id)
        firebaseManager?.rejectUser(id)
    }
}

class RepartidorRepository(
    private val database: AppDatabase,
    private val firebaseManager: FirebaseManager? = null
) {
    val allRepartidores: Flow<List<Repartidor>> = database.repartidorDao().getAllRepartidores()
    val disponiblesCount: Flow<Int> = database.repartidorDao().countDisponibles()
    val ocupadosCount: Flow<Int> = database.repartidorDao().countOcupados()

    suspend fun getRepartidorByUserId(userId: String): Repartidor? =
        database.repartidorDao().getRepartidorByUserId(userId)

    fun observeRepartidorByUserId(userId: String): Flow<Repartidor?> =
        database.repartidorDao().observeRepartidorByUserId(userId)

    suspend fun insertRepartidor(repartidor: Repartidor) {
        database.repartidorDao().insertRepartidor(repartidor)
        firebaseManager?.saveRepartidor(repartidor)
    }

    suspend fun updateEstado(userId: String, nuevoEstado: String) {
        database.repartidorDao().updateEstado(userId, nuevoEstado)
        firebaseManager?.updateRepartidorEstado(userId, nuevoEstado)
    }
}

class EnvioRepository(
    private val database: AppDatabase,
    private val firebaseManager: FirebaseManager? = null
) {
    val allEnvios: Flow<List<Envio>> = database.envioDao().getAllEnvios()
    val pendientes: Flow<List<Envio>> = database.envioDao().getPendientes()
    val activos: Flow<List<Envio>> = database.envioDao().getActivos()
    val countPendientes: Flow<Int> = database.envioDao().countPendientes()
    val countActivos: Flow<Int> = database.envioDao().countActivos()
    val countEntregados: Flow<Int> = database.envioDao().countEntregados()

    fun getEnviosByProveedor(proveedorId: String): Flow<List<Envio>> =
        database.envioDao().getEnviosByProveedor(proveedorId)

    fun getEnviosByRepartidor(repartidorId: String): Flow<List<Envio>> =
        database.envioDao().getEnviosByRepartidor(repartidorId)

    fun getActiveEnvioForRepartidor(repartidorId: String): Flow<Envio?> =
        database.envioDao().getActiveEnvioForRepartidor(repartidorId)

    fun getEnvioById(id: String): Flow<Envio?> =
        database.envioDao().getEnvioByIdFlow(id)

    suspend fun getEnvioSnapshot(id: String): Envio? =
        database.envioDao().getEnvioById(id)

    fun searchEnvios(query: String): Flow<List<Envio>> =
        database.envioDao().searchEnvios(query)

    suspend fun createEnvio(envio: Envio): Envio {
        // Ensure tracking number uniqueness
        var tracking = envio.numeroSeguimiento
        while (database.envioDao().getEnvioByTracking(tracking) != null) {
            tracking = Envio.generateTrackingNumber()
        }

        val initialHistorial = com.example.data.model.HistorialItem(
            estado = "pendiente",
            fecha = System.currentTimeMillis(),
            usuarioId = envio.creadoPorUserId,
            usuarioNombre = envio.proveedorNombre.ifBlank { "Proveedor" },
            descripcion = "Pedido creado con servicio ${envio.tipoServicio.uppercase()}"
        )

        val preparedEnvio = envio.copy(
            numeroSeguimiento = tracking,
            clienteNombre = envio.destinatarioNombre.ifBlank { envio.clienteNombre },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ).withHistorialEntry(initialHistorial)

        database.envioDao().insertEnvio(preparedEnvio)
        firebaseManager?.saveEnvio(preparedEnvio)
        return preparedEnvio
    }

    suspend fun updateEnvio(envio: Envio, editorNombre: String = "Proveedor"): Envio {
        val historialEntry = com.example.data.model.HistorialItem(
            estado = envio.estado,
            fecha = System.currentTimeMillis(),
            usuarioId = envio.creadoPorUserId,
            usuarioNombre = editorNombre,
            descripcion = "Datos del pedido actualizados"
        )
        val updatedEnvio = envio.withHistorialEntry(historialEntry)
        database.envioDao().updateEnvio(updatedEnvio)
        firebaseManager?.saveEnvio(updatedEnvio)
        return updatedEnvio
    }

    suspend fun cancelEnvio(id: String, motivo: String, canceladoPor: String) {
        val existing = database.envioDao().getEnvioById(id)
        val now = System.currentTimeMillis()
        val historialEntry = com.example.data.model.HistorialItem(
            estado = "cancelado",
            fecha = now,
            usuarioId = canceladoPor,
            usuarioNombre = canceladoPor,
            descripcion = "Cancelado: $motivo"
        )
        val updatedEnvio = existing?.copy(
            estado = "cancelado",
            motivoCancelacion = motivo,
            canceladoPor = canceladoPor,
            fechaCancelacion = now,
            updatedAt = now
        )?.withHistorialEntry(historialEntry)

        if (updatedEnvio != null) {
            database.envioDao().updateEnvio(updatedEnvio)
            firebaseManager?.cancelEnvioInFirestore(
                envioId = id,
                motivo = motivo,
                canceladoPor = canceladoPor,
                fecha = now,
                historialJson = updatedEnvio.historialJson
            )
        }
    }

    suspend fun updateEstado(
        id: String,
        nuevoEstado: String,
        actorNombre: String? = null,
        descripcionNota: String? = null
    ) {
        val existing = database.envioDao().getEnvioById(id)
        val now = System.currentTimeMillis()
        val desc = descripcionNota ?: when (nuevoEstado) {
            "asignado" -> "Repartidor asignado"
            "aceptado" -> "Repartidor aceptó el pedido"
            "en_recoleccion", "recogiendo" -> "Repartidor en punto de recolección"
            "recolectado" -> "Paquete recolectado por el repartidor"
            "en_camino" -> "Repartidor en camino al destino"
            "entregado" -> "Pedido entregado con éxito al destinatario"
            "no_entregado" -> "Intento de entrega fallido"
            "cancelado" -> "Pedido cancelado"
            else -> "Estado cambiado a $nuevoEstado"
        }

        val entry = com.example.data.model.HistorialItem(
            estado = nuevoEstado,
            fecha = now,
            usuarioId = actorNombre ?: "Sistema",
            usuarioNombre = actorNombre ?: "Sistema",
            descripcion = desc
        )

        var updated = existing?.copy(
            estado = nuevoEstado,
            updatedAt = now
        )

        if (nuevoEstado == "en_recoleccion" || nuevoEstado == "recogiendo" || nuevoEstado == "recolectado") {
            updated = updated?.copy(fechaRecoleccion = now)
        } else if (nuevoEstado == "entregado") {
            updated = updated?.copy(fechaEntrega = now)
        }

        val finalized = updated?.withHistorialEntry(entry)
        if (finalized != null) {
            database.envioDao().updateEnvio(finalized)
            firebaseManager?.updateEnvioEstado(id, nuevoEstado, finalized.historialJson)
        } else {
            database.envioDao().updateEstado(id, nuevoEstado)
            firebaseManager?.updateEnvioEstado(id, nuevoEstado)
        }
    }

    suspend fun assignRepartidor(
        id: String,
        repartidorId: String,
        repartidorNombre: String,
        asignadorNombre: String = "Coordinador"
    ) {
        val existing = database.envioDao().getEnvioById(id)
        val now = System.currentTimeMillis()
        val entry = com.example.data.model.HistorialItem(
            estado = "asignado",
            fecha = now,
            usuarioId = repartidorId,
            usuarioNombre = asignadorNombre,
            descripcion = "Asignado a repartidor: $repartidorNombre"
        )
        val updated = existing?.copy(
            repartidorId = repartidorId,
            repartidorNombre = repartidorNombre,
            estado = "asignado",
            fechaAsignacion = now,
            updatedAt = now
        )?.withHistorialEntry(entry)

        if (updated != null) {
            database.envioDao().updateEnvio(updated)
            firebaseManager?.saveEnvio(updated)
        } else {
            database.envioDao().assignRepartidor(id, repartidorId, repartidorNombre)
            firebaseManager?.assignRepartidorToEnvio(id, repartidorId, repartidorNombre)
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class PendingApproval(val user: User) : AuthState()
    data class Rejected(val user: User) : AuthState()
}

class AuthRepository(
    private val database: AppDatabase,
    private val firebaseManager: FirebaseManager? = null
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    suspend fun login(email: String, pass: String): Result<User> {
        val cleanEmail = email.trim().lowercase()

        // 1. Try Firebase Auth in cloud if available
        val firebaseResult = firebaseManager?.signInWithEmail(cleanEmail, pass)
        // If Firebase Auth succeeded or if offline, authenticate with local profile
        var user = database.userDao().getUserByEmail(cleanEmail)

        if (user == null) {
            // If user logged in through Firebase Auth but document is not in Room yet
            if (firebaseResult?.isSuccess == true) {
                val fbUser = firebaseResult.getOrNull()
                user = User(
                    id = fbUser?.uid ?: UUID.randomUUID().toString(),
                    uid = fbUser?.uid ?: UUID.randomUUID().toString(),
                    email = cleanEmail,
                    fullName = fbUser?.displayName ?: "Usuario",
                    telefono = fbUser?.phoneNumber ?: "",
                    role = UserRole.PROVEEDOR.value,
                    aprobadoPorAdmin = false,
                    rechazado = false,
                    activo = true
                )
                database.userDao().insertUser(user)
                firebaseManager.saveUser(user)
            } else {
                return Result.failure(Exception("No existe ninguna cuenta registrada con este correo."))
            }
        } else {
            if (user.passwordHash != pass.trim()) {
                return Result.failure(Exception("Contraseña incorrecta. Inténtalo nuevamente."))
            }
        }

        evaluateUserStatus(user)
        return Result.success(user)
    }

    suspend fun registerUser(
        user: User,
        repartidorData: Repartidor? = null
    ): Result<User> {
        val cleanEmail = user.email.trim().lowercase()

        // Prevent duplicate accounts
        val existing = database.userDao().getUserByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(Exception("Ya existe una cuenta con este correo electrónico."))
        }

        // Try creating account in Firebase Auth
        firebaseManager?.createUserWithEmail(cleanEmail, user.passwordHash)

        // Save in Room database
        database.userDao().insertUser(user)
        if (repartidorData != null) {
            database.repartidorDao().insertRepartidor(repartidorData)
        }

        // Sync to Firestore
        firebaseManager?.saveUser(user)
        if (repartidorData != null) {
            firebaseManager?.saveRepartidor(repartidorData)
        }

        evaluateUserStatus(user)
        return Result.success(user)
    }

    suspend fun loginWithGoogle(activity: Activity, webClientId: String? = null): Result<User> {
        val manager = firebaseManager ?: return Result.failure(Exception("Firebase no está activo"))
        val googleResult = manager.signInWithGoogle(activity, webClientId)
        if (googleResult.isFailure) {
            return Result.failure(googleResult.exceptionOrNull() ?: Exception("Error en Google Sign-In"))
        }

        val fbUser = googleResult.getOrNull() ?: return Result.failure(Exception("Usuario de Google nulo"))
        val email = fbUser.email?.trim()?.lowercase() ?: "google_user@turbomandados.mx"

        var user = database.userDao().getUserByEmail(email)
        if (user == null) {
            user = User(
                id = fbUser.uid,
                uid = fbUser.uid,
                email = email,
                fullName = fbUser.displayName ?: "Usuario Google",
                telefono = fbUser.phoneNumber ?: "",
                role = UserRole.PROVEEDOR.value,
                fotoPerfil = fbUser.photoUrl?.toString(),
                aprobadoPorAdmin = false,
                rechazado = false,
                activo = true
            )
            database.userDao().insertUser(user)
            manager.saveUser(user)
        }

        evaluateUserStatus(user)
        return Result.success(user)
    }

    fun evaluateUserStatus(user: User) {
        when {
            user.rechazado -> {
                _authState.value = AuthState.Rejected(user)
            }
            !user.aprobadoPorAdmin && user.role != "admin" -> {
                _authState.value = AuthState.PendingApproval(user)
            }
            else -> {
                _authState.value = AuthState.Authenticated(user)
            }
        }
    }

    suspend fun refreshCurrentSession() {
        val current = _authState.value
        val userEmail = when (current) {
            is AuthState.Authenticated -> current.user.email
            is AuthState.PendingApproval -> current.user.email
            is AuthState.Rejected -> current.user.email
            else -> null
        }
        if (userEmail != null) {
            val freshUser = database.userDao().getUserByEmail(userEmail)
            if (freshUser != null) {
                evaluateUserStatus(freshUser)
            } else {
                logout()
            }
        }
    }

    fun logout() {
        firebaseManager?.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    suspend fun switchDemoAccount(email: String) {
        val user = database.userDao().getUserByEmail(email)
        if (user != null) {
            evaluateUserStatus(user)
        }
    }
}
