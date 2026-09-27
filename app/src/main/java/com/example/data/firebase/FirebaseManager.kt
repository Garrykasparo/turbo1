package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.data.local.AppDatabase
import com.example.data.model.Envio
import com.example.data.model.Repartidor
import com.example.data.model.User
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseManager private constructor(private val context: Context) {

    private val TAG = "FirebaseManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val database = AppDatabase.getDatabase(context)

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _isFirebaseConnected = MutableStateFlow(false)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()

    private val _firebaseStatusMessage = MutableStateFlow("Iniciando conexión con Firebase...")
    val firebaseStatusMessage: StateFlow<String> = _firebaseStatusMessage.asStateFlow()

    private val _currentFirebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val currentFirebaseUser: StateFlow<FirebaseUser?> = _currentFirebaseUser.asStateFlow()

    private val _lastRealtimeSync = MutableStateFlow(System.currentTimeMillis())
    val lastRealtimeSync: StateFlow<Long> = _lastRealtimeSync.asStateFlow()

    private var usersListener: ListenerRegistration? = null
    private var repartidoresListener: ListenerRegistration? = null
    private var enviosListener: ListenerRegistration? = null

    init {
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }

            if (app != null) {
                firebaseAuth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                _isFirebaseConnected.value = true
                _firebaseStatusMessage.value = "Conectado a Firebase Firestore y Auth en tiempo real"
                _currentFirebaseUser.value = firebaseAuth?.currentUser

                firebaseAuth?.addAuthStateListener { auth ->
                    _currentFirebaseUser.value = auth.currentUser
                }

                // Setup realtime Firestore synchronization with local Room database
                setupRealtimeSync()
            } else {
                _isFirebaseConnected.value = false
                _firebaseStatusMessage.value = "Modo Local / Offline. Para sincronizar en la nube, coloca 'google-services.json'."
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not yet initialized (waiting for google-services.json): ${e.message}")
            _isFirebaseConnected.value = false
            _firebaseStatusMessage.value = "Modo Local Activo. Base de datos Room lista. Para conectar Firestore, agrega google-services.json."
        }
    }

    private fun setupRealtimeSync() {
        val db = firestore ?: return

        // 1. Sync 'users' collection
        try {
            usersListener = db.collection("users").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to users collection: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch {
                        for (doc in snapshot.documents) {
                            try {
                                val user = mapToUser(doc.id, doc.data ?: emptyMap())
                                database.userDao().insertUser(user)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping user from Firestore: ${e.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot start users snapshot listener: ${e.message}")
        }

        // 2. Sync 'repartidores' collection
        try {
            repartidoresListener = db.collection("repartidores").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to repartidores collection: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    scope.launch {
                        for (doc in snapshot.documents) {
                            try {
                                val rep = mapToRepartidor(doc.id, doc.data ?: emptyMap())
                                database.repartidorDao().insertRepartidor(rep)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping repartidor from Firestore: ${e.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot start repartidores snapshot listener: ${e.message}")
        }

        // 3. Sync 'envios' collection
        try {
            enviosListener = db.collection("envios").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to envios collection: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _lastRealtimeSync.value = System.currentTimeMillis()
                    scope.launch {
                        for (doc in snapshot.documents) {
                            try {
                                val envio = mapToEnvio(doc.id, doc.data ?: emptyMap())
                                database.envioDao().insertEnvio(envio)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapping envio from Firestore: ${e.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot start envios snapshot listener: ${e.message}")
        }
    }

    fun fetchEnviosFromFirestoreNow() {
        val db = firestore ?: return
        scope.launch {
            try {
                val snapshot = db.collection("envios").get().awaitResult()
                for (doc in snapshot.documents) {
                    try {
                        val envio = mapToEnvio(doc.id, doc.data ?: emptyMap())
                        database.envioDao().insertEnvio(envio)
                    } catch (_: Exception) {}
                }
                _lastRealtimeSync.value = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.w(TAG, "Manual fetch from Firestore error: ${e.message}")
            }
        }
    }

    // ==========================================
    // AUTHENTICATION: FIREBASE AUTH & GOOGLE
    // ==========================================

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth no disponible en este momento"))
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).awaitResult()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth no disponible en este momento"))
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).awaitResult()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(activity: Activity, webClientId: String? = null): Result<FirebaseUser?> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth no configurado"))
        return try {
            val credentialManager = CredentialManager.create(activity)
            val clientId = webClientId?.ifBlank { null }
                ?: "474877734599-google.apps.googleusercontent.com" // Default placeholder

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(request = request, context = activity)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).awaitResult()
                Result.success(authResult.user)
            } else {
                Result.failure(Exception("Tipo de credencial no soportado"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google Sign In: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}")
        }
    }

    // ==========================================
    // FIRESTORE DATABASE MUTATIONS
    // ==========================================

    suspend fun saveUser(user: User) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to user.id,
                "uid" to user.uid,
                "email" to user.email,
                "fullName" to user.fullName,
                "telefono" to user.telefono,
                "role" to user.role,
                "nombreNegocio" to user.nombreNegocio,
                "direccion" to user.direccion,
                "fotoPerfil" to user.fotoPerfil,
                "aprobadoPorAdmin" to user.aprobadoPorAdmin,
                "rechazado" to user.rechazado,
                "activo" to user.activo,
                "fechaRegistro" to user.fechaRegistro,
                "fechaAprobacion" to user.fechaAprobacion,
                "createdAt" to user.createdAt,
                "updatedAt" to user.updatedAt
            )
            db.collection("users").document(user.id).set(data, SetOptions.merge()).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save user in Firestore: ${e.message}")
        }
    }

    suspend fun approveUser(userId: String) {
        val db = firestore ?: return
        try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "aprobadoPorAdmin" to true,
                "rechazado" to false,
                "fechaAprobacion" to now,
                "updatedAt" to now
            )
            db.collection("users").document(userId).update(updates).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to approve user in Firestore: ${e.message}")
        }
    }

    suspend fun rejectUser(userId: String) {
        val db = firestore ?: return
        try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "aprobadoPorAdmin" to false,
                "rechazado" to true,
                "updatedAt" to now
            )
            db.collection("users").document(userId).update(updates).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to reject user in Firestore: ${e.message}")
        }
    }

    suspend fun saveRepartidor(repartidor: Repartidor) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to repartidor.id,
                "userId" to repartidor.userId,
                "visualCode" to repartidor.visualCode,
                "nombreCompleto" to repartidor.nombreCompleto,
                "telefono" to repartidor.telefono,
                "fotoPerfil" to repartidor.fotoPerfil,
                "vehiculoTipo" to repartidor.vehiculoTipo,
                "vehiculoPlaca" to repartidor.vehiculoPlaca,
                "vehiculoMarca" to repartidor.vehiculoMarca,
                "vehiculoModelo" to repartidor.vehiculoModelo,
                "vehiculoColor" to repartidor.vehiculoColor,
                "estado" to repartidor.estado,
                "ubicacionActualLat" to repartidor.ubicacionActualLat,
                "ubicacionActualLng" to repartidor.ubicacionActualLng,
                "ultimaActualizacionGps" to repartidor.ultimaActualizacionGps,
                "velocidadKmh" to repartidor.velocidadKmh,
                "calificacionPromedio" to repartidor.calificacionPromedio,
                "totalEntregas" to repartidor.totalEntregas,
                "entregasExitosas" to repartidor.entregasExitosas,
                "entregasCanceladas" to repartidor.entregasCanceladas,
                "activo" to repartidor.activo,
                "createdAt" to repartidor.createdAt,
                "updatedAt" to repartidor.updatedAt
            )
            db.collection("repartidores").document(repartidor.id).set(data, SetOptions.merge()).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save repartidor in Firestore: ${e.message}")
        }
    }

    suspend fun updateRepartidorEstado(userId: String, nuevoEstado: String) {
        val db = firestore ?: return
        try {
            val querySnapshot = db.collection("repartidores").whereEqualTo("userId", userId).get().awaitResult()
            val now = System.currentTimeMillis()
            for (doc in querySnapshot.documents) {
                db.collection("repartidores").document(doc.id).update(
                    mapOf(
                        "estado" to nuevoEstado,
                        "updatedAt" to now
                    )
                ).awaitResult()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update repartidor status in Firestore: ${e.message}")
        }
    }

    suspend fun saveEnvio(envio: Envio) {
        val db = firestore ?: return
        try {
            val data = hashMapOf(
                "id" to envio.id,
                "numeroSeguimiento" to envio.numeroSeguimiento,
                "proveedorId" to envio.proveedorId,
                "proveedorNombre" to envio.proveedorNombre,
                "proveedorNegocio" to envio.proveedorNegocio,
                "creadoPorUserId" to envio.creadoPorUserId,
                "creadoPorRol" to envio.creadoPorRol,
                "clienteNombre" to envio.clienteNombre,
                "origenNombre" to envio.origenNombre,
                "origenDireccion" to envio.origenDireccion,
                "origenReferencias" to envio.origenReferencias,
                "origenLat" to envio.origenLat,
                "origenLng" to envio.origenLng,
                "origenTelefono" to envio.origenTelefono,
                "destinoDireccion" to envio.destinoDireccion,
                "destinoReferencias" to envio.destinoReferencias,
                "destinoLat" to envio.destinoLat,
                "destinoLng" to envio.destinoLng,
                "destinatarioNombre" to envio.destinatarioNombre,
                "destinatarioTelefono" to envio.destinatarioTelefono,
                "tipoServicio" to envio.tipoServicio,
                "estado" to envio.estado,
                "repartidorId" to envio.repartidorId,
                "repartidorNombre" to envio.repartidorNombre,
                "fechaServicio" to envio.fechaServicio,
                "horarioTipo" to envio.horarioTipo,
                "franjaHoraria" to envio.franjaHoraria,
                "horaInicio" to envio.horaInicio,
                "horaFin" to envio.horaFin,
                "fechaProgramada" to envio.fechaProgramada,
                "intervaloEntrega" to envio.intervaloEntrega,
                "requiereCobro" to envio.requiereCobro,
                "montoCobrar" to envio.montoCobrar,
                "metodoPago" to envio.metodoPago,
                "formaPago" to envio.formaPago,
                "distanciaKm" to envio.distanciaKm,
                "costoEnvio" to envio.costoEnvio,
                "kmAdicionales" to envio.kmAdicionales,
                "indicacionesRepartidor" to envio.indicacionesRepartidor,
                "notasInternas" to envio.notasInternas,
                "notas" to envio.notas,
                "canceladoPor" to envio.canceladoPor,
                "motivoCancelacion" to envio.motivoCancelacion,
                "fechaCancelacion" to envio.fechaCancelacion,
                "fechaAsignacion" to envio.fechaAsignacion,
                "fechaRecoleccion" to envio.fechaRecoleccion,
                "fechaEntrega" to envio.fechaEntrega,
                "fuente" to envio.fuente,
                "historialJson" to envio.historialJson,
                "createdAt" to envio.createdAt,
                "updatedAt" to envio.updatedAt
            )
            db.collection("envios").document(envio.id).set(data, SetOptions.merge()).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save envio in Firestore: ${e.message}")
        }
    }

    suspend fun updateEnvioEstado(envioId: String, nuevoEstado: String, nuevoHistorialJson: String? = null) {
        val db = firestore ?: return
        try {
            val now = System.currentTimeMillis()
            val updates = mutableMapOf<String, Any?>(
                "estado" to nuevoEstado,
                "updatedAt" to now
            )
            if (nuevoHistorialJson != null) {
                updates["historialJson"] = nuevoHistorialJson
            }
            if (nuevoEstado == "en_recoleccion" || nuevoEstado == "recogiendo") {
                updates["fechaRecoleccion"] = now
            } else if (nuevoEstado == "entregado") {
                updates["fechaEntrega"] = now
            }
            db.collection("envios").document(envioId).update(updates).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update envio status in Firestore: ${e.message}")
        }
    }

    suspend fun cancelEnvioInFirestore(envioId: String, motivo: String, canceladoPor: String, fecha: Long, historialJson: String) {
        val db = firestore ?: return
        try {
            val updates = mapOf(
                "estado" to "cancelado",
                "motivoCancelacion" to motivo,
                "canceladoPor" to canceladoPor,
                "fechaCancelacion" to fecha,
                "historialJson" to historialJson,
                "updatedAt" to fecha
            )
            db.collection("envios").document(envioId).update(updates).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cancel envio in Firestore: ${e.message}")
        }
    }

    suspend fun assignRepartidorToEnvio(envioId: String, repartidorId: String, repartidorNombre: String) {
        val db = firestore ?: return
        try {
            val now = System.currentTimeMillis()
            db.collection("envios").document(envioId).update(
                mapOf(
                    "repartidorId" to repartidorId,
                    "repartidorNombre" to repartidorNombre,
                    "estado" to "asignado",
                    "updatedAt" to now
                )
            ).awaitResult()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to assign repartidor in Firestore: ${e.message}")
        }
    }

    fun syncAllLocalDataToFirestore() {
        if (firestore == null) return
        scope.launch {
            try {
                // Sync users
                val localUsers = database.userDao().getUserCount()
                if (localUsers > 0) {
                    val usersList = database.userDao().getUserById("user_admin_01")
                    if (usersList != null) saveUser(usersList)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Sync local to Firestore: ${e.message}")
            }
        }
    }

    // ==========================================
    // MAPPERS
    // ==========================================

    private fun mapToUser(id: String, map: Map<String, Any?>): User {
        return User(
            id = id,
            uid = map["uid"] as? String ?: id,
            email = map["email"] as? String ?: "",
            fullName = map["fullName"] as? String ?: "",
            telefono = map["telefono"] as? String ?: "",
            role = map["role"] as? String ?: "proveedor",
            passwordHash = map["passwordHash"] as? String ?: "123456",
            nombreNegocio = map["nombreNegocio"] as? String,
            direccion = map["direccion"] as? String,
            fotoPerfil = map["fotoPerfil"] as? String,
            aprobadoPorAdmin = map["aprobadoPorAdmin"] as? Boolean ?: false,
            rechazado = map["rechazado"] as? Boolean ?: false,
            activo = map["activo"] as? Boolean ?: true,
            fechaRegistro = (map["fechaRegistro"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            fechaAprobacion = (map["fechaAprobacion"] as? Number)?.toLong(),
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun mapToRepartidor(id: String, map: Map<String, Any?>): Repartidor {
        return Repartidor(
            id = id,
            userId = map["userId"] as? String ?: id,
            visualCode = map["visualCode"] as? String ?: "TM-0000",
            nombreCompleto = map["nombreCompleto"] as? String ?: "",
            telefono = map["telefono"] as? String ?: "",
            fotoPerfil = map["fotoPerfil"] as? String,
            vehiculoTipo = map["vehiculoTipo"] as? String ?: "moto",
            vehiculoPlaca = map["vehiculoPlaca"] as? String,
            vehiculoMarca = map["vehiculoMarca"] as? String,
            vehiculoModelo = map["vehiculoModelo"] as? String,
            vehiculoColor = map["vehiculoColor"] as? String,
            estado = map["estado"] as? String ?: "offline",
            ubicacionActualLat = (map["ubicacionActualLat"] as? Number)?.toDouble(),
            ubicacionActualLng = (map["ubicacionActualLng"] as? Number)?.toDouble(),
            ultimaActualizacionGps = (map["ultimaActualizacionGps"] as? Number)?.toLong(),
            velocidadKmh = (map["velocidadKmh"] as? Number)?.toDouble() ?: 0.0,
            calificacionPromedio = (map["calificacionPromedio"] as? Number)?.toDouble() ?: 5.0,
            totalEntregas = (map["totalEntregas"] as? Number)?.toInt() ?: 0,
            entregasExitosas = (map["entregasExitosas"] as? Number)?.toInt() ?: 0,
            entregasCanceladas = (map["entregasCanceladas"] as? Number)?.toInt() ?: 0,
            activo = map["activo"] as? Boolean ?: true,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun mapToEnvio(id: String, map: Map<String, Any?>): Envio {
        return Envio(
            id = id,
            numeroSeguimiento = map["numeroSeguimiento"] as? String ?: "TM-00000000",
            proveedorId = map["proveedorId"] as? String ?: "",
            proveedorNombre = map["proveedorNombre"] as? String ?: "",
            proveedorNegocio = map["proveedorNegocio"] as? String,
            creadoPorUserId = map["creadoPorUserId"] as? String ?: (map["proveedorId"] as? String ?: ""),
            creadoPorRol = map["creadoPorRol"] as? String ?: "proveedor",
            origenNombre = map["origenNombre"] as? String,
            origenDireccion = map["origenDireccion"] as? String ?: "",
            origenReferencias = map["origenReferencias"] as? String,
            origenLat = (map["origenLat"] as? Number)?.toDouble(),
            origenLng = (map["origenLng"] as? Number)?.toDouble(),
            origenTelefono = map["origenTelefono"] as? String,
            destinoDireccion = map["destinoDireccion"] as? String ?: "",
            destinoReferencias = map["destinoReferencias"] as? String,
            destinoLat = (map["destinoLat"] as? Number)?.toDouble(),
            destinoLng = (map["destinoLng"] as? Number)?.toDouble(),
            destinatarioNombre = map["destinatarioNombre"] as? String ?: (map["clienteNombre"] as? String ?: ""),
            destinatarioTelefono = map["destinatarioTelefono"] as? String ?: "",
            clienteNombre = map["clienteNombre"] as? String ?: (map["destinatarioNombre"] as? String ?: ""),
            tipoServicio = map["tipoServicio"] as? String ?: "express",
            estado = map["estado"] as? String ?: "pendiente",
            repartidorId = map["repartidorId"] as? String,
            repartidorNombre = map["repartidorNombre"] as? String,
            fechaServicio = (map["fechaServicio"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            horarioTipo = map["horarioTipo"] as? String ?: "inmediato",
            franjaHoraria = map["franjaHoraria"] as? String,
            horaInicio = map["horaInicio"] as? String,
            horaFin = map["horaFin"] as? String,
            fechaProgramada = (map["fechaProgramada"] as? Number)?.toLong(),
            intervaloEntrega = map["intervaloEntrega"] as? String,
            requiereCobro = map["requiereCobro"] as? Boolean ?: false,
            montoCobrar = (map["montoCobrar"] as? Number)?.toDouble() ?: 0.0,
            metodoPago = map["metodoPago"] as? String ?: (map["formaPago"] as? String ?: "efectivo"),
            formaPago = map["formaPago"] as? String ?: "efectivo",
            distanciaKm = (map["distanciaKm"] as? Number)?.toDouble() ?: 3.5,
            costoEnvio = (map["costoEnvio"] as? Number)?.toDouble() ?: 45.0,
            kmAdicionales = (map["kmAdicionales"] as? Number)?.toDouble() ?: 0.0,
            indicacionesRepartidor = map["indicacionesRepartidor"] as? String,
            notasInternas = map["notasInternas"] as? String,
            notas = map["notas"] as? String,
            canceladoPor = map["canceladoPor"] as? String,
            motivoCancelacion = map["motivoCancelacion"] as? String,
            fechaCancelacion = (map["fechaCancelacion"] as? Number)?.toLong(),
            fechaAsignacion = (map["fechaAsignacion"] as? Number)?.toLong(),
            fechaRecoleccion = (map["fechaRecoleccion"] as? Number)?.toLong(),
            fechaEntrega = (map["fechaEntrega"] as? Number)?.toLong(),
            fuente = map["fuente"] as? String ?: "app_movil",
            historialJson = map["historialJson"] as? String ?: "[]",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

// Suspending helper for Google Play Tasks
suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
