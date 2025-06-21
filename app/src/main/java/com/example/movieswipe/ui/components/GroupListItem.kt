package com.example.movieswipe.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movieswipe.network.Group

@Composable
fun GroupListItem(
    group: Group,
    onClick: () -> Unit,
    menuItems: List<Pair<String, () -> Unit>> = emptyList()
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() },
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = group.invitationCode, style = MaterialTheme.typography.titleMedium)
                Text(text = "Owner: ${group.owner.name}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Created: ${group.createdAt}", style = MaterialTheme.typography.bodySmall)
            }
            if (menuItems.isNotEmpty()) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        menuItems.forEach { (label, action) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                expanded = false
                                action()
                            })
                        }
                    }
                }
            }
        }
    }
}
