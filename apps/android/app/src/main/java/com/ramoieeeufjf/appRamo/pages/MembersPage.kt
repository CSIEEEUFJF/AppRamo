package com.ramoieeeufjf.appRamo.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ramoieeeufjf.appRamo.R
import java.util.SortedMap

// Updated data class for the new chapter roles structure
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,
    val chapterRoles: Map<String, String> = emptyMap()
)

@Composable
fun MembersPage() {
    var allUsers by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var clickedChapter by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val groupedMembers: SortedMap<String, List<UserProfile>> by remember(allUsers) {
        val map = mutableMapOf<String, MutableList<UserProfile>>()
        allUsers.forEach { user ->
            user.chapterRoles.keys.forEach { chapter ->
                if (chapter != "Todos") {
                    map.getOrPut(chapter) { mutableListOf() }.add(user)
                }
            }
        }
        mutableStateOf(map.toSortedMap())
    }

    // Using a snapshot listener for real-time updates.
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").addSnapshotListener { snapshot, e ->
            if (e != null) {
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                allUsers = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<UserProfile>()?.copy(uid = doc.id)
                }
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedMembers.forEach { (chapter, members) ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(text = chapter, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(members, key = { it.uid + chapter }) { member ->
                    MemberGridItem(member = member, onClick = {
                        selectedUser = member
                        clickedChapter = chapter // Save which chapter was clicked
                    })
                }
            }
        }
    }

    if (selectedUser != null && clickedChapter != null) {
        Dialog(onDismissRequest = { selectedUser = null }) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(24.dp)) {
                    val roleInChapter = selectedUser!!.chapterRoles[clickedChapter]

                    Text(text = selectedUser!!.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Cargo em $clickedChapter: ${roleInChapter ?: ""}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Telefone: ${selectedUser!!.phoneNumber}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { selectedUser = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun MemberGridItem(member: UserProfile, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = member.profilePictureUrl ?: R.drawable.ic_launcher_foreground),
            contentDescription = "Profile picture of ${member.name}",
            modifier = Modifier.size(80.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(member.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun MembersPagePreview() {
    MembersPage()
}
