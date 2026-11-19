package com.example.befubol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.befubol.ui.theme.colorParaFondoOscuro
import com.example.befubol.ui.theme.colorTextoContraste
import com.example.befubol.ui.theme.esColorClaro
import com.example.befubol.ui.viewmodel.PachangaViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaResumen(
    viewModel: PachangaViewModel,
    onNuevoPartido: () -> Unit,
    onVerHistorial: () -> Unit,
    onSalirAlMenu: () -> Unit
) {
    val partido by viewModel.partidoActual.collectAsState()
    val p = partido ?: return

    val equipoGanador = when (p.ganadorEquipoId) {
        p.equipo1.id -> p.equipo1
        p.equipo2.id -> p.equipo2
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen del Partido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onSalirAlMenu) {
                        Icon(Icons.Default.Home, contentDescription = "Menú Principal")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSalirAlMenu,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Menú")
                    }

                    OutlinedButton(
                        onClick = onVerHistorial,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Historial")
                    }

                    Button(
                        onClick = {
                            viewModel.reiniciarParaNuevoPartido()
                            onNuevoPartido()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revancha", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = p.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inicio: ${p.fechaHoraInicio} · ${p.duracionMinutos} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val colorEq1 = Color(p.equipo1.colorHex)
                        val colorVisible1 = colorEq1.colorParaFondoOscuro()
                        val colorEq2 = Color(p.equipo2.colorHex)
                        val colorVisible2 = colorEq2.colorParaFondoOscuro()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(colorEq1)
                                        .border(1.dp, colorVisible1, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = p.equipo1.nombre,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${p.golesEquipo1}",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = colorVisible1
                                )
                            }

                            Text(
                                text = "vs",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )


                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(colorEq2)
                                        .border(1.dp, colorVisible2, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = p.equipo2.nombre,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${p.golesEquipo2}",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = colorVisible2
                                )
                            }
                        }


                        val pen = p.estadoPenaltis
                        if (pen != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "Penaltis: ${p.equipo1.nombre} (${pen.golesEquipo1}) - (${pen.golesEquipo2}) ${p.equipo2.nombre}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        if (equipoGanador != null) {
                            val colorGanador = Color(equipoGanador.colorHex)
                            val colorGanadorVisible = colorGanador.colorParaFondoOscuro()
                            val esClaro = colorGanador.esColorClaro()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (esClaro) colorGanador else colorGanador.copy(alpha = 0.22f),
                                border = BorderStroke(1.5.dp, colorGanadorVisible)
                            ) {
                                Text(
                                    text = "🏆 Ganador: ${equipoGanador.nombre}",
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (esClaro) colorGanador.colorTextoContraste() else colorGanadorVisible
                                )
                            }
                        } else {
                            Text("Empate", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }


            item {
                Text(
                    text = "Goleadores del partido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (p.goles.isEmpty()) {
                item {
                    Text(
                        text = "No se marcaron goles durante el tiempo regular.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(p.goles) { gol ->
                    val equipo = if (gol.equipoId == p.equipo1.id) p.equipo1 else p.equipo2
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
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(equipo.colorHex))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚽ ${gol.jugadorNombre} - Minuto ${gol.minuto}'",
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

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
