package com.example.catalogohub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = Color.White,
    primaryContainer = VerdeContainer,
    onPrimaryContainer = VerdeSobreContainer,
    secondary = TurquesaSecundario,
    onSecondary = Color.White,
    secondaryContainer = TurquesaContainer,
    onSecondaryContainer = TurquesaSobreContainer,
    background = Color(0xFFF3F8F5),
    onBackground = Color(0xFF171D1A),
    surface = Color.White,
    onSurface = Color(0xFF171D1A),
    surfaceVariant = Color(0xFFDCE5DF),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707973),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6FAF8),
    surfaceContainer = Color(0xFFEEF4F0),
    surfaceContainerHigh = Color(0xFFE8EFEB),
    surfaceContainerHighest = Color(0xFFE2EAE5)
)

private val EsquemaEscuro = darkColorScheme(
    primary = VerdePrimarioEscuro,
    onPrimary = Color(0xFF003920),
    primaryContainer = VerdeContainerEscuro,
    onPrimaryContainer = VerdeSobreContainerEscuro,
    secondary = TurquesaSecundarioEscuro,
    onSecondary = Color(0xFF003735),
    secondaryContainer = TurquesaContainerEscuro,
    onSecondaryContainer = TurquesaSobreContainerEscuro,
    background = Color(0xFF0E1411),
    onBackground = Color(0xFFDEE4E0),
    surface = Color(0xFF161C19),
    onSurface = Color(0xFFDEE4E0),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C2),
    outline = Color(0xFF89938D),
    surfaceContainerLowest = Color(0xFF090F0C),
    surfaceContainerLow = Color(0xFF161C19),
    surfaceContainer = Color(0xFF1A211D),
    surfaceContainerHigh = Color(0xFF252B27),
    surfaceContainerHighest = Color(0xFF303632)
)

/** Tema do app: segue o modo claro/escuro do celular. */
@Composable
fun CatalogoHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaEscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}