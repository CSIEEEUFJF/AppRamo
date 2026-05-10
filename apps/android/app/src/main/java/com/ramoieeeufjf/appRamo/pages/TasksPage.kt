package com.ramoieeeufjf.appRamo.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramoieeeufjf.appRamo.ui.theme.REIEEEUFJFTheme

// Data class is now independent of Firebase
data class ChapterTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val chapter: String = "",
    val completed: Boolean = false
)

@Composable
fun TasksPage(
    tasks: List<ChapterTask>,
    userChapters: List<String>,
    canManageContent: Boolean = false,
    onAddTask: (ChapterTask) -> Unit,
    onTaskCompleted: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit // New delete handler
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<ChapterTask?>(null) }

    Scaffold(
        floatingActionButton = {
            if (canManageContent && userChapters.isNotEmpty()) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar tarefa")
                }
            }
        }
    ) { paddingValues ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Sem tarefas agendadas para o seu capítulo.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task, 
                        canManageContent = canManageContent,
                        onTaskCompleted = onTaskCompleted,
                        onClick = { selectedTask = task } // Make item clickable
                    )
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            userChapters = userChapters,
            onDismiss = { showDialog = false },
            onConfirm = { title, description, chapter ->
                val newTask = ChapterTask(
                    title = title,
                    description = description,
                    chapter = chapter
                )
                onAddTask(newTask)
                showDialog = false
            }
        )
    }

    selectedTask?.let {
        TaskDetailsDialog(
            task = it,
            canManageContent = canManageContent,
            onDismiss = { selectedTask = null },
            onDelete = { taskId ->
                onDeleteTask(taskId)
                selectedTask = null
            }
        )
    }
}

@Composable
fun TaskItem(
    task: ChapterTask, 
    canManageContent: Boolean,
    onTaskCompleted: (String, Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Make the whole card clickable
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = if (canManageContent) {
                    { onTaskCompleted(task.id, it) }
                } else {
                    null
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                if (task.description.isNotBlank()) {
                    Text(text = task.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
                Text(text = "Capítulo: ${task.chapter}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDialog(
    userChapters: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var selectedChapter by remember { mutableStateOf(userChapters.firstOrNull() ?: "") }

    val isFormValid = taskTitle.isNotBlank() && taskDescription.isNotBlank() && selectedChapter.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(vertical = 24.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Adicionar Nova Tarefa", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(value = taskTitle, onValueChange = { taskTitle = it }, label = { Text("Título da Tarefa") }, modifier = Modifier.fillMaxWidth(), isError = taskTitle.isBlank())
                OutlinedTextField(value = taskDescription, onValueChange = { taskDescription = it }, label = { Text("Descrição da Tarefa") }, modifier = Modifier.fillMaxWidth(), isError = taskDescription.isBlank())

                ExposedDropdownMenuBox(
                    expanded = chapterDropdownExpanded,
                    onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedChapter,
                        onValueChange = {},
                        label = { Text("Capítulo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                        isError = selectedChapter.isBlank()
                    )
                    ExposedDropdownMenu(expanded = chapterDropdownExpanded, onDismissRequest = { chapterDropdownExpanded = false }) {
                        userChapters.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedChapter = selectionOption
                                    chapterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = { onConfirm(taskTitle, taskDescription, selectedChapter) },
                        enabled = isFormValid
                    ) { Text("Adicionar") }
                }
            }
        }
    }
}

@Composable
fun TaskDetailsDialog(
    task: ChapterTask,
    canManageContent: Boolean,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = task.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = task.description)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Capítulo: ${task.chapter}")
                Spacer(modifier = Modifier.height(16.dp))
                 Row {
                    if (canManageContent) {
                        Button(
                            onClick = { onDelete(task.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Button(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Tasks Page With Tasks")
@Composable
fun TasksPagePreview() {
    val sampleTasks = listOf(
        ChapterTask(id = "1", title = "Prepare workshop", description = "Need to create slides and examples.", chapter = "RAS", completed = false),
        ChapterTask(id = "2", title = "Send weekly report", chapter = "Diretoria", completed = true)
    )
    REIEEEUFJFTheme {
        TasksPage(tasks = sampleTasks, userChapters = listOf("RAS", "Diretoria", "Todos"), canManageContent = true, onAddTask = {}, onTaskCompleted = { _, _ -> }, onDeleteTask = {})
    }
}
