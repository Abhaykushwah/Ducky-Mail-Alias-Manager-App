package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Simple Light Palette
val SimpleLightBackground = Color(0xFFF8FAFC) // Clean slate off-white canvas
val SimpleLightSurface = Color(0xFFFFFFFF)    // Pure white card surface
val SimpleLightSurfaceVariant = Color(0xFFF1F5F9) // Soft gray container
val SimpleLightBorder = Color(0xFFE2E8F0)     // Divider border
val SimpleLightTextPrimary = Color(0xFF0F172A) // Slate-900 primary text
val SimpleLightTextSecondary = Color(0xFF475569) // Slate-600 secondary text
val SimpleLightTextMuted = Color(0xFF94A3B8)   // Slate-400 muted text
val SimpleLightPrimary = Color(0xFF059669)     // Duck emerald primary light
val SimpleLightSecondary = Color(0xFF0891B2)   // Cyan secondary light

// Simple Dark Palette
val SimpleDarkBackground = Color(0xFF121212)   // Simple true dark canvas
val SimpleDarkSurface = Color(0xFF1E1E1E)      // Charcoal card surface
val SimpleDarkSurfaceVariant = Color(0xFF2B2B2B) // Dark input container
val SimpleDarkBorder = Color(0xFF383838)       // Dark border
val SimpleDarkTextPrimary = Color(0xFFF8FAFC)  // Crisp white primary text
val SimpleDarkTextSecondary = Color(0xFFCBD5E1) // Soft gray secondary text
val SimpleDarkTextMuted = Color(0xFF94A3B8)    // Muted text
val SimpleDarkPrimary = Color(0xFF10B981)      // Bright emerald primary dark
val SimpleDarkSecondary = Color(0xFF06B6D4)    // Bright cyan secondary dark

// Status indicator colors
val StatusActiveGreen = Color(0xFF22C55E)
val StatusActiveGreenBg = Color(0x2022C55E)
val StatusDeactiveRed = Color(0xFFEF4444)
val StatusDeactiveRedBg = Color(0x20EF4444)

// Legacy alias definitions mapped to Simple Dark defaults for backward compatibility
val ProtonDarkCanvas = SimpleDarkBackground
val ProtonDarkCard = SimpleDarkSurface
val ProtonDarkCardBorder = SimpleDarkBorder
val ProtonInputBg = SimpleDarkSurfaceVariant
val DuckEmeraldPrimary = SimpleDarkPrimary
val DuckEmeraldLight = Color(0xFF34D399)
val DuckEmeraldDark = Color(0xFF059669)
val DuckCyanSecondary = SimpleDarkSecondary
val ProtonPurpleAccent = Color(0xFF8B5CF6)
val TextPrimaryDark = SimpleDarkTextPrimary
val TextSecondaryDark = SimpleDarkTextSecondary
val TextMutedDark = SimpleDarkTextMuted
val DuckOrangeBeak = Color(0xFFF59E0B)
