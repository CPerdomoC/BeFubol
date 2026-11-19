package com.example.befubol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.befubol.data.model.Equipo
import com.example.befubol.data.model.Jugador
import com.example.befubol.ui.theme.colorParaFondoOscuro
import com.example.befubol.ui.theme.colorTextoContraste
import com.example.befubol.ui.theme.esColorClaro
import com.example.befubol.ui.viewmodel.PachangaViewModel
import java.util.Locale

/**
 * Pantalla principal del partido en juego:
 * Cronómetro digital, marcador simétrico de estadio, banner festivo de ¡GOOOL! con contraste inteligente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPartidoEnCurso(
    viewModel: PachangaViewModel,
    onPartidoFinalizado: () -> Unit,
    onIrAPenaltis: () -> Unit
) {
    val partido by viewModel.partidoActual.collectAsState()
    val tiempoSegundos by viewModel.tiempoRestanteSegundos.collectAsState()
    val enPausa by viewModel.cronometroEnPausa.collectAsState()
    val golCelebracion by viewModel.ultimoGolCelebracion.collectAsState()

    var equipoParaAnotar by remember { mutableStateOf<Equipo?>(null) }
    var mostrarConfirmacionTerminar by remember { mutableStateOf(false) }

    LaunchedEffect(partido?.terminado, partido?.estadoPenaltis) {
        val p = partido
        if (p != null) {
            if (p.estadoPenaltis != null && !p.terminado) {
                onIrAPenaltis()
            } else if (p.terminado) {
                onPartidoFinalizado()
            }
        }
    }

    val minutosFormateados = String.format(Locale.getDefault(), "%02d:%02d", tiempoSegundos / 60, tiempoSegundos % 60)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = partido?.nombre ?: "Partido en Vivo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    TextButton(onClick = { mostrarConfirmacionTerminar = true }) {
                        Text("Terminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        val p = partido
        if (p == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay partido en curso.")
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de Cronómetro Digital
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = minutosFormateados,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (tiempoSegundos <= 60 && tiempoSegundos > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.alternarPausaCronometro() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (enPausa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (enPausa) Icons.Default.PlayArrow else Icons.Default.Close,
                                contentDescription = if (enPausa) "Reanudar" else "Pausar"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (enPausa) "Iniciar / Reanudar" else "Pausar tiempo",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Marcador visual simétrico con control para nombres largos y contraste
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColumnaEquipoMarcador(
                        equipo = p.equipo1,
                        goles = p.golesEquipo1,
                        onAnotarGol = { equipoParaAnotar = p.equipo1 },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "VS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ColumnaEquipoMarcador(
                        equipo = p.equipo2,
                        goles = p.golesEquipo2,
                        onAnotarGol = { equipoParaAnotar = p.equipo2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Deshacer Gol
                if (p.goles.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.deshacerUltimoGol() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deshacer: ${p.goles.last().jugadorNombre} (${p.goles.last().minuto}')",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lista de goles anotados
                Text(
                    text = "Goles anotados (${p.goles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (p.goles.isEmpty()) {
                        item {
                            Text(
                                text = "Aún sin goles. ¡Pulsa '+ GOL' para abrir el marcador!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(p.goles.reversed()) { gol ->
                            val equipo = if (gol.equipoId == p.equipo1.id) p.equipo1 else p.equipo2
                            val colorEq = Color(equipo.colorHex)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(colorEq)
                                            .border(1.dp, colorEq.colorParaFondoOscuro(), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "⚽ ${gol.jugadorNombre} (Minuto ${gol.minuto}')",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = equipo.nombre,
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
            }

            // Banner animado de ¡¡GOOOOL!! con contraste inteligente
            AnimatedVisibility(
                visible = golCelebracion != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                val gol = golCelebracion
                if (gol != null) {
                    val eqGol = if (gol.equipoId == p.equipo1.id) p.equipo1 else p.equipo2
                    val colorFondo = Color(eqGol.colorHex)
                    val colorTexto = colorFondo.colorTextoContraste()

                    Surface(
                        color = colorFondo,
                        contentColor = colorTexto,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 10.dp,
                        border = BorderStroke(2.dp, colorTexto),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.descartarCelebracionGol() }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚽ ¡¡GOOOOOOL!! ⚽",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTexto
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${gol.jugadorNombre} anotó para ${eqGol.nombre} en el minuto ${gol.minuto}'",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = colorTexto
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "(Toca para cerrar)",
                                fontSize = 11.sp,
                                color = colorTexto.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Diálogo para elegir goleador
        val equipoAnotando = equipoParaAnotar
        if (equipoAnotando != null) {
            DialogoSeleccionarGoleador(
                equipo = equipoAnotando,
                onGoleadorSeleccionado = { jugador ->
                    viewModel.anotarGol(equipoAnotando.id, jugador)
                    equipoParaAnotar = null
                },
                onCancelar = { equipoParaAnotar = null }
            )
        }

        // Diálogo de confirmación para terminar partido manualmente
        if (mostrarConfirmacionTerminar) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacionTerminar = false },
                title = { Text("¿Terminar partido?") },
                text = { Text("Se detendrá el cronómetro. Si hay empate se pasará a la tanda de penaltis.") },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarConfirmacionTerminar = false
                            viewModel.finalizarTiempoReglamentario()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Terminar ahora")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacionTerminar = false }) {
                        Text("Continuar jugando")
                    }
                }
            )
        }
    }
}

/**
 * Columna de equipo en el marcador:
 * Aplica lógica de luminancia para garantizar que tanto equipos blancos como negros sean 100% legibles.
 */
@Composable
fun ColumnaEquipoMarcador(
    equipo: Equipo,
    goles: Int,
    onAnotarGol: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorOriginal = Color(equipo.colorHex)
    val colorParaFondo = colorOriginal.colorParaFondoOscuro()
    val colorTextoBoton = colorOriginal.colorTextoContraste()

    Card(
        modifier = modifier
            .border(
                border = BorderStroke(1.5.dp, colorParaFondo.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colorOriginal)
                    .border(1.dp, colorParaFondo, CircleShape)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Nombre con altura mínima y elipsis controlada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Panel de puntuación estilo LED con contraste inteligente
            Surface(
                color = if (colorOriginal.esColorClaro()) Color(0xFF232A36) else colorOriginal.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, colorParaFondo.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "$goles",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = colorParaFondo,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón + GOL con color de texto dinámico (negro para fondo blanco, blanco para fondos oscuros)
            Button(
                onClick = onAnotarGol,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorOriginal,
                    contentColor = colorTextoBoton
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = colorTextoBoton,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GOL",
                    fontWeight = FontWeight.Bold,
                    color = colorTextoBoton
                )
            }
        }
    }
}

@Composable
fun DialogoSeleccionarGoleador(
    equipo: Equipo,
    onGoleadorSeleccionado: (Jugador) -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Quién marcó el gol para ${equipo.nombre}?") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                val todosLosJugadores = equipo.jugadores.ifEmpty {
                    listOf(Jugador(nombre = "Gol general / Propia puerta"))
                }
                items(todosLosJugadores) { jugador ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = jugador.nombre,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = { Text(if (jugador.esTitular) "Titular" else "Suplente") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingContent = {
                            Button(onClick = { onGoleadorSeleccionado(jugador) }) {
                                Text("Anotar")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
