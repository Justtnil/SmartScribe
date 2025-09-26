// File: app/src/main/java/com/example/smartscribe/ui/components/ThemeToggle.kt
package com.example.smartscribe.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartscribe.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeToggle(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Create a compact theme toggle with sun/moon icons
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sun icon (Light mode indicator)
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.WbSunny,
            contentDescription = "Light Mode",
            tint = if (currentMode == ThemeMode.LIGHT)
                Color.DarkGray else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        // Toggle switch
        Switch(
            checked = currentMode == ThemeMode.DARK,
            onCheckedChange = { isDark ->
                onModeChange(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Moon icon (Dark mode indicator)
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Nightlight,
            contentDescription = "Dark Mode",
            tint = if (currentMode == ThemeMode.DARK)
                Color.White else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}