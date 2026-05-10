package com.ramoieeeufjf.appRamo

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.ramoieeeufjf.appRamo.pages.CalendarPage
import com.ramoieeeufjf.appRamo.pages.ChapterEvent
import com.ramoieeeufjf.appRamo.pages.ChapterTask
import com.ramoieeeufjf.appRamo.pages.DoorControlPage
import com.ramoieeeufjf.appRamo.pages.LoginPage
import com.ramoieeeufjf.appRamo.pages.MainPage
import com.ramoieeeufjf.appRamo.pages.MembersPage
import com.ramoieeeufjf.appRamo.pages.ProfilePage
import com.ramoieeeufjf.appRamo.pages.TasksPage
import com.ramoieeeufjf.appRamo.security.AccessPolicy
import com.ramoieeeufjf.appRamo.ui.screens.RegistrationPage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var chapterRoles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var events by remember { mutableStateOf<List<ChapterEvent>>(emptyList()) }
    var tasks by remember { mutableStateOf<List<ChapterTask>>(emptyList()) }
    var userName by remember { mutableStateOf<String?>(null) }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var userEmail by remember { mutableStateOf<String?>(null) }
    var authenticatedUid by remember { mutableStateOf(auth.currentUser?.uid) }

    val visibleChapters = remember(chapterRoles) { AccessPolicy.visibleChapters(chapterRoles) }
    val canManageContent = remember(chapterRoles) { AccessPolicy.canManageContent(chapterRoles) }
    val canControlRoom = remember(chapterRoles) { AccessPolicy.canControlRoom(chapterRoles) }
    val startDestination = remember { if (auth.currentUser == null) "login" else "main" }

    suspend fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        authenticatedUid = currentUser.uid
        val userDoc = db.collection("users").document(currentUser.uid).get().await()
        chapterRoles = readStringMap(userDoc.get("chapterRoles"))
        userName = userDoc.getString("name")
        profilePictureUrl = userDoc.getString("profilePictureUrl")
        userEmail = userDoc.getString("email") ?: currentUser.email
    }

    fun clearSessionState() {
        authenticatedUid = null
        chapterRoles = emptyMap()
        events = emptyList()
        tasks = emptyList()
        userName = null
        profilePictureUrl = null
        userEmail = null
    }

    fun requireContentManager(action: () -> Unit) {
        if (!canManageContent) {
            Toast.makeText(context, "Você não tem permissão para alterar este módulo.", Toast.LENGTH_SHORT).show()
            return
        }
        action()
    }

    LaunchedEffect(authenticatedUid) {
        if (authenticatedUid != null) {
            runCatching { loadUserData() }
                .onFailure { Toast.makeText(context, "Falha ao carregar perfil.", Toast.LENGTH_SHORT).show() }
        }
    }

    DisposableEffect(authenticatedUid, visibleChapters) {
        if (authenticatedUid == null) {
            onDispose { }
        } else {
            val eventsListener = db.collection("events")
                .whereIn("chapter", visibleChapters)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Toast.makeText(context, "Falha ao sincronizar eventos.", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    events = snapshots?.mapNotNull { it.toObject<ChapterEvent>().copy(id = it.id) } ?: emptyList()
                }

            val tasksListener = db.collection("tasks")
                .whereIn("chapter", visibleChapters)
                .orderBy("completed")
                .orderBy("title")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Toast.makeText(context, "Falha ao sincronizar tarefas.", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    tasks = snapshots?.mapNotNull { it.toObject<ChapterTask>().copy(id = it.id) } ?: emptyList()
                }

            onDispose {
                eventsListener.remove()
                tasksListener.remove()
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginPage(
                onLoginClick = { email, password ->
                    coroutineScope.launch {
                        try {
                            auth.signInWithEmailAndPassword(email, password).await()
                            loadUserData()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRegisterClick = { navController.navigate("registration") }
            )
        }
        composable("registration") {
            RegistrationPage(
                onRegisterClick = {
                    coroutineScope.launch {
                        runCatching { loadUserData() }
                        if (navController.previousBackStackEntry?.destination?.route == "profile") {
                            navController.navigateUp()
                        } else {
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        composable("main") {
            MainPage(
                userName = userName,
                profilePictureUrl = profilePictureUrl,
                canControlRoom = canControlRoom,
                onProfileClick = { navController.navigate("profile") },
                onChapterTasksClick = { navController.navigate("tasks") },
                onChapterCalendarClick = { navController.navigate("calendar") },
                onRoomControlClick = { navController.navigate("door_control") },
                onBranchMembersClick = { navController.navigate("members") }
            )
        }
        composable("profile") {
            ProfilePage(
                userName = userName,
                profilePictureUrl = profilePictureUrl,
                email = userEmail,
                chapterRoles = chapterRoles,
                onEditProfile = { navController.navigate("registration") },
                onLogoutClick = {
                    auth.signOut()
                    clearSessionState()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable("calendar") {
            CalendarPage(
                events = events,
                userChapters = visibleChapters,
                canManageContent = canManageContent,
                onAddEvent = { event ->
                    requireContentManager {
                        coroutineScope.launch {
                            db.collection("events").add(event).await()
                        }
                    }
                },
                onDeleteEvent = { eventId ->
                    requireContentManager {
                        coroutineScope.launch {
                            db.collection("events").document(eventId).delete().await()
                        }
                    }
                }
            )
        }
        composable("tasks") {
            TasksPage(
                tasks = tasks,
                userChapters = visibleChapters,
                canManageContent = canManageContent,
                onAddTask = { task ->
                    requireContentManager {
                        coroutineScope.launch {
                            db.collection("tasks").add(task).await()
                        }
                    }
                },
                onTaskCompleted = { taskId, completed ->
                    requireContentManager {
                        coroutineScope.launch {
                            db.collection("tasks").document(taskId).update("completed", completed).await()
                        }
                    }
                },
                onDeleteTask = { taskId ->
                    requireContentManager {
                        coroutineScope.launch {
                            db.collection("tasks").document(taskId).delete().await()
                        }
                    }
                }
            )
        }
        composable("door_control") {
            if (canControlRoom) {
                DoorControlPage()
            } else {
                PermissionDeniedPage("Você não tem permissão para controlar a sala.")
            }
        }
        composable("members") { MembersPage() }
    }
}

private fun readStringMap(value: Any?): Map<String, String> {
    return (value as? Map<*, *>)
        ?.mapNotNull { (key, mapValue) ->
            val chapter = key as? String
            val role = mapValue as? String
            if (chapter.isNullOrBlank() || role.isNullOrBlank()) null else chapter to role
        }
        ?.toMap()
        ?: emptyMap()
}

@Composable
private fun PermissionDeniedPage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}
