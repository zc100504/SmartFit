package com.example.smartfit.ui.components // Make sure the package name is correct

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Note: 'private' has been removed to make these composables public and reusable

@Composable
fun FieldCard(
    label: String,
    modifier: Modifier = Modifier, // Added modifier parameter for more flexibility
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                content()
            }
        }
    }
}

@Composable
fun BorderlessInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, // Added modifier
    singleLine: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        ),
        modifier = modifier.fillMaxWidth(), // Use passed modifier
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
