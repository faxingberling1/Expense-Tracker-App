package com.expenseos.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ExpenseOsColorScheme: ColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = Surface,
    secondary = Coral,
    onSecondary = Surface,
    tertiary = Gold,
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Mint,
    onSurfaceVariant = Pine
)

@Composable
fun ExpenseOsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ExpenseOsColorScheme,
        content = content
    )
}

