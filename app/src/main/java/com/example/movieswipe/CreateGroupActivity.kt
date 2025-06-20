package com.example.movieswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movieswipe.ui.components.CustomTextField
import com.example.movieswipe.ui.components.MultiSelectDropdown
import com.example.movieswipe.ui.components.PrimaryButton
import com.example.movieswipe.ui.theme.GreenPrimary
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.material3.SnackbarHost

class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieSwipeTheme {
                CreateGroupForm()
            }
        }
    }
}

@Composable
fun CreateGroupForm() {
    var name by remember { mutableStateOf("") }
    val genreOptions =
        listOf("Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller")
    var selectedGenres by remember { mutableStateOf(listOf<String>()) }
    var submitted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = name,
                onValueChange = { name = it },
                label = "Group Name",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            MultiSelectDropdown(
                label = "Select Genres",
                options = genreOptions,
                selectedOptions = selectedGenres,
                onSelectionChange = { selectedGenres = it },
                modifier = Modifier.padding(bottom = 24.dp)
            )
            PrimaryButton(
                text = "Submit",
                onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Submitting...")
                    }
                    Log.d("CreateGroupForm", "Submitting: name=$name, genres=$selectedGenres")
                    // TODO: Add API call to actually submit the data
                    submitted = true
                },
                modifier = Modifier,
                containerColor = GreenPrimary
            )
            if (submitted) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "Submitted: $name, Genres: ${selectedGenres.joinToString()}"
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
