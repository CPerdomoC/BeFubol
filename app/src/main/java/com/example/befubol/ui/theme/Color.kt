package com.example.befubol.ui.theme

import androidx.compose.ui.graphics.Color

val FondoPizarra = Color(0xFF0E1117)
val SuperficieTarjeta = Color(0xFF161B22)
val SuperficieVariante = Color(0xFF212836)
val BordeSutil = Color(0xFF30363D)

val VerdeCespedNeon = Color(0xFF00E676)
val VerdeOscuroContenedor = Color(0xFF004D25)
val OroTrofeo = Color(0xFFFFB300)
val OroOscuroContenedor = Color(0xFF4A3400)
val AzulEstadio = Color(0xFF38BDF8)
val RojoTarjeta = Color(0xFFFF5252)

val TextoPrincipal = Color(0xFFF0F6FC)
val TextoSecundario = Color(0xFF8B949E)

fun Color.esColorClaro(): Boolean {
    val luminancia = 0.299f * red + 0.587f * green + 0.114f * blue
    return luminancia > 0.5f
}

fun Color.colorTextoContraste(): Color = if (esColorClaro()) Color(0xFF0E1117) else Color.White

fun Color.colorParaFondoOscuro(): Color {
    val luminancia = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminancia < 0.28f) Color(0xFFE2E8F0) else this
}
