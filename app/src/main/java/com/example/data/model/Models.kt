package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class UserRole(val value: String, val label: String) {
    ADMIN("admin", "Administrador"),
    PROVEEDOR("proveedor", "Proveedor"),
    REPARTIDOR("repartidor", "Repartidor"),
    COORDINADOR("coordinador", "Coordinador"),
    ASISTENTE("asistente", "Asistente");

    companion object {
        fun fromValue(value: String): UserRole {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PROVEEDOR
        }
    }
}

enum class VehiculoTipo(val value: String, val label: String) {
    MOTO("moto", "Motocicleta"),
    BICICLETA("bicicleta", "Bicicleta"),
    AUTO("auto", "Automóvil"),
    CAMIONETA("camioneta", "Camioneta");

    companion object {
        fun fromValue(value: String): VehiculoTipo {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: MOTO
        }
    }
}

enum class RepartidorEstado(val value: String, val label: String) {
    DISPONIBLE("disponible", "Disponible"),
    EN_PEDIDO("en_pedido", "Ocupado"),
    OFFLINE("offline", "Offline"),
    PAUSA("pausa", "En Pausa");

    companion object {
        fun fromValue(value: String): RepartidorEstado {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: OFFLINE
        }
    }
}

enum class TipoServicio(val value: String, val label: String, val descripcion: String, val tarifaBase: Double) {
    BASICO("basico", "Básico", "Entrega estándar para pedidos locales", 35.0),
    EXPRESS("express", "Express", "Entrega prioritaria con atención inmediata", 50.0),
    PROGRAMADO("programado", "Programado", "Selecciona fecha y horario de entrega", 45.0),
    TURBO("turbo", "Turbo Flash", "Servicio especial para distancias mayores o fuera de zona", 70.0);

    companion object {
        fun fromValue(value: String): TipoServicio {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: EXPRESS
        }
    }
}

enum class EnvioEstado(val value: String, val label: String) {
    PENDIENTE("pendiente", "Pendiente"),
    ASIGNADO("asignado", "Asignado"),
    ACEPTADO("aceptado", "Aceptado"),
    EN_RECOLECCION("en_recoleccion", "En Recolección"),
    RECOLECTADO("recolectado", "Recolectado"),
    RECOGIENDO("recogiendo", "Recogiendo"),
    EN_CAMINO("en_camino", "En Camino"),
    ENTREGADO("entregado", "Entregado"),
    NO_ENTREGADO("no_entregado", "No Entregado"),
    CANCELADO("cancelado", "Cancelado");

    companion object {
        fun fromValue(value: String): EnvioEstado {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDIENTE
        }
    }
}

enum class FormaPago(val value: String, val label: String) {
    EFECTIVO("efectivo", "Efectivo"),
    TRANSFERENCIA("transferencia", "Transferencia"),
    TARJETA("tarjeta", "Tarjeta"),
    SIN_COBRO("sin_cobro", "Sin Cobro");

    companion object {
        fun fromValue(value: String): FormaPago {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: EFECTIVO
        }
    }
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val uid: String = UUID.randomUUID().toString(),
    val email: String,
    val fullName: String,
    val telefono: String,
    val role: String, // admin, proveedor, repartidor, coordinador, asistente
    val passwordHash: String = "123456",
    val nombreNegocio: String? = null,
    val direccion: String? = null,
    val fotoPerfil: String? = null,
    val aprobadoPorAdmin: Boolean = false,
    val rechazado: Boolean = false,
    val activo: Boolean = true,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaAprobacion: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "repartidores")
data class Repartidor(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val visualCode: String = "TM-${(1000..9999).random()}",
    val nombreCompleto: String,
    val telefono: String,
    val fotoPerfil: String? = null,
    val vehiculoTipo: String = VehiculoTipo.MOTO.value,
    val vehiculoPlaca: String? = null,
    val vehiculoMarca: String? = null,
    val vehiculoModelo: String? = null,
    val vehiculoColor: String? = null,
    val estado: String = RepartidorEstado.OFFLINE.value,
    val ubicacionActualLat: Double? = 20.9674, // Mérida, Yucatán
    val ubicacionActualLng: Double? = -89.5926,
    val ultimaActualizacionGps: Long? = System.currentTimeMillis(),
    val velocidadKmh: Double? = 0.0,
    val calificacionPromedio: Double = 5.0,
    val totalEntregas: Int = 0,
    val entregasExitosas: Int = 0,
    val entregasCanceladas: Int = 0,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "envios")
data class Envio(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val numeroSeguimiento: String = generateTrackingNumber(),

    val proveedorId: String,
    val proveedorNombre: String = "",
    val proveedorNegocio: String? = null,

    val creadoPorUserId: String = proveedorId,
    val creadoPorRol: String = "proveedor",

    // ORIGEN
    val origenNombre: String? = null,
    val origenDireccion: String,
    val origenReferencias: String? = null,
    val origenLat: Double? = 20.9674,
    val origenLng: Double? = -89.5926,
    val origenTelefono: String? = null,

    // DESTINO
    val destinatarioNombre: String = "",
    val destinatarioTelefono: String = "",
    val destinoDireccion: String,
    val destinoReferencias: String? = null,
    val destinoLat: Double? = 20.9753,
    val destinoLng: Double? = -89.6231,
    val clienteNombre: String = destinatarioNombre,

    // SERVICIO
    val tipoServicio: String = TipoServicio.EXPRESS.value,

    // ESTADO
    val estado: String = EnvioEstado.PENDIENTE.value,

    // REPARTIDOR
    val repartidorId: String? = null,
    val repartidorNombre: String? = null,

    // PROGRAMACIÓN
    val fechaServicio: Long = System.currentTimeMillis(),
    val horarioTipo: String = "inmediato", // "inmediato" | "programado"
    val franjaHoraria: String? = null,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val fechaProgramada: Long? = null,
    val intervaloEntrega: String? = "30-45 min",

    // COBRO
    val requiereCobro: Boolean = false,
    val montoCobrar: Double = 0.0,
    val metodoPago: String = FormaPago.EFECTIVO.value,
    val formaPago: String = FormaPago.EFECTIVO.value,

    // TARIFA
    val distanciaKm: Double? = 3.5,
    val costoEnvio: Double = 45.0,
    val kmAdicionales: Double? = 0.0,

    // INFORMACIÓN
    val indicacionesRepartidor: String? = null,
    val notasInternas: String? = null,
    val notas: String? = null,

    // CONTROL
    val canceladoPor: String? = null,
    val motivoCancelacion: String? = null,
    val fechaCancelacion: Long? = null,

    // TIEMPOS
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val fechaAsignacion: Long? = null,
    val fechaRecoleccion: Long? = null,
    val fechaEntrega: Long? = null,

    // FUTURO
    val fuente: String? = "app_movil",
    val clienteFrecuenteId: String? = null,
    val rutaId: String? = null,
    val historialJson: String = "[]"
) {
    fun parseHistorial(): List<HistorialItem> {
        val list = mutableListOf<HistorialItem>()
        if (historialJson.isBlank() || historialJson == "[]") return list
        try {
            val jsonArray = JSONArray(historialJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HistorialItem(
                        estado = obj.optString("estado", "pendiente"),
                        fecha = obj.optLong("fecha", System.currentTimeMillis()),
                        usuarioId = obj.optString("usuarioId", ""),
                        usuarioNombre = obj.optString("usuarioNombre", null),
                        descripcion = obj.optString("descripcion", null)
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    fun withHistorialEntry(entry: HistorialItem): Envio {
        val current = parseHistorial().toMutableList()
        current.add(entry)
        val jsonArray = JSONArray()
        for (item in current) {
            val obj = JSONObject()
            obj.put("estado", item.estado)
            obj.put("fecha", item.fecha)
            obj.put("usuarioId", item.usuarioId)
            if (item.usuarioNombre != null) obj.put("usuarioNombre", item.usuarioNombre)
            if (item.descripcion != null) obj.put("descripcion", item.descripcion)
            jsonArray.put(obj)
        }
        return copy(
            historialJson = jsonArray.toString(),
            updatedAt = System.currentTimeMillis()
        )
    }

    companion object {
        fun generateTrackingNumber(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val randomStr = (1..8)
                .map { chars.random() }
                .joinToString("")
            return "TM-$randomStr"
        }
    }
}

data class HistorialItem(
    val estado: String,
    val fecha: Long = System.currentTimeMillis(),
    val usuarioId: String,
    val usuarioNombre: String? = null,
    val descripcion: String? = null
)
