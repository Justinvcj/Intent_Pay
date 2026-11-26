package com.intentpay.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Brand palette: one muted sage-green accent + iOS-style neutrals.
// Light and dark are kept at parity; Theme.kt maps these to Material3 roles.
// ---------------------------------------------------------------------------

// Light
val AccentLight = Color(0xFF587A4F)               // primary
val OnAccentLight = Color(0xFFFFFFFF)
val AccentContainerLight = Color(0xFFDDE8D4)
val OnAccentContainerLight = Color(0xFF1B3016)
val SecondaryLight = Color(0xFF5F6B66)
val OnSecondaryLight = Color(0xFFFFFFFF)
val TertiaryLight = Color(0xFF9A6A1F)             // amber, used for "needs attention"
val OnTertiaryLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF2F2F7)
val OnBackgroundLight = Color(0xFF1C1C1E)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1C1C1E)
val SurfaceVariantLight = Color(0xFFE9E9EE)
val OnSurfaceVariantLight = Color(0xFF6C6C70)
val OutlineLight = Color(0xFF8E8E93)
val OutlineVariantLight = Color(0xFFD1D1D6)
val ErrorLight = Color(0xFFC73A32)
val OnErrorLight = Color(0xFFFFFFFF)
val InverseSurfaceLight = Color(0xFF2C2C2E)
val InverseOnSurfaceLight = Color(0xFFF2F2F7)
val InversePrimaryLight = Color(0xFFB8D4A4)
val ScrimLight = Color(0xFF000000)
val SurfaceTintLight = AccentLight

// Dark
val AccentDark = Color(0xFFB8D4A4)                // primary
val OnAccentDark = Color(0xFF1D3314)
val AccentContainerDark = Color(0xFF2F4A27)
val OnAccentContainerDark = Color(0xFFD3EBC0)
val SecondaryDark = Color(0xFFB5C2BB)
val OnSecondaryDark = Color(0xFF1F2A25)
val TertiaryDark = Color(0xFFE6C07B)
val OnTertiaryDark = Color(0xFF3A2800)
val BackgroundDark = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFF2F2F7)
val SurfaceDark = Color(0xFF1C1C1E)
val OnSurfaceDark = Color(0xFFF2F2F7)
val SurfaceVariantDark = Color(0xFF2C2C2E)
val OnSurfaceVariantDark = Color(0xFFAEAEB2)
val OutlineDark = Color(0xFF6E6E73)
val OutlineVariantDark = Color(0xFF38383A)
val ErrorDark = Color(0xFFFF6961)
val OnErrorDark = Color(0xFF3B0A07)
val InverseSurfaceDark = Color(0xFFE5E5EA)
val InverseOnSurfaceDark = Color(0xFF1C1C1E)
val InversePrimaryDark = Color(0xFF587A4F)
val ScrimDark = Color(0xFF000000)
val SurfaceTintDark = AccentDark

// ---------------------------------------------------------------------------
// Legacy tokens. Still referenced by screens that have not been redesigned yet
// (history, analytics, settings, glass components, ...). Remove once the last
// consumer is migrated to MaterialTheme.colorScheme.
// ---------------------------------------------------------------------------
// iOS-like Native Colors
val IosBlue = Color(0xFF0A84FF)
val IosGreen = Color(0xFF32D74B)
val IosRed = Color(0xFFFF453A)
val IosGray = Color(0xFF8E8E93)

// Dark Theme Colors
val BackgroundBlack = Color(0xFF000000)
val SurfaceDarkGray = Color(0xFF1C1C1E)
val SurfaceElevated = Color(0xFF2C2C2E)

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextSecondary = Color(0x99EBEBF5)
val DividerColor = Color(0x33FFFFFF)

// Glassmorphic Colors
val GlassSurface = Color(0x1AFFFFFF) // 10% white for translucent cards
val GlassBorder = Color(0x26FFFFFF)  // 15% white for borders
val GlassSurfaceHighlight = Color(0x33FFFFFF) // 20% white for active/pressed states

// Gradient Mesh Base Colors
val MeshDarkBg = Color(0xFF0F0F13) // Very deep dark background
val MeshAccent1 = Color(0xFF1C1C30) // Subtle deep purple/blue hue
val MeshAccent2 = Color(0xFF182A24) // Subtle deep green hue
