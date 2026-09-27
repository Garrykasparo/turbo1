package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Envio
import com.example.data.model.EnvioEstado
import com.example.data.model.FormaPago
import com.example.data.model.Repartidor
import com.example.data.model.RepartidorEstado
import com.example.data.model.TipoServicio
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VehiculoTipo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    suspend fun seedIfNeeded(database: AppDatabase) = withContext(Dispatchers.IO) {
        val userCount = database.userDao().getUserCount()
        if (userCount > 0) return@withContext

        // Admin User
        val adminUser = User(
            id = "user_admin_01",
            uid = "uid_admin_01",
            email = "admin@turbomandados.mx",
            fullName = "Administrador Central",
            telefono = "999-123-4567",
            role = UserRole.ADMIN.value,
            passwordHash = "123456",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        database.userDao().insertUser(adminUser)

        // Coordinador
        val coordUser = User(
            id = "user_coord_01",
            uid = "uid_coord_01",
            email = "coordinador@turbomandados.mx",
            fullName = "Valeria Morales",
            telefono = "999-234-5678",
            role = UserRole.COORDINADOR.value,
            passwordHash = "123456",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        database.userDao().insertUser(coordUser)

        // Proveedores
        val provUser1 = User(
            id = "user_prov_01",
            uid = "uid_prov_01",
            email = "sureno@turbomandados.mx",
            fullName = "Roberto Pacheco",
            telefono = "999-345-6789",
            role = UserRole.PROVEEDOR.value,
            passwordHash = "123456",
            nombreNegocio = "Restaurante El Sureño",
            direccion = "Calle 60 #345 x 35 y 37, Centro, Mérida",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        val provUser2 = User(
            id = "user_prov_02",
            uid = "uid_prov_02",
            email = "progreso@turbomandados.mx",
            fullName = "Mariana Lara",
            telefono = "999-456-7890",
            role = UserRole.PROVEEDOR.value,
            passwordHash = "123456",
            nombreNegocio = "Farmacia Progreso",
            direccion = "Av. Prolongación Montejo #120, Campestre, Mérida",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        database.userDao().insertUser(provUser1)
        database.userDao().insertUser(provUser2)

        // Repartidores Users & Repartidor records
        val repUser1 = User(
            id = "user_rep_01",
            uid = "uid_rep_01",
            email = "carlos@repartidor.mx",
            fullName = "Carlos Méndez",
            telefono = "999-567-8901",
            role = UserRole.REPARTIDOR.value,
            passwordHash = "123456",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        val repRecord1 = Repartidor(
            id = "rep_01",
            userId = "user_rep_01",
            visualCode = "TM-1042",
            nombreCompleto = "Carlos Méndez",
            telefono = "999-567-8901",
            vehiculoTipo = VehiculoTipo.MOTO.value,
            vehiculoPlaca = "YUC-45-8A",
            vehiculoMarca = "Honda",
            vehiculoModelo = "Cargo 150",
            vehiculoColor = "Rojo",
            estado = RepartidorEstado.DISPONIBLE.value,
            calificacionPromedio = 4.9,
            totalEntregas = 84,
            entregasExitosas = 82,
            entregasCanceladas = 2
        )

        val repUser2 = User(
            id = "user_rep_02",
            uid = "uid_rep_02",
            email = "sofia@repartidor.mx",
            fullName = "Sofía Chan",
            telefono = "999-678-9012",
            role = UserRole.REPARTIDOR.value,
            passwordHash = "123456",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        val repRecord2 = Repartidor(
            id = "rep_02",
            userId = "user_rep_02",
            visualCode = "TM-2089",
            nombreCompleto = "Sofía Chan",
            telefono = "999-678-9012",
            vehiculoTipo = VehiculoTipo.BICICLETA.value,
            vehiculoPlaca = "S/P",
            vehiculoMarca = "Giant",
            vehiculoModelo = "Escape 3",
            vehiculoColor = "Negro",
            estado = RepartidorEstado.EN_PEDIDO.value,
            calificacionPromedio = 5.0,
            totalEntregas = 45,
            entregasExitosas = 45,
            entregasCanceladas = 0
        )

        val repUser3 = User(
            id = "user_rep_03",
            uid = "uid_rep_03",
            email = "miguel@repartidor.mx",
            fullName = "Miguel Pech",
            telefono = "999-789-0123",
            role = UserRole.REPARTIDOR.value,
            passwordHash = "123456",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        val repRecord3 = Repartidor(
            id = "rep_03",
            userId = "user_rep_03",
            visualCode = "TM-3114",
            nombreCompleto = "Miguel Pech",
            telefono = "999-789-0123",
            vehiculoTipo = VehiculoTipo.AUTO.value,
            vehiculoPlaca = "YUC-99-1Z",
            vehiculoMarca = "Nissan",
            vehiculoModelo = "March",
            vehiculoColor = "Plata",
            estado = RepartidorEstado.DISPONIBLE.value,
            calificacionPromedio = 4.8,
            totalEntregas = 112,
            entregasExitosas = 110,
            entregasCanceladas = 2
        )

        val repUser4 = User(
            id = "user_rep_04",
            uid = "uid_rep_04",
            email = "alejandro@repartidor.mx",
            fullName = "Alejandro Canto",
            telefono = "999-890-1234",
            role = UserRole.REPARTIDOR.value,
            passwordHash = "123456",
            aprobadoPorAdmin = true,
            rechazado = false,
            activo = true
        )
        val repRecord4 = Repartidor(
            id = "rep_04",
            userId = "user_rep_04",
            visualCode = "TM-4450",
            nombreCompleto = "Alejandro Canto",
            telefono = "999-890-1234",
            vehiculoTipo = VehiculoTipo.MOTO.value,
            vehiculoPlaca = "YUC-12-3X",
            vehiculoMarca = "Yamaha",
            vehiculoModelo = "FZ 2.0",
            vehiculoColor = "Azul",
            estado = RepartidorEstado.OFFLINE.value,
            calificacionPromedio = 4.7,
            totalEntregas = 34,
            entregasExitosas = 33,
            entregasCanceladas = 1
        )

        database.userDao().insertUser(repUser1)
        database.userDao().insertUser(repUser2)
        database.userDao().insertUser(repUser3)
        database.userDao().insertUser(repUser4)

        database.repartidorDao().insertRepartidor(repRecord1)
        database.repartidorDao().insertRepartidor(repRecord2)
        database.repartidorDao().insertRepartidor(repRecord3)
        database.repartidorDao().insertRepartidor(repRecord4)

        // Pending user for approval demo testing!
        val pendingUser = User(
            id = "user_pending_demo",
            uid = "uid_pending_demo",
            email = "tacos.amigo@merida.mx",
            fullName = "Pedro Novelo",
            telefono = "999-901-2345",
            role = UserRole.PROVEEDOR.value,
            passwordHash = "123456",
            nombreNegocio = "Taquería El Amigo",
            direccion = "Calle 21 x 14, Altabrisa",
            aprobadoPorAdmin = false,
            rechazado = false,
            activo = true
        )
        database.userDao().insertUser(pendingUser)

        // 8 Envíos Demo con Historial completo
        val envios = listOf(
            Envio(
                id = "envio_01",
                numeroSeguimiento = "TM-X89K2A11",
                proveedorId = "user_prov_01",
                proveedorNombre = "Restaurante El Sureño",
                proveedorNegocio = "Restaurante El Sureño",
                origenNombre = "Restaurante El Sureño - Matriz",
                origenDireccion = "Calle 60 #345 Centro, Mérida",
                origenTelefono = "999-345-6789",
                origenReferencias = "Portón de madera, timbre 1",
                destinoDireccion = "Calle 15 #210, Fracc. Altabrisa",
                destinoReferencias = "Fachada blanca, portón eléctrico negro",
                destinatarioNombre = "Andrea Canul",
                destinatarioTelefono = "999-111-2233",
                clienteNombre = "Andrea Canul",
                tipoServicio = TipoServicio.TURBO.value,
                estado = EnvioEstado.PENDIENTE.value,
                requiereCobro = true,
                montoCobrar = 380.0,
                costoEnvio = 55.0,
                distanciaKm = 6.4,
                metodoPago = FormaPago.EFECTIVO.value,
                formaPago = FormaPago.EFECTIVO.value,
                indicacionesRepartidor = "Entregar en caseta de vigilancia, pedir identificación.",
                notas = "Entregar en caseta de vigilancia, pedir identificación.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 3600000},"usuarioId":"user_prov_01","usuarioNombre":"Roberto Pacheco","descripcion":"Pedido creado en línea con servicio TURBO"}]"""
            ),
            Envio(
                id = "envio_02",
                numeroSeguimiento = "TM-P32M9B45",
                proveedorId = "user_prov_02",
                proveedorNombre = "Farmacia Progreso",
                proveedorNegocio = "Farmacia Progreso",
                origenNombre = "Farmacia Progreso - Sucursal Montejo",
                origenDireccion = "Av. Prolongación Montejo #120, Campestre",
                origenTelefono = "999-456-7890",
                destinoDireccion = "Calle 49 #502, Francisco de Montejo",
                destinoReferencias = "Casa de 2 pisos, portón blanco",
                destinatarioNombre = "Lic. Fernando Díaz",
                destinatarioTelefono = "999-222-3344",
                clienteNombre = "Lic. Fernando Díaz",
                tipoServicio = TipoServicio.EXPRESS.value,
                estado = EnvioEstado.PENDIENTE.value,
                requiereCobro = true,
                montoCobrar = 620.0,
                costoEnvio = 45.0,
                distanciaKm = 4.2,
                metodoPago = FormaPago.TARJETA.value,
                formaPago = FormaPago.TARJETA.value,
                indicacionesRepartidor = "Medicamento delicado, mantener en sombra.",
                notas = "Medicamento delicado, mantener en sombra.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 2800000},"usuarioId":"user_prov_02","usuarioNombre":"Mariana Lara","descripcion":"Pedido recibido para envío express"}]"""
            ),
            Envio(
                id = "envio_03",
                numeroSeguimiento = "TM-A55L8C90",
                proveedorId = "user_prov_01",
                proveedorNombre = "Restaurante El Sureño",
                proveedorNegocio = "Restaurante El Sureño",
                origenNombre = "Restaurante El Sureño",
                origenDireccion = "Calle 60 #345 Centro, Mérida",
                origenTelefono = "999-345-6789",
                destinoDireccion = "Privada Montebello Lote 14",
                destinoReferencias = "Caseta de vigilancia entrada privada",
                destinatarioNombre = "Patricia Gómez",
                destinatarioTelefono = "999-333-4455",
                clienteNombre = "Patricia Gómez",
                tipoServicio = TipoServicio.EXPRESS.value,
                estado = EnvioEstado.ASIGNADO.value,
                repartidorId = "rep_01",
                repartidorNombre = "Carlos Méndez",
                fechaAsignacion = System.currentTimeMillis() - 1500000,
                requiereCobro = true,
                montoCobrar = 450.0,
                costoEnvio = 50.0,
                distanciaKm = 5.1,
                metodoPago = FormaPago.TRANSFERENCIA.value,
                formaPago = FormaPago.TRANSFERENCIA.value,
                indicacionesRepartidor = "Comida caliente, listo en mostrador.",
                notas = "Comida caliente, listo en mostrador.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 3000000},"usuarioId":"user_prov_01","usuarioNombre":"Roberto Pacheco","descripcion":"Pedido creado"},{"estado":"asignado","fecha":${System.currentTimeMillis() - 1500000},"usuarioId":"user_coord_01","usuarioNombre":"Valeria Morales","descripcion":"Asignado a repartidor: Carlos Méndez"}]"""
            ),
            Envio(
                id = "envio_04",
                numeroSeguimiento = "TM-R77K1D23",
                proveedorId = "user_prov_02",
                proveedorNombre = "Farmacia Progreso",
                proveedorNegocio = "Farmacia Progreso",
                origenNombre = "Farmacia Progreso",
                origenDireccion = "Av. Prolongación Montejo #120",
                origenTelefono = "999-456-7890",
                destinoDireccion = "Calle 1H #130, Campestre",
                destinatarioNombre = "Guillermo Romero",
                destinatarioTelefono = "999-444-5566",
                clienteNombre = "Guillermo Romero",
                tipoServicio = TipoServicio.TURBO.value,
                estado = EnvioEstado.EN_RECOLECCION.value,
                repartidorId = "rep_01",
                repartidorNombre = "Carlos Méndez",
                fechaAsignacion = System.currentTimeMillis() - 2000000,
                fechaRecoleccion = System.currentTimeMillis() - 600000,
                requiereCobro = true,
                montoCobrar = 190.0,
                costoEnvio = 40.0,
                distanciaKm = 2.8,
                metodoPago = FormaPago.EFECTIVO.value,
                formaPago = FormaPago.EFECTIVO.value,
                indicacionesRepartidor = "El repartidor ya está en la sucursal recogiendo.",
                notas = "El repartidor ya está en la sucursal recogiendo.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 3600000},"usuarioId":"user_prov_02","usuarioNombre":"Mariana Lara","descripcion":"Pedido creado"},{"estado":"asignado","fecha":${System.currentTimeMillis() - 2000000},"usuarioId":"user_coord_01","usuarioNombre":"Valeria Morales","descripcion":"Asignado a Carlos Méndez"},{"estado":"en_recoleccion","fecha":${System.currentTimeMillis() - 600000},"usuarioId":"rep_01","usuarioNombre":"Carlos Méndez","descripcion":"Repartidor en farmacia"}]"""
            ),
            Envio(
                id = "envio_05",
                numeroSeguimiento = "TM-E12W4E67",
                proveedorId = "user_prov_01",
                proveedorNombre = "Restaurante El Sureño",
                proveedorNegocio = "Restaurante El Sureño",
                origenNombre = "Restaurante El Sureño",
                origenDireccion = "Calle 60 #345 Centro, Mérida",
                destinoDireccion = "Calle 25 #304, García Ginerés",
                destinatarioNombre = "Esteban Solís",
                destinatarioTelefono = "999-555-6677",
                clienteNombre = "Esteban Solís",
                tipoServicio = TipoServicio.BASICO.value,
                estado = EnvioEstado.EN_CAMINO.value,
                repartidorId = "rep_02",
                repartidorNombre = "Sofía Chan",
                fechaAsignacion = System.currentTimeMillis() - 2500000,
                fechaRecoleccion = System.currentTimeMillis() - 1200000,
                requiereCobro = true,
                montoCobrar = 275.0,
                costoEnvio = 40.0,
                distanciaKm = 3.5,
                metodoPago = FormaPago.EFECTIVO.value,
                formaPago = FormaPago.EFECTIVO.value,
                indicacionesRepartidor = "En camino en bicicleta hacia destino.",
                notas = "En camino en bicicleta hacia destino.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 3000000},"usuarioId":"user_prov_01","usuarioNombre":"Roberto Pacheco","descripcion":"Pedido creado"},{"estado":"asignado","fecha":${System.currentTimeMillis() - 2500000},"usuarioId":"user_coord_01","usuarioNombre":"Valeria Morales","descripcion":"Asignado a Sofía Chan"},{"estado":"en_recoleccion","fecha":${System.currentTimeMillis() - 1200000},"usuarioId":"rep_02","usuarioNombre":"Sofía Chan","descripcion":"Paquete recolectado"},{"estado":"en_camino","fecha":${System.currentTimeMillis() - 500000},"usuarioId":"rep_02","usuarioNombre":"Sofía Chan","descripcion":"En trayecto hacia el cliente"}]"""
            ),
            Envio(
                id = "envio_06",
                numeroSeguimiento = "TM-F89V5G78",
                proveedorId = "user_prov_02",
                proveedorNombre = "Farmacia Progreso",
                proveedorNegocio = "Farmacia Progreso",
                origenNombre = "Farmacia Progreso",
                origenDireccion = "Av. Prolongación Montejo #120",
                destinoDireccion = "Av. Dzibilchaltún #450, Las Américas",
                destinatarioNombre = "Dra. Carmen Ceballos",
                destinatarioTelefono = "999-666-7788",
                clienteNombre = "Dra. Carmen Ceballos",
                tipoServicio = TipoServicio.PROGRAMADO.value,
                estado = EnvioEstado.EN_CAMINO.value,
                horarioTipo = "programado",
                franjaHoraria = "02:00 PM - 04:00 PM",
                repartidorId = "rep_03",
                repartidorNombre = "Miguel Pech",
                fechaAsignacion = System.currentTimeMillis() - 3600000,
                fechaRecoleccion = System.currentTimeMillis() - 1800000,
                requiereCobro = true,
                montoCobrar = 850.0,
                costoEnvio = 60.0,
                distanciaKm = 8.5,
                metodoPago = FormaPago.TARJETA.value,
                formaPago = FormaPago.TARJETA.value,
                indicacionesRepartidor = "Llevar terminal bancaria Clip/MercadoPago.",
                notas = "Llevar terminal bancaria Clip/MercadoPago.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 7200000},"usuarioId":"user_prov_02","usuarioNombre":"Mariana Lara","descripcion":"Pedido programado registrado"},{"estado":"asignado","fecha":${System.currentTimeMillis() - 3600000},"usuarioId":"user_coord_01","usuarioNombre":"Valeria Morales","descripcion":"Asignado a Miguel Pech"},{"estado":"en_camino","fecha":${System.currentTimeMillis() - 1800000},"usuarioId":"rep_03","usuarioNombre":"Miguel Pech","descripcion":"En ruta hacia Las Américas"}]"""
            ),
            Envio(
                id = "envio_07",
                numeroSeguimiento = "TM-T44J2H99",
                proveedorId = "user_prov_01",
                proveedorNombre = "Restaurante El Sureño",
                proveedorNegocio = "Restaurante El Sureño",
                origenNombre = "Restaurante El Sureño",
                origenDireccion = "Calle 60 #345 Centro, Mérida",
                destinoDireccion = "Calle 55 #480, Santa Lucía, Centro",
                destinatarioNombre = "Raúl Várguez",
                destinatarioTelefono = "999-777-8899",
                clienteNombre = "Raúl Várguez",
                tipoServicio = TipoServicio.EXPRESS.value,
                estado = EnvioEstado.ENTREGADO.value,
                repartidorId = "rep_01",
                repartidorNombre = "Carlos Méndez",
                fechaAsignacion = System.currentTimeMillis() - 7200000,
                fechaRecoleccion = System.currentTimeMillis() - 5400000,
                fechaEntrega = System.currentTimeMillis() - 3600000,
                requiereCobro = true,
                montoCobrar = 310.0,
                costoEnvio = 45.0,
                distanciaKm = 1.5,
                metodoPago = FormaPago.EFECTIVO.value,
                formaPago = FormaPago.EFECTIVO.value,
                indicacionesRepartidor = "Entregado exitosamente con firma.",
                notas = "Entregado exitosamente con firma.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 8000000},"usuarioId":"user_prov_01","usuarioNombre":"Roberto Pacheco","descripcion":"Pedido creado"},{"estado":"asignado","fecha":${System.currentTimeMillis() - 7200000},"usuarioId":"user_coord_01","usuarioNombre":"Valeria Morales","descripcion":"Asignado a Carlos Méndez"},{"estado":"en_camino","fecha":${System.currentTimeMillis() - 5400000},"usuarioId":"rep_01","usuarioNombre":"Carlos Méndez","descripcion":"En trayecto"},{"estado":"entregado","fecha":${System.currentTimeMillis() - 3600000},"usuarioId":"rep_01","usuarioNombre":"Carlos Méndez","descripcion":"Entregado en Santa Lucía"}]"""
            ),
            Envio(
                id = "envio_08",
                numeroSeguimiento = "TM-Z66N3K12",
                proveedorId = "user_prov_02",
                proveedorNombre = "Farmacia Progreso",
                proveedorNegocio = "Farmacia Progreso",
                origenNombre = "Farmacia Progreso",
                origenDireccion = "Av. Prolongación Montejo #120",
                destinoDireccion = "Calle 20 #105, Cholul",
                destinatarioNombre = "Sra. Teresa Burgos",
                destinatarioTelefono = "999-888-9900",
                clienteNombre = "Sra. Teresa Burgos",
                tipoServicio = TipoServicio.TURBO.value,
                estado = EnvioEstado.ENTREGADO.value,
                repartidorId = "rep_03",
                repartidorNombre = "Miguel Pech",
                fechaAsignacion = System.currentTimeMillis() - 10800000,
                fechaRecoleccion = System.currentTimeMillis() - 7200000,
                fechaEntrega = System.currentTimeMillis() - 4000000,
                requiereCobro = true,
                montoCobrar = 540.0,
                costoEnvio = 65.0,
                distanciaKm = 11.2,
                metodoPago = FormaPago.TRANSFERENCIA.value,
                formaPago = FormaPago.TRANSFERENCIA.value,
                indicacionesRepartidor = "Entregado a las 11:30 AM.",
                notas = "Entregado a las 11:30 AM.",
                historialJson = """[{"estado":"pendiente","fecha":${System.currentTimeMillis() - 12000000},"usuarioId":"user_prov_02","usuarioNombre":"Mariana Lara","descripcion":"Pedido creado"},{"estado":"asignado","fecha":${System.currentTimeMillis() - 10800000},"usuarioId":"user_coord_01","usuarioNombre":"Valeria Morales","descripcion":"Asignado a Miguel Pech"},{"estado":"en_camino","fecha":${System.currentTimeMillis() - 7200000},"usuarioId":"rep_03","usuarioNombre":"Miguel Pech","descripcion":"En camino a Cholul"},{"estado":"entregado","fecha":${System.currentTimeMillis() - 4000000},"usuarioId":"rep_03","usuarioNombre":"Miguel Pech","descripcion":"Entregado con transferencia verificada"}]"""
            )
        )

        for (envio in envios) {
            database.envioDao().insertEnvio(envio)
        }
    }
}
