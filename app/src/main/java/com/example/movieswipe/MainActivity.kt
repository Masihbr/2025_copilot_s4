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
import com.example.movieswipe.ui.components.PrimaryButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.movieswipe.data.TokenManager
import com.example.movieswipe.network.GroupService
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryButton(
            text = "Create Group",
            onClick = {
                coroutineScope.launch {
                    val result = GroupService.createGroup(context)
                    result.onSuccess { group ->
                        val intent = Intent(context, GroupDetailsActivity::class.java)
                        intent.putExtra("groupId", group._id)
                        context.startActivity(intent)
                    }.onFailure {
                        // Handle error (show snackbar, etc.)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = "Join Group",
            onClick = { context.startActivity(Intent(context, JoinGroupActivity::class.java)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MovieSwipeTheme {
        Greeting("World")
    }
}