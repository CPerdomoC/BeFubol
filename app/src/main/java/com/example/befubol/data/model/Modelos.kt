package com.example.befubol.data.model

import java.util.UUID

/**
 * Representa a un jugador de un equipo.
 * @param id Identificador único del jugador.
 * @param nombre Nombre del jugador.
 * @param esTitular Indica si el jugador es titular (true) o suplente (false).
 */
data class Jugador(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val esTitular: Boolean = true
)

/**
 * Representa una opción de color seleccionable con nombre amigable para la UI.
 */
data class OpcionColor(
    val nombre: String,
    val colorHex: Long
)

/**
 * Representa a un equipo de fútbol para las pachangas.
 * Límites del reglamento: entre 1 y 11 titulares, y hasta 5 suplentes.
 */
data class Equipo(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val colorHex: Long, // Color representativo del equipo en formato ARGB (ej: 0xFFE53935)
    val jugadores: List<Jugador> = emptyList()
) {
    val titulares: List<Jugador> get() = jugadores.filter { it.esTitular }
    val suplentes: List<Jugador> get() = jugadores.filter { !it.esTitular }

    /**
     * Valida que el equipo cumpla con las reglas:
     * - Entre 1 y 11 titulares.
     * - Máximo 5 suplentes.
     * - Nombre no vacío.
     */
    fun esValido(): Boolean {
        return nombre.isNotBlank() &&
                titulares.size in 1..11 &&
                suplentes.size <= 5
    }
}

/**
 * Representa un gol marcado durante el partido.
 */
data class Gol(
    val id: String = UUID.randomUUID().toString(),
    val jugadorId: String,
    val jugadorNombre: String,
    val equipoId: String,
    val minuto: Int
)

/**
 * Estado y desarrollo de la tanda de penaltis.
 * Cada lista contiene booleanos: true = Gol (✅), false = Fallo (❌).
 */
data class EstadoPenaltis(
    val lanzamientosEquipo1: List<Boolean> = emptyList(),
    val lanzamientosEquipo2: List<Boolean> = emptyList(),
    val turnoEquipo1: Boolean = true, // true si le toca lanzar al Equipo 1, false si al Equipo 2
    val tandaTerminada: Boolean = false,
    val ganadorEquipoId: String? = null,
    val esMuerteSubita: Boolean = false
) {
    val golesEquipo1: Int get() = lanzamientosEquipo1.count { it }
    val golesEquipo2: Int get() = lanzamientosEquipo2.count { it }
}

/**
 * Representa un partido completo (en curso o finalizado).
 */
data class Partido(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String, // Ejemplo: "Partido 1" o "Gran Final"
    val equipo1: Equipo,
    val equipo2: Equipo,
    val duracionMinutos: Int,
    val fechaHoraInicio: String,
    val goles: List<Gol> = emptyList(),
    val estadoPenaltis: EstadoPenaltis? = null,
    val terminado: Boolean = false,
    val ganadorEquipoId: String? = null // Id del equipo ganador o null si hay empate sin penaltis
) {
    val golesEquipo1: Int get() = goles.count { it.equipoId == equipo1.id }
    val golesEquipo2: Int get() = goles.count { it.equipoId == equipo2.id }
}

/**
 * Estadística para mostrar el máximo goleador ("Pichichi") de la sesión.
 */
data class GoleadorEstadistica(
    val nombre: String,
    val totalGoles: Int
)
