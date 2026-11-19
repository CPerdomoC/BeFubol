package com.example.befubol.data.model

import java.util.UUID

data class Jugador(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val esTitular: Boolean = true
)

data class OpcionColor(
    val nombre: String,
    val colorHex: Long
)

data class Equipo(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val colorHex: Long,
    val jugadores: List<Jugador> = emptyList()
) {
    val titulares: List<Jugador> get() = jugadores.filter { it.esTitular }
    val suplentes: List<Jugador> get() = jugadores.filter { !it.esTitular }

    fun esValido(): Boolean {
        return nombre.isNotBlank() &&
                titulares.size in 1..11 &&
                suplentes.size <= 5
    }
}

data class Gol(
    val id: String = UUID.randomUUID().toString(),
    val jugadorId: String,
    val jugadorNombre: String,
    val equipoId: String,
    val minuto: Int
)

data class EstadoPenaltis(
    val lanzamientosEquipo1: List<Boolean> = emptyList(),
    val lanzamientosEquipo2: List<Boolean> = emptyList(),
    val turnoEquipo1: Boolean = true,
    val tandaTerminada: Boolean = false,
    val ganadorEquipoId: String? = null,
    val esMuerteSubita: Boolean = false
) {
    val golesEquipo1: Int get() = lanzamientosEquipo1.count { it }
    val golesEquipo2: Int get() = lanzamientosEquipo2.count { it }
}

data class Partido(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val equipo1: Equipo,
    val equipo2: Equipo,
    val duracionMinutos: Int,
    val fechaHoraInicio: String,
    val goles: List<Gol> = emptyList(),
    val estadoPenaltis: EstadoPenaltis? = null,
    val terminado: Boolean = false,
    val ganadorEquipoId: String? = null
) {
    val golesEquipo1: Int get() = goles.count { it.equipoId == equipo1.id }
    val golesEquipo2: Int get() = goles.count { it.equipoId == equipo2.id }
}

data class GoleadorEstadistica(
    val nombre: String,
    val totalGoles: Int
)
