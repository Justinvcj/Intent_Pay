package com.intentpay.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// One neutral ramp (Apple-style grouped greys) plus a single sage accent.
// Surfaces stay neutral; the accent marks the primary action, the selection
// and the "auto-detected" dot. Dark is the primary design target and light is
// kept at parity. Theme.kt maps these to Material3 roles.
//
// Contrast (computed from the hex values):
//   dark  onSurface        #F2F2F7 on #1C1C1E  16.0:1
//   dark  onSurfaceVariant #AEAEB2 on #1C1C1E   7.7:1
//   dark  primary          #B8D4A4 on #1C1C1E   9.6:1
//   light onSurfaceVariant #636366 on #FFFFFF   6.0:1
//   light primary          #4F7046 on #FFFFFF   5.9:1
// ---------------------------------------------------------------------------

// Dark
val AccentDark = Color(0xFFB8D4A4)
val OnAccentDark = Color(0xFF14230F)
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
val OutlineDark = Color(0xFF8E8E93)
val OutlineVariantDark = Color(0xFF38383A)
val ErrorDark = Color(0xFFFF6961)
val OnErrorDark = Color(0xFF3B0A07)
val InverseSurfaceDark = Color(0xFFE5E5EA)
val InverseOnSurfaceDark = Color(0xFF1C1C1E)
val InversePrimaryDark = Color(0xFF4F7046)
val ScrimDark = Color(0xFF000000)
val SurfaceTintDark = AccentDark
// Neutral container steps so no Material baseline purple can leak in.
val SurfaceContainerLowestDark = Color(0xFF000000)
val SurfaceContainerLowDark = Color(0xFF141416)
val SurfaceContainerDark = Color(0xFF1C1C1E)
val SurfaceContainerHighDark = Color(0xFF2C2C2E)
val SurfaceContainerHighestDark = Color(0xFF38383A)
val TertiaryContainerDark = Color(0xFF4A3A14)
val OnTertiaryContainerDark = Color(0xFFF3DDB0)
val ErrorContainerDark = Color(0xFF5C1F1B)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// Light: a faintly warm paper background under white cards.
val AccentLight = Color(0xFF4F7046)
val OnAccentLight = Color(0xFFFFFFFF)
val AccentContainerLight = Color(0xFFDDE8D4)
val OnAccentContainerLight = Color(0xFF1B3016)
val SecondaryLight = Color(0xFF5F6B66)
val OnSecondaryLight = Color(0xFFFFFFFF)
val TertiaryLight = Color(0xFF8A5C14)
val OnTertiaryLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF4F4F2)
val OnBackgroundLight = Color(0xFF1C1C1E)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1C1C1E)
val SurfaceVariantLight = Color(0xFFEBEBE8)
val OnSurfaceVariantLight = Color(0xFF636366)
val OutlineLight = Color(0xFF8E8E93)
val OutlineVariantLight = Color(0xFFD8D8D4)
val ErrorLight = Color(0xFFC73A32)
val OnErrorLight = Color(0xFFFFFFFF)
val InverseSurfaceLight = Color(0xFF2C2C2E)
val InverseOnSurfaceLight = Color(0xFFF2F2F7)
val InversePrimaryLight = Color(0xFFB8D4A4)
val ScrimLight = Color(0xFF000000)
val SurfaceTintLight = AccentLight
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF9F9F7)
val SurfaceContainerLight = Color(0xFFFFFFFF)
val SurfaceContainerHighLight = Color(0xFFEBEBE8)
val SurfaceContainerHighestLight = Color(0xFFE2E2DE)
val TertiaryContainerLight = Color(0xFFF3DDB0)
val OnTertiaryContainerLight = Color(0xFF3A2800)
val ErrorContainerLight = Color(0xFFFADAD6)
val OnErrorContainerLight = Color(0xFF5C1F1B)
