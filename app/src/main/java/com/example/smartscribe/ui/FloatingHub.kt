// File: app/src/main/java/com/example/smartscribe/ui/FloatingHub.kt
package com.example.smartscribe.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingHub(
    onHomeClick: () -> Unit,
    onMicClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubButton(
                icon = Icons.Filled.Save,
                label = "Save",
                onClick = onSaveClick,
                color = Color(0xFF6200EE) // Purple
            )
            HubButton(
                icon = Icons.Filled.Mic,
                label = "Mic",
                onClick = onMicClick,
                color = Color(0xFF4CAF50) // Green
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubButton(
                icon = Icons.Filled.Home,
                label = "Home",
                onClick = onHomeClick,
                color = Color(0xFF2196F3) // Blue
            )
            HubButton(
                icon = Icons.Filled.SmartToy, // ✅ Replaced Robot with SmartToy
                label = "AI",
                onClick = onAiClick,
                color = Color.Gray,
                enabled = false
            )
        }
    }
}

@Composable
fun HubButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) color else Color.Gray,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (enabled) color else Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}
