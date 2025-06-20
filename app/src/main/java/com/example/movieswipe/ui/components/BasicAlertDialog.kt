package com.example.movieswipe.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun BasicAlertDialog(
    show: Boolean,
    title: String? = null,
    message: String? = null,
    onDismiss: (() -> Unit)? = null,
    showLoader: Boolean = false,
    confirmButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (show) {
        AlertDialog(
            onDismissRequest = { onDismiss?.invoke() ?: Unit },
            confirmButton = confirmButton ?: {},
            title = {
                if (message != null) Text(message)
            },
            text = {
                Column(
                    modifier = modifier.size(140.dp, 70.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showLoader) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
        )
    }
}
