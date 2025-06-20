package com.example.movieswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.movieswipe.data.TokenManager
import com.example.movieswipe.network.GroupService
import com.example.movieswipe.network.Group
import com.example.movieswipe.ui.theme.MovieSwipeTheme

class GroupDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getStringExtra("groupId")
        setContent {
            MovieSwipeTheme {
                GroupDetailsScreen(groupId = groupId)
            }
        }
    }
}

@Composable
fun GroupDetailsScreen(groupId: String?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val groupState = remember { mutableStateOf<Group?>(null) }
    val errorState = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(groupId) {
        if (groupId != null) {
            val tokenManager = TokenManager.getInstance(context)
            val accessToken = tokenManager.getAccessToken()
            if (!accessToken.isNullOrEmpty()) {
                val result = GroupService.getGroupById(accessToken, groupId)
                result.onSuccess { groupState.value = it }
                result.onFailure { errorState.value = it.localizedMessage }
            } else {
                errorState.value = "No access token. Please log in."
            }
        } else {
            errorState.value = "No group ID provided."
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (groupState.value != null) {
            GroupDetailsContent(group = groupState.value!!)
        } else if (errorState.value != null) {
            Text(text = errorState.value!!)
        } else {
            Text(text = "Loading group details...")
        }
    }
}

@Composable
fun GroupDetailsContent(group: Group) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Group ID: ${group._id}")
        Text(text = "Invitation Code: ${group.invitationCode}")
        Text(text = "Owner: ${group.owner.name} (${group.owner.email})")
        Text(text = "Members:")
        group.members.forEach {
            Text(text = "- ${it.name} (${it.email})")
        }
        Text(text = "Created At: ${group.createdAt}")
        Text(text = "Updated At: ${group.updatedAt}")
    }
}

