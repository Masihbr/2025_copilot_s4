package com.example.movieswipe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieSwipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GroupButtonsView(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun GroupButtonsView(modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            context.startActivity(Intent(context, CreateGroupActivity::class.java))
        }) {
            Text("Create Group")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            context.startActivity(Intent(context, JoinGroupActivity::class.java))
        }) {
            Text("Join Group")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MovieSwipeTheme {
        Greeting("World")
    }
}