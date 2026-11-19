package com.example.befubol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.befubol.data.model.Equipo
import com.example.befubol.ui.theme.colorParaFondoOscuro
import com.example.befubol.ui.viewmodel.PachangaViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrepararPartido(
    viewModel: PachangaViewModel,
    onVolver: () -> Unit,
    onPartidoIniciado: () -> Unit
) {
    val listaEquipos by viewModel.equipos.collectAsState()

    var equipo1Seleccionado by remember { mutableStateOf(listaEquipos.getOrNull(0)) }
    var equipo2Seleccionado by remember { mutableStateOf(listaEquipos.getOrNull(1)) }

    var nombrePartido by remember { mutableStateOf("") }
    var duracionMinutos by remember { mutableIntStateOf(10) }
    var duracionPersonalizadaTexto by remember { mutableStateOf("") }
    var esDuracionPersonalizada by remember { mutableStateOf(false) }

    val puedeIniciar = equipo1Seleccionado != null &&
            equipo2Seleccionado != null &&
            equipo1Seleccionado?.id != equipo2Seleccionado?.id &&
            duracionMinutos > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preparar Partido") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            OutlinedTextField(
                value = nombrePartido,
                onValueChange = { nombrePartido = it },
                label = { Text("Nombre del partido (opcional, ej: Final)") },
                placeholder = { Text("Ej: Gran Partido del Domingo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            Text("Selecciona los 2 equipos:", style = MaterialTheme.typography.titleMedium)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                Box(modifier = Modifier.weight(1f)) {
                    SelectorEquipoDesplegable(
                        titulo = "Equipo Local",
                        equipos = listaEquipos,
                        equipoSeleccionado = equipo1Seleccionado,
                        onEquipoSeleccionado = { equipo1Seleccionado = it }
                    )
                }


                Box(modifier = Modifier.weight(1f)) {
                    SelectorEquipoDesplegable(
                        titulo = "Equipo Visitante",
                        equipos = listaEquipos,
                        equipoSeleccionado = equipo2Seleccionado,
                        onEquipoSeleccionado = { equipo2Seleccionado = it }
                    )
                }
            }

            if (equipo1Seleccionado != null && equipo2Seleccionado != null && equipo1Seleccionado?.id == equipo2Seleccionado?.id) {
                Text(
                    text = "⚠ Debes elegir dos equipos distintos.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Text("Duración del partido:", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(7, 10, 15).forEach { minutos ->
                    FilterChip(
                        selected = !esDuracionPersonalizada && duracionMinutos == minutos,
                        onClick = {
                            duracionMinutos = minutos
                            esDuracionPersonalizada = false
                        },
                        label = { Text("$minutos min") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = esDuracionPersonalizada,
                    onClick = { esDuracionPersonalizada = true },
                    label = { Text("Personalizado") }
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (esDuracionPersonalizada) {
                    OutlinedTextField(
                        value = duracionPersonalizadaTexto,
                        onValueChange = {
                            duracionPersonalizadaTexto = it
                            val parsed = it.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                duracionMinutos = parsed
                            }
                        },
                        label = { Text("Minutos") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))


            Button(
                onClick = {
                    val e1 = equipo1Seleccionado ?: return@Button
                    val e2 = equipo2Seleccionado ?: return@Button
                    viewModel.iniciarPartido(
                        equipo1 = e1,
                        equipo2 = e2,
                        duracionMinutos = duracionMinutos,
                        nombrePersonalizado = nombrePartido
                    )
                    onPartidoIniciado()
                },
                enabled = puedeIniciar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("¡Comenzar Partido!", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorEquipoDesplegable(
    titulo: String,
    equipos: List<Equipo>,
    equipoSeleccionado: Equipo?,
    onEquipoSeleccionado: (Equipo) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it }
    ) {
        OutlinedTextField(
            value = equipoSeleccionado?.nombre ?: "Seleccionar",
            onValueChange = {},
            readOnly = true,
            label = { Text(titulo) },
            leadingIcon = {
                if (equipoSeleccionado != null) {
                    val colorEq = Color(equipoSeleccionado.colorHex)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(colorEq)
                            .border(1.dp, colorEq.colorParaFondoOscuro(), CircleShape)
                    )
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            singleLine = true,
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            equipos.forEach { equipo ->
                val colorEq = Color(equipo.colorHex)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(colorEq)
                                    .border(1.dp, colorEq.colorParaFondoOscuro(), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(equipo.nombre)
                        }
                    },
                    onClick = {
                        onEquipoSeleccionado(equipo)
                        expandido = false
                    }
                )
            }
        }
    }
}
