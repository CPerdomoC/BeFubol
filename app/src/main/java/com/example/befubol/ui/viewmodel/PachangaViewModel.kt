package com.example.befubol.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.befubol.data.model.Equipo
import com.example.befubol.data.model.EstadoPenaltis
import com.example.befubol.data.model.Gol
import com.example.befubol.data.model.GoleadorEstadistica
import com.example.befubol.data.model.Jugador
import com.example.befubol.data.model.OpcionColor
import com.example.befubol.data.model.Partido
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PachangaViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("befubol_prefs", Context.MODE_PRIVATE)

    val opcionesColoresDisponibles = listOf(
        OpcionColor("Rojo Furia", 0xFFEF4444),
        OpcionColor("Azul Real", 0xFF2563EB),
        OpcionColor("Verde Césped", 0xFF10B981),
        OpcionColor("Amarillo Oro", 0xFFF59E0B),
        OpcionColor("Naranja Fuego", 0xFFEA580C),
        OpcionColor("Morado Rayo", 0xFF7C3AED),
        OpcionColor("Cian Neón", 0xFF06B6D4),
        OpcionColor("Rosa Flúor", 0xFFDB2777),
        OpcionColor("Blanco Plata", 0xFFE2E8F0),
        OpcionColor("Negro Noche", 0xFF1E293B)
    )

    private val _equipos = MutableStateFlow<List<Equipo>>(cargarEquiposDeAlmacenamiento())
    val equipos: StateFlow<List<Equipo>> = _equipos.asStateFlow()

    private val _partidoActual = MutableStateFlow<Partido?>(null)
    val partidoActual: StateFlow<Partido?> = _partidoActual.asStateFlow()

    private val _tiempoRestanteSegundos = MutableStateFlow(0)
    val tiempoRestanteSegundos: StateFlow<Int> = _tiempoRestanteSegundos.asStateFlow()

    private val _cronometroEnPausa = MutableStateFlow(true)
    val cronometroEnPausa: StateFlow<Boolean> = _cronometroEnPausa.asStateFlow()

    private val _ultimoGolCelebracion = MutableStateFlow<Gol?>(null)
    val ultimoGolCelebracion: StateFlow<Gol?> = _ultimoGolCelebracion.asStateFlow()

    private val _historialPartidos = MutableStateFlow<List<Partido>>(cargarHistorialDeAlmacenamiento())
    val historialPartidos: StateFlow<List<Partido>> = _historialPartidos.asStateFlow()

    val maximoGoleador: StateFlow<GoleadorEstadistica?> = _historialPartidos.map { partidos ->
        val todosLosGoles = partidos.flatMap { it.goles }
        if (todosLosGoles.isEmpty()) {
            null
        } else {
            val conteoPorJugador = todosLosGoles.groupingBy { it.jugadorNombre }.eachCount()
            val maxEntrada = conteoPorJugador.maxByOrNull { it.value }
            if (maxEntrada != null) {
                GoleadorEstadistica(nombre = maxEntrada.key, totalGoles = maxEntrada.value)
            } else {
                null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var contadorPartidos = _historialPartidos.value.size + 1

    private var jobCronometro: Job? = null

    fun guardarEquipo(equipo: Equipo) {
        _equipos.update { lista ->
            val index = lista.indexOfFirst { it.id == equipo.id }
            val nuevaLista = if (index != -1) {
                lista.toMutableList().apply { set(index, equipo) }
            } else {
                lista + equipo
            }
            guardarEquiposEnAlmacenamiento(nuevaLista)
            nuevaLista
        }
    }

    fun eliminarEquipo(equipoId: String) {
        _equipos.update { lista ->
            val nuevaLista = lista.filter { it.id != equipoId }
            guardarEquiposEnAlmacenamiento(nuevaLista)
            nuevaLista
        }
    }

    fun sortearEquipos(
        nombresJugadores: List<String>,
        numeroEquipos: Int = 2
    ) {
        val limpios = nombresJugadores.map { it.trim() }.filter { it.isNotBlank() }
        val numEquiposAjustado = numeroEquipos.coerceIn(2, maxOf(2, minOf(limpios.size, 6)))
        if (limpios.size < numEquiposAjustado) return

        val barajados = limpios.shuffled()
        val nombresDefecto = listOf(
            "Equipo Blanco", "Equipo Azul", "Equipo Rojo",
            "Equipo Verde", "Equipo Amarillo", "Equipo Morado"
        )
        val coloresDefecto = listOf(
            0xFFE2E8F0,
            0xFF2563EB,
            0xFFEF4444,
            0xFF10B981,
            0xFFF59E0B,
            0xFF7C3AED
        )

        val listasJugadores = List(numEquiposAjustado) { mutableListOf<Jugador>() }
        barajados.forEachIndexed { index, nombre ->
            val eqIdx = index % numEquiposAjustado
            val esTitular = listasJugadores[eqIdx].size < 11
            listasJugadores[eqIdx].add(Jugador(nombre = nombre, esTitular = esTitular))
        }

        for (i in 0 until numEquiposAjustado) {
            val nombreEq = nombresDefecto.getOrElse(i) { "Equipo ${i + 1}" }
            val colorEq = coloresDefecto.getOrElse(i) { 0xFFE2E8F0 }
            val nuevoEquipo = Equipo(
                nombre = nombreEq,
                colorHex = colorEq,
                jugadores = listasJugadores[i]
            )
            guardarEquipo(nuevoEquipo)
        }
    }

    fun iniciarPartido(
        equipo1: Equipo,
        equipo2: Equipo,
        duracionMinutos: Int,
        nombrePersonalizado: String?
    ) {
        val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaHoraActual = formatoFechaHora.format(Date())

        val nombreFinal = if (!nombrePersonalizado.isNullOrBlank()) {
            nombrePersonalizado.trim()
        } else {
            "Partido $contadorPartidos".also { contadorPartidos++ }
        }

        val nuevoPartido = Partido(
            nombre = nombreFinal,
            equipo1 = equipo1,
            equipo2 = equipo2,
            duracionMinutos = duracionMinutos,
            fechaHoraInicio = fechaHoraActual
        )

        _partidoActual.value = nuevoPartido
        _tiempoRestanteSegundos.value = duracionMinutos * 60
        _cronometroEnPausa.value = true
        _ultimoGolCelebracion.value = null

        detenerCronometro()
    }

    fun alternarPausaCronometro() {
        if (_cronometroEnPausa.value) {
            reanudarCronometro()
        } else {
            pausarCronometro()
        }
    }

    private fun reanudarCronometro() {
        _cronometroEnPausa.value = false
        jobCronometro?.cancel()
        jobCronometro = viewModelScope.launch {
            while (_tiempoRestanteSegundos.value > 0 && !_cronometroEnPausa.value) {
                delay(1000L)
                _tiempoRestanteSegundos.update { maxOf(0, it - 1) }
            }
            if (_tiempoRestanteSegundos.value == 0) {
                _cronometroEnPausa.value = true
                finalizarTiempoReglamentario()
            }
        }
    }

    private fun pausarCronometro() {
        _cronometroEnPausa.value = true
        jobCronometro?.cancel()
    }

    private fun detenerCronometro() {
        _cronometroEnPausa.value = true
        jobCronometro?.cancel()
        jobCronometro = null
    }

    fun anotarGol(equipoId: String, jugador: Jugador) {
        val partido = _partidoActual.value ?: return
        val duracionTotalSegundos = partido.duracionMinutos * 60
        val segundosJugados = duracionTotalSegundos - _tiempoRestanteSegundos.value
        val minutoActual = maxOf(1, (segundosJugados / 60) + 1)

        val nuevoGol = Gol(
            jugadorId = jugador.id,
            jugadorNombre = jugador.nombre,
            equipoId = equipoId,
            minuto = minutoActual
        )

        _partidoActual.update { it?.copy(goles = it.goles + nuevoGol) }
        _ultimoGolCelebracion.value = nuevoGol
    }

    fun descartarCelebracionGol() {
        _ultimoGolCelebracion.value = null
    }

    fun deshacerUltimoGol() {
        _partidoActual.update { partido ->
            if (partido != null && partido.goles.isNotEmpty()) {
                partido.copy(goles = partido.goles.dropLast(1))
            } else {
                partido
            }
        }
        _ultimoGolCelebracion.value = null
    }

    fun finalizarTiempoReglamentario() {
        detenerCronometro()
        val partido = _partidoActual.value ?: return

        if (partido.golesEquipo1 == partido.golesEquipo2) {
            _partidoActual.update {
                it?.copy(estadoPenaltis = EstadoPenaltis())
            }
        } else {
            val ganadorId = if (partido.golesEquipo1 > partido.golesEquipo2) partido.equipo1.id else partido.equipo2.id
            val partidoTerminado = partido.copy(
                terminado = true,
                ganadorEquipoId = ganadorId
            )
            _partidoActual.value = partidoTerminado
            guardarEnHistorial(partidoTerminado)
        }
    }

    fun registrarLanzamientoPenalti(marcado: Boolean) {
        val partido = _partidoActual.value ?: return
        val estadoActual = partido.estadoPenaltis ?: return
        if (estadoActual.tandaTerminada) return

        val turn1 = estadoActual.turnoEquipo1
        val nuevosTiros1 = if (turn1) estadoActual.lanzamientosEquipo1 + marcado else estadoActual.lanzamientosEquipo1
        val nuevosTiros2 = if (!turn1) estadoActual.lanzamientosEquipo2 + marcado else estadoActual.lanzamientosEquipo2

        val goles1 = nuevosTiros1.count { it }
        val goles2 = nuevosTiros2.count { it }

        var terminada = false
        var ganadorId: String? = null
        var muerteSubita = estadoActual.esMuerteSubita

        if (!muerteSubita) {
            val restantes1 = maxOf(0, 3 - nuevosTiros1.size)
            val restantes2 = maxOf(0, 3 - nuevosTiros2.size)

            if (goles1 > goles2 + restantes2) {
                terminada = true
                ganadorId = partido.equipo1.id
            } else if (goles2 > goles1 + restantes1) {
                terminada = true
                ganadorId = partido.equipo2.id
            } else if (nuevosTiros1.size >= 3 && nuevosTiros2.size >= 3) {
                if (goles1 == goles2) {
                    muerteSubita = true
                } else {
                    terminada = true
                    ganadorId = if (goles1 > goles2) partido.equipo1.id else partido.equipo2.id
                }
            }
        } else {
            if (nuevosTiros1.size == nuevosTiros2.size) {
                if (goles1 != goles2) {
                    terminada = true
                    ganadorId = if (goles1 > goles2) partido.equipo1.id else partido.equipo2.id
                }
            }
        }

        val nuevoEstado = estadoActual.copy(
            lanzamientosEquipo1 = nuevosTiros1,
            lanzamientosEquipo2 = nuevosTiros2,
            turnoEquipo1 = !turn1,
            tandaTerminada = terminada,
            ganadorEquipoId = ganadorId,
            esMuerteSubita = muerteSubita
        )

        val partidoActualizado = partido.copy(
            estadoPenaltis = nuevoEstado,
            terminado = terminada,
            ganadorEquipoId = ganadorId
        )

        _partidoActual.value = partidoActualizado

        if (terminada) {
            guardarEnHistorial(partidoActualizado)
        }
    }

    private fun guardarEnHistorial(partido: Partido) {
        _historialPartidos.update { lista ->
            val nuevaLista = listOf(partido) + lista.filter { it.id != partido.id }
            guardarHistorialEnAlmacenamiento(nuevaLista)
            nuevaLista
        }
    }

    fun reiniciarParaNuevoPartido() {
        detenerCronometro()
        _partidoActual.value = null
        _tiempoRestanteSegundos.value = 0
        _ultimoGolCelebracion.value = null
    }

    private fun guardarEquiposEnAlmacenamiento(lista: List<Equipo>) {
        try {
            val jsonArray = JSONArray()
            lista.forEach { equipo ->
                val eqObj = JSONObject().apply {
                    put("id", equipo.id)
                    put("nombre", equipo.nombre)
                    put("colorHex", equipo.colorHex)
                    val jugArray = JSONArray()
                    equipo.jugadores.forEach { jug ->
                        val jObj = JSONObject().apply {
                            put("id", jug.id)
                            put("nombre", jug.nombre)
                            put("esTitular", jug.esTitular)
                        }
                        jugArray.put(jObj)
                    }
                    put("jugadores", jugArray)
                }
                jsonArray.put(eqObj)
            }
            sharedPreferences.edit().putString("equipos_guardados", jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    private fun cargarEquiposDeAlmacenamiento(): List<Equipo> {
        val jsonString = sharedPreferences.getString("equipos_guardados", null)
        if (jsonString.isNullOrBlank()) {
            return crearEquiposIniciales()
        }
        return try {
            val jsonArray = JSONArray(jsonString)
            val resultado = mutableListOf<Equipo>()
            for (i in 0 until jsonArray.length()) {
                val eqObj = jsonArray.getJSONObject(i)
                val id = eqObj.getString("id")
                val nombre = eqObj.getString("nombre")
                val colorHex = eqObj.getLong("colorHex")
                val jugArray = eqObj.getJSONArray("jugadores")
                val jugadores = mutableListOf<Jugador>()
                for (j in 0 until jugArray.length()) {
                    val jObj = jugArray.getJSONObject(j)
                    jugadores.add(
                        Jugador(
                            id = jObj.getString("id"),
                            nombre = jObj.getString("nombre"),
                            esTitular = jObj.getBoolean("esTitular")
                        )
                    )
                }
                resultado.add(Equipo(id = id, nombre = nombre, colorHex = colorHex, jugadores = jugadores))
            }
            if (resultado.isEmpty()) crearEquiposIniciales() else resultado
        } catch (_: Exception) {
            crearEquiposIniciales()
        }
    }

    private fun guardarHistorialEnAlmacenamiento(lista: List<Partido>) {
        try {
            val jsonArray = JSONArray()
            lista.forEach { partido ->
                val pObj = JSONObject().apply {
                    put("id", partido.id)
                    put("nombre", partido.nombre)
                    put("duracionMinutos", partido.duracionMinutos)
                    put("fechaHoraInicio", partido.fechaHoraInicio)
                    put("terminado", partido.terminado)
                    put("ganadorEquipoId", partido.ganadorEquipoId ?: "")

                    val eq1Obj = JSONObject().apply {
                        put("id", partido.equipo1.id)
                        put("nombre", partido.equipo1.nombre)
                        put("colorHex", partido.equipo1.colorHex)
                    }
                    put("equipo1", eq1Obj)

                    val eq2Obj = JSONObject().apply {
                        put("id", partido.equipo2.id)
                        put("nombre", partido.equipo2.nombre)
                        put("colorHex", partido.equipo2.colorHex)
                    }
                    put("equipo2", eq2Obj)

                    val golesArray = JSONArray()
                    partido.goles.forEach { gol ->
                        val gObj = JSONObject().apply {
                            put("id", gol.id)
                            put("jugadorId", gol.jugadorId)
                            put("jugadorNombre", gol.jugadorNombre)
                            put("equipoId", gol.equipoId)
                            put("minuto", gol.minuto)
                        }
                        golesArray.put(gObj)
                    }
                    put("goles", golesArray)

                    if (partido.estadoPenaltis != null) {
                        val pen = partido.estadoPenaltis
                        val penObj = JSONObject().apply {
                            put("tandaTerminada", pen.tandaTerminada)
                            put("ganadorEquipoId", pen.ganadorEquipoId ?: "")
                            put("esMuerteSubita", pen.esMuerteSubita)
                            val t1Array = JSONArray()
                            pen.lanzamientosEquipo1.forEach { t1Array.put(it) }
                            put("lanzamientosEquipo1", t1Array)
                            val t2Array = JSONArray()
                            pen.lanzamientosEquipo2.forEach { t2Array.put(it) }
                            put("lanzamientosEquipo2", t2Array)
                        }
                        put("estadoPenaltis", penObj)
                    }
                }
                jsonArray.put(pObj)
            }
            sharedPreferences.edit().putString("historial_partidos_guardado", jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }

    private fun cargarHistorialDeAlmacenamiento(): List<Partido> {
        val jsonString = sharedPreferences.getString("historial_partidos_guardado", null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val resultado = mutableListOf<Partido>()
            for (i in 0 until jsonArray.length()) {
                val pObj = jsonArray.getJSONObject(i)
                val eq1Obj = pObj.getJSONObject("equipo1")
                val eq1 = Equipo(
                    id = eq1Obj.getString("id"),
                    nombre = eq1Obj.getString("nombre"),
                    colorHex = eq1Obj.getLong("colorHex")
                )
                val eq2Obj = pObj.getJSONObject("equipo2")
                val eq2 = Equipo(
                    id = eq2Obj.getString("id"),
                    nombre = eq2Obj.getString("nombre"),
                    colorHex = eq2Obj.getLong("colorHex")
                )

                val golesArray = pObj.getJSONArray("goles")
                val goles = mutableListOf<Gol>()
                for (g in 0 until golesArray.length()) {
                    val gObj = golesArray.getJSONObject(g)
                    goles.add(
                        Gol(
                            id = gObj.getString("id"),
                            jugadorId = gObj.getString("jugadorId"),
                            jugadorNombre = gObj.getString("jugadorNombre"),
                            equipoId = gObj.getString("equipoId"),
                            minuto = gObj.getInt("minuto")
                        )
                    )
                }

                val estadoPenaltis = if (pObj.has("estadoPenaltis")) {
                    val penObj = pObj.getJSONObject("estadoPenaltis")
                    val t1Array = penObj.getJSONArray("lanzamientosEquipo1")
                    val lanzamientos1 = (0 until t1Array.length()).map { t1Array.getBoolean(it) }
                    val t2Array = penObj.getJSONArray("lanzamientosEquipo2")
                    val lanzamientos2 = (0 until t2Array.length()).map { t2Array.getBoolean(it) }
                    EstadoPenaltis(
                        lanzamientosEquipo1 = lanzamientos1,
                        lanzamientosEquipo2 = lanzamientos2,
                        tandaTerminada = penObj.optBoolean("tandaTerminada", true),
                        ganadorEquipoId = penObj.optString("ganadorEquipoId").ifEmpty { null },
                        esMuerteSubita = penObj.optBoolean("esMuerteSubita", false)
                    )
                } else null

                resultado.add(
                    Partido(
                        id = pObj.getString("id"),
                        nombre = pObj.getString("nombre"),
                        equipo1 = eq1,
                        equipo2 = eq2,
                        duracionMinutos = pObj.getInt("duracionMinutos"),
                        fechaHoraInicio = pObj.getString("fechaHoraInicio"),
                        goles = goles,
                        estadoPenaltis = estadoPenaltis,
                        terminado = pObj.optBoolean("terminado", true),
                        ganadorEquipoId = pObj.optString("ganadorEquipoId").ifEmpty { null }
                    )
                )
            }
            resultado
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun crearEquiposIniciales(): List<Equipo> {
        return listOf(
            Equipo(
                nombre = "Los Galácticos",
                colorHex = 0xFF2563EB,
                jugadores = listOf(
                    Jugador(nombre = "Carlos (P)", esTitular = true),
                    Jugador(nombre = "Marcos", esTitular = true),
                    Jugador(nombre = "Dani", esTitular = true),
                    Jugador(nombre = "Álex", esTitular = true),
                    Jugador(nombre = "Lucas", esTitular = true),
                    Jugador(nombre = "Mateo", esTitular = false),
                    Jugador(nombre = "Hugo", esTitular = false)
                )
            ),
            Equipo(
                nombre = "Furia Roja",
                colorHex = 0xFFEF4444,
                jugadores = listOf(
                    Jugador(nombre = "Rubén (P)", esTitular = true),
                    Jugador(nombre = "Javi", esTitular = true),
                    Jugador(nombre = "Sergio", esTitular = true),
                    Jugador(nombre = "Adrián", esTitular = true),
                    Jugador(nombre = "Pablo", esTitular = true),
                    Jugador(nombre = "Diego", esTitular = false)
                )
            )
        )
    }
}
