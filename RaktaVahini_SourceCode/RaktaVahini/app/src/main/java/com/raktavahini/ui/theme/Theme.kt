package com.raktavahini.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────────────────────────
val BloodRed       = Color(0xFFB71C1C)
val DeepRed        = Color(0xFF7F0000)
val MedRed         = Color(0xFFC62828)
val LightRed       = Color(0xFFFFEBEE)
val CardRed        = Color(0xFFFCE4EC)
val GoldAccent     = Color(0xFFFFB300)
val GoldLight      = Color(0xFFFFF8E1)
val SafeGreen      = Color(0xFF1B5E20)
val SafeGreenLight = Color(0xFFE8F5E9)
val NeutralGrey    = Color(0xFF757575)
val SurfaceWhite   = Color(0xFFFFFBFB)
val BackgroundTint = Color(0xFFFFF5F5)

private val RaktaColorScheme = lightColorScheme(
    primary             = BloodRed,
    onPrimary           = Color.White,
    primaryContainer    = LightRed,
    onPrimaryContainer  = DeepRed,
    secondary           = GoldAccent,
    onSecondary         = Color.White,
    secondaryContainer  = GoldLight,
    onSecondaryContainer = Color(0xFF4A3000),
    tertiary            = SafeGreen,
    onTertiary          = Color.White,
    tertiaryContainer   = SafeGreenLight,
    onTertiaryContainer = SafeGreen,
    background          = BackgroundTint,
    onBackground        = Color(0xFF1A0000),
    surface             = SurfaceWhite,
    onSurface           = Color(0xFF1A0000),
    surfaceVariant      = LightRed,
    error               = Color(0xFF9C0000),
    onError             = Color.White,
)

@Composable
fun RaktaVahiniTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RaktaColorScheme,
        typography  = Typography(),
        content     = content
    )
}
