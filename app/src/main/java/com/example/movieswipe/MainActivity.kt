package com.example.movieswipe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import com.example.movieswipe.ui.components.PrimaryButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.movieswipe.data.TokenManager
import com.example.movieswipe.network.GroupService
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.movieswipe.network.Group
import com.example.movieswipe.ui.components.GroupListItem
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.Duration
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieSwipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
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
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        val result = GroupService.getGroups(context)
        result.onSuccess { allGroups ->
            val now = Instant.now()
            val recentGroups = allGroups.filter {
                val createdAt = try { Instant.parse(it.createdAt) } catch (_: Exception) { null }
                createdAt != null && Duration.between(createdAt, now).toHours() < 1
            }.sortedByDescending { it.createdAt }
            groups = recentGroups
        }.onFailure {
            error = it.localizedMessage
        }
        isLoading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top 70%: Group List
            Box(modifier = Modifier.weight(0.7f)) {
                when {
                    isLoading -> Text("Loading groups...", modifier = Modifier.align(Alignment.Center))
                    error != null -> Text("Error: $error", modifier = Modifier.align(Alignment.Center))
                    groups.isEmpty() -> Text("No recent groups.", modifier = Modifier.align(Alignment.Center))
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(groups) { group: Group ->
                            GroupListItem(
                                group = group,
                                onClick = {
                                    val intent = Intent(context, GroupDetailsActivity::class.java)
                                    intent.putExtra("groupId", group._id)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
            // Bottom 30%: Buttons
            Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PrimaryButton(
                        text = "Create Group",
                        onClick = {
                            coroutineScope.launch {
                                val result = GroupService.createGroup(context)
                                result.onSuccess { group ->
                                    val intent = Intent(context, GroupDetailsActivity::class.java)
                                    intent.putExtra("groupId", group._id)
                                    context.startActivity(intent)
                                }
                                // Optionally handle error
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