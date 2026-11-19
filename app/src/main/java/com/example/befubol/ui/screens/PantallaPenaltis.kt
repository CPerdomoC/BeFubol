package com.example.befubol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.befubol.data.model.Equipo
import com.example.befubol.ui.theme.colorParaFondoOscuro
import com.example.befubol.ui.viewmodel.PachangaViewModel

/**
 * Pantalla para la tanda de penaltis:
 * 3 penaltis iniciales por equipo, finalización anticipada por ventaja matemática y muerte súbita.
 * Aplica lógica de luminancia para colores de equipos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPenaltis(
    viewModel: PachangaViewModel,
    onIrAResumen: () -> Unit
) {
    val partido by viewModel.partidoActual.collectAsState()
    val p = partido ?: return
    val estadoPenaltis = p.estadoPenaltis ?: return

    val equipoEnTurno = if (estadoPenaltis.turnoEquipo1) p.equipo1 else p.equipo2
    val colorTurnoVisible = Color(equipoEnTurno.colorHex).colorParaFondoOscuro()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tanda de Penaltis", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Título y Marcador de Penaltis
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (estadoPenaltis.esMuerteSubita && !estadoPenaltis.tandaTerminada) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("⚡ ¡MUERTE SÚBITA! ⚡", fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Marcador grande de penaltis
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColumnaEquipoPenaltis(
                        equipo = p.equipo1,
                        golesPenaltis = estadoPenaltis.golesEquipo1,
                        lanzamientos = estadoPenaltis.lanzamientosEquipo1,
                        esTurno = !estadoPenaltis.tandaTerminada && estadoPenaltis.turnoEquipo1
                    )

                    Text("vs", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    ColumnaEquipoPenaltis(
                        equipo = p.equipo2,
                        golesPenaltis = estadoPenaltis.golesEquipo2,
                        lanzamientos = estadoPenaltis.lanzamientosEquipo2,
                        esTurno = !estadoPenaltis.tandaTerminada && !estadoPenaltis.turnoEquipo1
                    )
                }
            }

            // Sección central: Turno actual o Ganador
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (estadoPenaltis.tandaTerminada)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!estadoPenaltis.tandaTerminada) {
                        Text(
                            text = "Turno de lanzamiento:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(equipoEnTurno.colorHex))
                                    .border(1.dp, colorTurnoVisible, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = equipoEnTurno.nombre,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorTurnoVisible
                            )
                        }
                    } else {
                        val equipoGanador = if (estadoPenaltis.ganadorEquipoId == p.equipo1.id) p.equipo1 else p.equipo2
                        val colorGanadorVisible = Color(equipoGanador.colorHex).colorParaFondoOscuro()
                        Text(
                            text = "🏆 ¡Tanda Finalizada!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ganador: ${equipoGanador.nombre}",
                            fontSize = 18.sp,
                            color = colorGanadorVisible,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Botones de acción: "GOL" o "FALLO" (sin emojis redundantes), o "Ver Resumen" si terminó
            if (!estadoPenaltis.tandaTerminada) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.registrarLanzamientoPenalti(marcado = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FALLO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.registrarLanzamientoPenalti(marcado = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Verde éxito
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GOL", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onIrAResumen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver Resumen Final", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ColumnaEquipoPenaltis(
    equipo: Equipo,
    golesPenaltis: Int,
    lanzamientos: List<Boolean>,
    esTurno: Boolean
) {
    val colorEq = Color(equipo.colorHex)
    val colorVisible = colorEq.colorParaFondoOscuro()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(130.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(colorEq)
                .border(1.dp, colorVisible, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = equipo.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (esTurno) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$golesPenaltis",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colorVisible
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Indicadores visuales circulares elegantes de lanzamientos
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val totalSlots = maxOf(3, lanzamientos.size)
            for (i in 0 until totalSlots) {
                if (i < lanzamientos.size) {
                    val gol = lanzamientos[i]
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (gol) Color(0xFF22C55E) else Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (gol) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
        }
    }
}
