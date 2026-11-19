package com.example.befubol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.befubol.ui.navigation.Destino
import com.example.befubol.ui.screens.PantallaEquipos
import com.example.befubol.ui.screens.PantallaHistorial
import com.example.befubol.ui.screens.PantallaPartidoEnCurso
import com.example.befubol.ui.screens.PantallaPenaltis
import com.example.befubol.ui.screens.PantallaPrepararPartido
import com.example.befubol.ui.screens.PantallaResumen
import com.example.befubol.ui.theme.BeFubolTheme
import com.example.befubol.ui.viewmodel.PachangaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeFubolTheme {
                val navController = rememberNavController()
                val sharedViewModel: PachangaViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = Destino.Equipos.ruta
                ) {
                    composable(Destino.Equipos.ruta) {
                        PantallaEquipos(
                            viewModel = sharedViewModel,
                            onNavegarAPrepararPartido = {
                                navController.navigate(Destino.PrepararPartido.ruta)
                            },
                            onNavegarAHistorial = {
                                navController.navigate(Destino.Historial.ruta)
                            }
                        )
                    }

                    composable(Destino.PrepararPartido.ruta) {
                        PantallaPrepararPartido(
                            viewModel = sharedViewModel,
                            onVolver = { navController.popBackStack() },
                            onPartidoIniciado = {
                                navController.navigate(Destino.PartidoEnCurso.ruta) {
                                    popUpTo(Destino.Equipos.ruta) { inclusive = false }
                                }
                            }
                        )
                    }

                    composable(Destino.PartidoEnCurso.ruta) {
                        PantallaPartidoEnCurso(
                            viewModel = sharedViewModel,
                            onPartidoFinalizado = {
                                navController.navigate(Destino.Resumen.ruta) {
                                    popUpTo(Destino.PartidoEnCurso.ruta) { inclusive = true }
                                }
                            },
                            onIrAPenaltis = {
                                navController.navigate(Destino.Penaltis.ruta) {
                                    popUpTo(Destino.PartidoEnCurso.ruta) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Destino.Penaltis.ruta) {
                        PantallaPenaltis(
                            viewModel = sharedViewModel,
                            onIrAResumen = {
                                navController.navigate(Destino.Resumen.ruta) {
                                    popUpTo(Destino.Penaltis.ruta) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Destino.Resumen.ruta) {
                        PantallaResumen(
                            viewModel = sharedViewModel,
                            onNuevoPartido = {
                                navController.navigate(Destino.PrepararPartido.ruta) {
                                    popUpTo(Destino.Equipos.ruta) { inclusive = false }
                                }
                            },
                            onVerHistorial = {
                                navController.navigate(Destino.Historial.ruta)
                            },
                            onSalirAlMenu = {
                                navController.navigate(Destino.Equipos.ruta) {
                                    popUpTo(Destino.Equipos.ruta) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Destino.Historial.ruta) {
                        PantallaHistorial(
                            viewModel = sharedViewModel,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
