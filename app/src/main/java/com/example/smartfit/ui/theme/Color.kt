// app/src/main/java/com/example/smartfit/ui/theme/Color.kt
package com.example.smartfit.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------- Brand / Lime colors ----------------

val LimePrimary       = Color(0xFFB3FF00)  // main brand lime
val LimePrimarySoft   = Color(0x33B3FF00)  // 20% alpha, for gradients / glass

// Extra lime variants (charts / icons)
val Lime              = Color(0xFFCCFF00)
val LimeBright        = Color(0xFFD6FF4D)
val LimeSoft          = Color(0xFFEEFFBB)
val LimeDark          = Color(0xFFA6D921)

// Accent colors
val AccentRed         = Color(0xFFF97373)
val AccentYellow      = Color(0xFFFACC15)

// ---------------- Light theme base colors ----------------

val LightBackground    = Color(0xFFEFF2F5)  // app background
val LightSurface       = Color(0xFFFFFFFF)  // main card color
val LightSurfaceGlass  = Color(0xD9FFFFFF)  // 85% white glass
val LightSurfaceStrong = Color(0xFFFFFFFF)  // solid cards (can be same as surface)

val LightPrimary       = LimePrimary
val LightPrimarySoftAlias = LimePrimarySoft
val LightSecondary     = Color(0xFF22C55E)

val LightOnBackground  = Color(0xFF020617)
val LightOnSurface     = Color(0xFF050816)
val LightOutline       = Color(0x22020617)  // very subtle border

// ---------------- Dark theme base colors ----------------

val DarkBackground     = Color(0xFF020617)  // app background in dark mode
val DarkSurface        = Color(0xFF020617)
val DarkSurfaceGlass   = Color(0x6615253A)  // glass card in dark mode
val DarkSurfaceStrong  = Color(0xFF020617)

val DarkPrimary        = LimePrimary
val DarkPrimarySoftAlias = LimePrimarySoft
val DarkSecondary      = Color(0xFF34D399)

val DarkOnBackground   = Color(0xFFE5E7EB)
val DarkOnSurface      = Color(0xFFF9FAFB)
val DarkOutline        = Color(0x33E5E7EB)

// ---------------- ActivityStats background helpers ----------------

// Big gradient backgrounds (Activity Summary screen)
val DarkBg             = Color(0xFF050A18)
val DarkBgSoft         = Color(0xFF0C1224)

val LightBg            = Color(0xFFF4F6FB)
val LightBgSoft        = Color(0xFFEFF1F6)

// ---------------- Glass / chart / cards helpers ----------------

// Glass surface colors
val GlassLight         = Color.White.copy(alpha = 0.55f)
val GlassDark          = Color.White.copy(alpha = 0.08f)

// Glass borders
val GlassBorderLight   = Color.White.copy(alpha = 0.45f)
val GlassBorderDark    = Color.White.copy(alpha = 0.15f)

// Chart glow line
val ChartGlow          = Lime.copy(alpha = 0.85f)

// Dark cards slightly lighter than background
val DarkCard           = Color(0xFF0F172A)  // outer shells / chart container
val DarkCardElevated   = Color(0xFF111827)  // inner chart area, etc.

// ------------------ Tip Chat Colors -------------------

// User message bubble
val TipBubbleUserLight = Color(0xFFF1FFD6)      // light green-ish
val TipBubbleUserDark = Color(0xFF214000)        // deep green but soft

// Assistant message bubble
val TipBubbleBotLight = Color(0xFFF7F7F7)
val TipBubbleBotDark = Color(0xFF111827)

// Input bar background
val TipInputLight = Color(0xFFF0F7DF)
val TipInputDark = Color(0xFF1A1F2E)

// Divider / subtle border
val TipBorderLight = Color(0x22000000)
val TipBorderDark = Color(0x33FFFFFF)
