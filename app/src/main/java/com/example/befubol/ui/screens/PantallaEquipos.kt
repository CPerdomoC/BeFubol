package com.example.befubol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.befubol.data.model.Equipo
import com.example.befubol.data.model.Jugador
import com.example.befubol.data.model.OpcionColor
import com.example.befubol.ui.theme.colorParaFondoOscuro
import com.example.befubol.ui.theme.colorTextoContraste
import com.example.befubol.ui.viewmodel.PachangaViewModel

/**
 * Pantalla principal para gestionar los equipos y jugadores.
 * Permite crear, ver, editar, eliminar y sortear equipos aleatoriamente en N grupos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEquipos(
    viewModel: PachangaViewModel,
    onNavegarAPrepararPartido: () -> Unit,
    onNavegarAHistorial: () -> Unit
) {
    val listaEquipos by viewModel.equipos.collectAsState()
    var equipoParaEditar by remember { mutableStateOf<Equipo?>(null) }
    var mostrarDialogoCreacion by remember { mutableStateOf(false) }
    var mostrarDialogoSorteo by remember { mutableStateOf(false) }
    var menuFABExpandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚽", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BeFubol", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    TextButton(onClick = onNavegarAHistorial) {
                        Text("Historial", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { menuFABExpandido = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Opciones de equipo")
                }

                DropdownMenu(
                    expanded = menuFABExpandido,
                    onDismissRequest = { menuFABExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("➕ Crear equipo", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuFABExpandido = false
                            equipoParaEditar = null
                            mostrarDialogoCreacion = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🎲 Sortear equipos", fontWeight = FontWeight.Medium) },
                        onClick = {
                            menuFABExpandido = false
                            mostrarDialogoSorteo = true
                        }
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavegarAPrepararPartido,
                    enabled = listaEquipos.size >= 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (listaEquipos.size >= 2) "¡Preparar Partido!"
                        else "Crea o sortea al menos 2 equipos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValores ->
        if (listaEquipos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValores)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay equipos creados.\n\n¡Usa '🎲 Sortear' en la parte superior para repartir amigos al azar o pulsa en '+ Nuevo Equipo'!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValores)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Equipos registrados (${listaEquipos.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(listaEquipos, key = { it.id }) { equipo ->
                    TarjetaEquipo(
                        equipo = equipo,
                        onEditar = {
                            equipoParaEditar = equipo
                            mostrarDialogoCreacion = true
                        },
                        onEliminar = { viewModel.eliminarEquipo(equipo.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Diálogo para crear o editar equipo manual
        if (mostrarDialogoCreacion) {
            DialogoFormularioEquipo(
                equipoExistente = equipoParaEditar,
                opcionesColores = viewModel.opcionesColoresDisponibles,
                onGuardar = { nuevoEquipo ->
                    viewModel.guardarEquipo(nuevoEquipo)
                    mostrarDialogoCreacion = false
                },
                onCancelar = { mostrarDialogoCreacion = false }
            )
        }

        // Diálogo del Randomizador configurable (2, 3 o más equipos)
        if (mostrarDialogoSorteo) {
            DialogoSortearEquipos(
                onSortear = { listaNombres, numEquipos ->
                    viewModel.sortearEquipos(listaNombres, numEquipos)
                    mostrarDialogoSorteo = false
                },
                onCancelar = { mostrarDialogoSorteo = false }
            )
        }
    }
}

@Composable
fun TarjetaEquipo(
    equipo: Equipo,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorEq = Color(equipo.colorHex)
    val colorContraste = colorEq.colorParaFondoOscuro()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorContraste.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorEq)
                        .border(1.5.dp, colorContraste, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Titulares (${equipo.titulares.size}/11): ${equipo.titulares.joinToString { it.nombre }.ifEmpty { "Ninguno" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Suplentes (${equipo.suplentes.size}/5): ${equipo.suplentes.joinToString { it.nombre }.ifEmpty { "Ninguno" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DialogoFormularioEquipo(
    equipoExistente: Equipo?,
    opcionesColores: List<OpcionColor>,
    onGuardar: (Equipo) -> Unit,
    onCancelar: () -> Unit
) {
    var nombreEquipo by remember { mutableStateOf(equipoExistente?.nombre ?: "") }
    var colorSeleccionado by remember { mutableStateOf(equipoExistente?.colorHex ?: opcionesColores.first().colorHex) }
    var listaJugadores by remember { mutableStateOf(equipoExistente?.jugadores ?: emptyList()) }

    var nuevoJugadorNombre by remember { mutableStateOf("") }
    var nuevoJugadorEsTitular by remember { mutableStateOf(true) }

    val titulares = listaJugadores.filter { it.esTitular }
    val suplentes = listaJugadores.filter { !it.esTitular }

    val nombreValido = nombreEquipo.isNotBlank()
    val titularesValidos = titulares.size in 1..11
    val suplentesValidos = suplentes.size <= 5
    val formularioValido = nombreValido && titularesValidos && suplentesValidos

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (equipoExistente == null) "Nuevo Equipo" else "Editar Equipo") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nombreEquipo,
                        onValueChange = { nombreEquipo = it },
                        label = { Text("Nombre del equipo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Selecciona color del equipo:", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(opcionesColores) { opcion ->
                            val seleccionado = colorSeleccionado == opcion.colorHex
                            val colorOpcion = Color(opcion.colorHex)
                            val colorContraste = colorOpcion.colorParaFondoOscuro()
                            val colorCheck = colorOpcion.colorTextoContraste()

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (seleccionado) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { colorSeleccionado = opcion.colorHex }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(colorOpcion)
                                            .border(1.dp, colorContraste, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (seleccionado) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = colorCheck,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opcion.nombre,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Text(
                        text = "Jugadores (${titulares.size} titulares [mín 1, máx 11], ${suplentes.size} suplentes [máx 5])",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoJugadorNombre,
                            onValueChange = { nuevoJugadorNombre = it },
                            label = { Text("Nombre jugador") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (nuevoJugadorEsTitular) "Titular" else "Suplente", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = nuevoJugadorEsTitular,
                                onCheckedChange = { nuevoJugadorEsTitular = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (nuevoJugadorNombre.isNotBlank()) {
                                listaJugadores = listaJugadores + Jugador(
                                    nombre = nuevoJugadorNombre.trim(),
                                    esTitular = nuevoJugadorEsTitular
                                )
                                nuevoJugadorNombre = ""
                            }
                        },
                        enabled = nuevoJugadorNombre.isNotBlank() &&
                                (if (nuevoJugadorEsTitular) titulares.size < 11 else suplentes.size < 5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir Jugador")
                    }
                }

                items(listaJugadores) { jugador ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${jugador.nombre} (${if (jugador.esTitular) "Titular" else "Suplente"})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = { listaJugadores = listaJugadores.filter { it.id != jugador.id } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                if (!titularesValidos || !suplentesValidos) {
                    item {
                        Text(
                            text = "⚠ Debe tener entre 1 y 11 titulares (actual: ${titulares.size}) y máximo 5 suplentes (actual: ${suplentes.size}).",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val equipoGuardar = Equipo(
                        id = equipoExistente?.id ?: java.util.UUID.randomUUID().toString(),
                        nombre = nombreEquipo.trim(),
                        colorHex = colorSeleccionado,
                        jugadores = listaJugadores
                    )
                    onGuardar(equipoGuardar)
                },
                enabled = formularioValido
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para introducir la lista de amigos y sortearlos al azar en 2, 3 o más equipos equilibrados.
 */
@Composable
fun DialogoSortearEquipos(
    onSortear: (List<String>, Int) -> Unit,
    onCancelar: () -> Unit
) {
    var textoNombreJugador by remember { mutableStateOf("") }
    var listaAmigos by remember { mutableStateOf(listOf<String>()) }
    var numEquiposSeleccionado by remember { mutableIntStateOf(2) }

    val cantidad = listaAmigos.size
    val puedeSortear = cantidad >= numEquiposSeleccionado

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("🎲 Sortear Equipos al Azar") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Introduce los amigos que jugarán hoy. Puedes repartirlos en 2, 3 o más equipos según cuántos seáis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textoNombreJugador,
                            onValueChange = { textoNombreJugador = it },
                            label = { Text("Nombre del amigo") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (textoNombreJugador.isNotBlank()) {
                                    listaAmigos = listaAmigos + textoNombreJugador.trim()
                                    textoNombreJugador = ""
                                }
                            },
                            enabled = textoNombreJugador.isNotBlank() && cantidad < 40
                        ) {
                            Text("Añadir")
                        }
                    }
                }

                item {
                    Text("¿En cuántos equipos dividir?", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 4).forEach { n ->
                            FilterChip(
                                selected = numEquiposSeleccionado == n,
                                onClick = { numEquiposSeleccionado = n },
                                label = { Text("$n equipos") }
                            )
                        }
                    }
                }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val porEquipo = if (cantidad > 0) cantidad / numEquiposSeleccionado else 0
                        val sobran = if (cantidad > 0) cantidad % numEquiposSeleccionado else 0
                        val detalle = if (cantidad < numEquiposSeleccionado) {
                            "Añade al menos $numEquiposSeleccionado jugadores."
                        } else if (sobran == 0) {
                            "$cantidad jugadores ➔ $numEquiposSeleccionado equipos de $porEquipo jugadores cada uno"
                        } else {
                            "$cantidad jugadores ➔ $sobran equipos de ${porEquipo + 1} y ${numEquiposSeleccionado - sobran} equipos de $porEquipo"
                        }

                        Text(
                            text = detalle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (cantidad >= numEquiposSeleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                items(listaAmigos) { nombre ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("👤 $nombre", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { listaAmigos = listaAmigos - nombre }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSortear(listaAmigos, numEquiposSeleccionado)
                },
                enabled = puedeSortear
            ) {
                Text("¡Hacer Sorteo ($numEquiposSeleccionado eq)!")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
