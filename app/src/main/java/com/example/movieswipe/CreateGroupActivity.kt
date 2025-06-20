package com.example.movieswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.movieswipe.ui.theme.MovieSwipeTheme

class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieSwipeTheme {
                Text("Hello World")
            }
        }
    }
}

