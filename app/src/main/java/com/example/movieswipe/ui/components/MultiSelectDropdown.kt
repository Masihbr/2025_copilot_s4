package com.example.movieswipe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    label: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (selectedOptions.isEmpty()) label else selectedOptions.joinToString(", ")
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = MaterialTheme.typography.bodyLarge,
                enabled = true,
                singleLine = true
            )
            // Overlay a clickable area to open the dropdown
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { expanded = true }
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                DropdownMenuItem(
                    text = {
                        Row {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null // handled by onClick
                            )
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    },
                    onClick = {
                        val newSelection = if (isSelected) {
                            selectedOptions - option
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChange(newSelection)
                    },
                    trailingIcon = null
                )
            }
        }
    }
}
