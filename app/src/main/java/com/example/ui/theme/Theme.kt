package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class PsychologyTheme(
    val title: String,
    val description: String,
    val hexSample: String
) {
    COGNITIVE_BLUE("Cognitive Blue", "Stimulates mental clarity & analytical focus", "#0284C7"),
    SAGE_CONCENTRATION("Sage Green", "Reduces eye fatigue & supports sustained memory", "#0D9488"),
    LAVENDER_CALM("Lavender Calm", "Relieves test anxiety & inspires creative synthesis", "#7C3AED"),
    SOLAR_ENERGY("Solar Amber", "Boosts dopamine, motivation & morning momentum", "#D97706")
}

fun getPsychologyColorScheme(theme: PsychologyTheme, darkTheme: Boolean): ColorScheme {
    return when (theme) {
        PsychologyTheme.COGNITIVE_BLUE -> if (darkTheme) {
            darkColorScheme(
                primary = PrimaryDark,
                onPrimary = OnPrimaryDark,
                primaryContainer = PrimaryContainerDark,
                onPrimaryContainer = OnPrimaryContainerDark,
                secondary = SecondaryDark,
                onSecondary = OnSecondaryDark,
                tertiary = TertiaryDark,
                background = BackgroundDark,
                surface = SurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                outline = OutlineDark
            )
        } else {
            lightColorScheme(
                primary = PrimaryLight,
                onPrimary = OnPrimaryLight,
                primaryContainer = PrimaryContainerLight,
                onPrimaryContainer = OnPrimaryContainerLight,
                secondary = SecondaryLight,
                onSecondary = OnSecondaryLight,
                tertiary = TertiaryLight,
                background = BackgroundLight,
                surface = SurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                outline = OutlineLight
            )
        }
        PsychologyTheme.SAGE_CONCENTRATION -> if (darkTheme) {
            darkColorScheme(
                primary = SagePrimaryDark,
                onPrimary = BackgroundDark,
                primaryContainer = SagePrimaryContainerDark,
                onPrimaryContainer = SageOnPrimaryContainerDark,
                secondary = PrimaryDark,
                tertiary = TertiaryDark,
                background = BackgroundDark,
                surface = SurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                outline = OutlineDark
            )
        } else {
            lightColorScheme(
                primary = SagePrimaryLight,
                onPrimary = OnPrimaryLight,
                primaryContainer = SagePrimaryContainerLight,
                onPrimaryContainer = SageOnPrimaryContainerLight,
                secondary = PrimaryLight,
                tertiary = TertiaryLight,
                background = BackgroundLight,
                surface = SurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                outline = OutlineLight
            )
        }
        PsychologyTheme.LAVENDER_CALM -> if (darkTheme) {
            darkColorScheme(
                primary = LavenderPrimaryDark,
                onPrimary = BackgroundDark,
                primaryContainer = LavenderPrimaryContainerDark,
                onPrimaryContainer = LavenderOnPrimaryContainerDark,
                secondary = PrimaryDark,
                tertiary = TertiaryDark,
                background = BackgroundDark,
                surface = SurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                outline = OutlineDark
            )
        } else {
            lightColorScheme(
                primary = LavenderPrimaryLight,
                onPrimary = OnPrimaryLight,
                primaryContainer = LavenderPrimaryContainerLight,
                onPrimaryContainer = LavenderOnPrimaryContainerLight,
                secondary = PrimaryLight,
                tertiary = TertiaryLight,
                background = BackgroundLight,
                surface = SurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                outline = OutlineLight
            )
        }
        PsychologyTheme.SOLAR_ENERGY -> if (darkTheme) {
            darkColorScheme(
                primary = SolarPrimaryDark,
                onPrimary = BackgroundDark,
                primaryContainer = SolarPrimaryContainerDark,
                onPrimaryContainer = SolarOnPrimaryContainerDark,
                secondary = SecondaryDark,
                tertiary = TertiaryDark,
                background = BackgroundDark,
                surface = SurfaceDark,
                surfaceVariant = SurfaceVariantDark,
                outline = OutlineDark
            )
        } else {
            lightColorScheme(
                primary = SolarPrimaryLight,
                onPrimary = OnPrimaryLight,
                primaryContainer = SolarPrimaryContainerLight,
                onPrimaryContainer = SolarOnPrimaryContainerLight,
                secondary = SecondaryLight,
                tertiary = TertiaryLight,
                background = BackgroundLight,
                surface = SurfaceLight,
                surfaceVariant = SurfaceVariantLight,
                outline = OutlineLight
            )
        }
    }
}

@Composable
fun JarvisStudyTheme(
    psychologyTheme: PsychologyTheme = PsychologyTheme.COGNITIVE_BLUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getPsychologyColorScheme(psychologyTheme, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = JarvisStudyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
