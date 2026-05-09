package com.ramoieeeufjf.appRamo

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.ramoieeeufjf.appRamo.pages.*
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

    fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            coroutineScope.launch {
                try {
                    val userDoc = db.collection("users").document(currentUser.uid).get().await()
                    chapterRoles = userDoc.get("chapterRoles") as? Map<String, String> ?: emptyMap()
                    userName = userDoc.getString("name")
                    profilePictureUrl = userDoc.getString("profilePictureUrl")
                    userEmail = userDoc.getString("email")
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun fetchEvents() {
        if (chapterRoles.isNotEmpty()) {
            val chaptersToQuery = chapterRoles.keys.toList() + "Todos"
            db.collection("events")
                .whereIn("chapter", chaptersToQuery)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    events = snapshots?.mapNotNull { it.toObject<ChapterEvent>().copy(id = it.id) } ?: emptyList()
                }
        }
    }

    fun fetchTasks() {
        if (chapterRoles.isNotEmpty()) {
            val chaptersToQuery = chapterRoles.keys.toList() + "Todos"
            db.collection("tasks")
                .whereIn("chapter", chaptersToQuery)
                .orderBy("completed")
                .orderBy("title")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    tasks = snapshots?.mapNotNull { it.toObject<ChapterTask>().copy(id = it.id) } ?: emptyList()
                }
        }
    }

    LaunchedEffect(auth.currentUser) {
        fetchUserData()
    }

    LaunchedEffect(chapterRoles) {
        fetchEvents()
        fetchTasks()
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginPage(
                onLoginClick = { email, password ->
                    coroutineScope.launch {
                        try {
                            auth.signInWithEmailAndPassword(email, password).await()
                            fetchUserData()
                            navController.navigate("main") { popUpTo("login") { inclusive = true } }
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
                    fetchUserData()
                    navController.navigateUp()
                }
            )
        }
        composable("main") {
            MainPage(
                userName = userName,
                profilePictureUrl = profilePictureUrl,
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
                    navController.navigate("login") { popUpTo("main") { inclusive = true } } 
                }
            )
        }
        composable("calendar") { 
            CalendarPage(
                events = events,
                userChapters = chapterRoles.keys.toList() + "Todos",
                onAddEvent = {
                    coroutineScope.launch {
                        db.collection("events").add(it).await()
                    }
                },
                onDeleteEvent = { eventId ->
                    coroutineScope.launch {
                        db.collection("events").document(eventId).delete().await()
                    }
                }
            ) 
        }
        composable("tasks") { 
            TasksPage(
                tasks = tasks,
                userChapters = chapterRoles.keys.toList() + "Todos",
                onAddTask = {
                    coroutineScope.launch {
                        db.collection("tasks").add(it).await()
                    }
                },
                onTaskCompleted = { taskId, completed ->
                    coroutineScope.launch {
                        db.collection("tasks").document(taskId).update("completed", completed)
                    }
                },
                onDeleteTask = { taskId ->
                    coroutineScope.launch {
                        db.collection("tasks").document(taskId).delete().await()
                    }
                }
            )
        }
        composable("door_control") { DoorControlPage() }
        composable("members") { MembersPage() }
    }
}