package com.example.befubol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkSportsColorScheme = darkColorScheme(
    primary = VerdeCespedNeon,
    onPrimary = Color(0xFF003816),
    primaryContainer = VerdeOscuroContenedor,
    onPrimaryContainer = Color(0xFF69FFA4),

    secondary = OroTrofeo,
    onSecondary = Color(0xFF412D00),
    secondaryContainer = OroOscuroContenedor,
    onSecondaryContainer = Color(0xFFFFDF9E),

    tertiary = AzulEstadio,
    onTertiary = Color(0xFF003548),

    background = FondoPizarra,
    onBackground = TextoPrincipal,

    surface = SuperficieTarjeta,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,

    outline = BordeSutil,
    outlineVariant = Color(0xFF21262D),
    error = RojoTarjeta,
    onError = Color.White
)

@Composable
fun BeFubolTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkSportsColorScheme,
        content = content
    )
}
