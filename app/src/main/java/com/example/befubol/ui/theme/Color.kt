package com.example.befubol.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta oscura estilo marcador deportivo de estadio
val FondoPizarra = Color(0xFF0E1117)
val SuperficieTarjeta = Color(0xFF161B22)
val SuperficieVariante = Color(0xFF212836)
val BordeSutil = Color(0xFF30363D)

// Acentos deportivos
val VerdeCespedNeon = Color(0xFF00E676)
val VerdeOscuroContenedor = Color(0xFF004D25)
val OroTrofeo = Color(0xFFFFB300)
val OroOscuroContenedor = Color(0xFF4A3400)
val AzulEstadio = Color(0xFF38BDF8)
val RojoTarjeta = Color(0xFFFF5252)

// Textos
val TextoPrincipal = Color(0xFFF0F6FC)
val TextoSecundario = Color(0xFF8B949E)

/**
 * Determina si un color es claro en base a la fórmula estándar de luminancia percibida relativa.
 */
fun Color.esColorClaro(): Boolean {
    val luminancia = 0.299f * red + 0.587f * green + 0.114f * blue
    return luminancia > 0.5f
}

/**
 * Devuelve un color de texto con máximo contraste para dibujar sobre un fondo de este color:
 * - Si el fondo es claro (blanco, amarillo, cian claro), devuelve texto negro/oscuro.
 * - Si el fondo es oscuro (azul, rojo, negro, verde), devuelve texto blanco.
 */
fun Color.colorTextoContraste(): Color = if (esColorClaro()) Color(0xFF0E1117) else Color.White

/**
 * Ajusta colores que son demasiado oscuros (como negro o azul noche) para que se vean nítidos
 * cuando se muestran como texto o números sobre el fondo oscuro de la aplicación.
 */
fun Color.colorParaFondoOscuro(): Color {
    val luminancia = 0.299f * red + 0.587f * green + 0.114f * blue
    return if (luminancia < 0.28f) Color(0xFFE2E8F0) else this
}
