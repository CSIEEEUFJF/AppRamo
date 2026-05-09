package com.ramoieeeufjf.appRamo.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.provider.CalendarContract
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramoieeeufjf.appRamo.R
import com.ramoieeeufjf.appRamo.ui.theme.REIEEEUFJFTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChapterEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startTime: Date? = null,
    val endTime: Date? = null,
    val chapter: String = ""
)

@Composable
fun CalendarPage(
    events: List<ChapterEvent>,
    userChapters: List<String>,
    onAddEvent: (ChapterEvent) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<ChapterEvent?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Schedule event")
            }
        }
    ) { paddingValues ->
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Sem eventos ou reuniões agendadas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(events, key = { it.id }) { event ->
                    val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                    ListItem(
                        headlineContent = { Text(event.title) },
                        supportingContent = {
                            val start = event.startTime?.let { dateFormat.format(it) } ?: "N/A"
                            val end = event.endTime?.let { dateFormat.format(it) } ?: "N/A"
                            Text("De: $start \nAté: $end")
                        },
                        overlineContent = { Text(event.location) },
                        modifier = Modifier.clickable { selectedEvent = event }
                    )
                }
            }
        }
    }

    if (showDialog) {
        EventDialog(
            userChapters = userChapters,
            onDismiss = { showDialog = false },
            onConfirm = { event ->
                onAddEvent(event)
                showDialog = false
            }
        )
    }

    selectedEvent?.let {
        EventDetailsDialog(
            event = it,
            onDismiss = { selectedEvent = null },
            onDelete = { eventId -> // Correctly handle the eventId parameter
                onDeleteEvent(eventId)
                selectedEvent = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDialog(
    userChapters: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (ChapterEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(Calendar.getInstance()) }
    var endTime by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var selectedChapter by remember { mutableStateOf(userChapters.firstOrNull() ?: "") }

    val isFormValid = title.isNotBlank() && location.isNotBlank() && selectedChapter.isNotBlank()

    val context = LocalContext.current
    val dateTimeFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    fun showDateTimePicker(calendar: Calendar, onDateTimeSet: (Calendar) -> Unit) {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            TimePickerDialog(context, { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, dayOfMonth, hourOfDay, minute)
                onDateTimeSet(newCal)
            }, currentHour, currentMinute, true).show()
        }, currentYear, currentMonth, currentDay).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(id = R.string.agendar_reuniao), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(id = R.string.titulo)) }, isError = title.isBlank())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(id = R.string.descricao)) })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(stringResource(id = R.string.local)) }, isError = location.isBlank())

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

                Text(stringResource(id = R.string.inicio, dateTimeFormat.format(startTime.time)))
                Button(onClick = { showDateTimePicker(startTime) { startTime = it } }) {
                    Text(stringResource(id = R.string.selecionar_inicio))
                }

                Text(stringResource(id = R.string.fim, dateTimeFormat.format(endTime.time)))
                Button(onClick = { showDateTimePicker(endTime) { endTime = it } }) {
                    Text(stringResource(id = R.string.selecionar_fim))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            val eventData = ChapterEvent(
                                title = title,
                                description = description,
                                location = location,
                                startTime = startTime.time,
                                endTime = endTime.time,
                                chapter = selectedChapter
                            )
                            onConfirm(eventData)

                            val intent = Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.timeInMillis)
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
                                .putExtra(CalendarContract.Events.TITLE, title)
                                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                            context.startActivity(intent)
                        },
                        enabled = isFormValid
                    ) {
                        Text(stringResource(id = R.string.agendar_reuniao))
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailsDialog(event: ChapterEvent, onDismiss: () -> Unit, onDelete: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = event.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = event.description)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Local: ${event.location}")
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                Text(text = "Início: ${event.startTime?.let { dateFormat.format(it) } ?: "N/A"}")
                Text(text = "Fim: ${event.endTime?.let { dateFormat.format(it) } ?: "N/A"}")
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = { onDelete(event.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Calendar Page with Events")
@Composable
fun CalendarPageWithEventsPreview() {
    val sampleEvents = listOf(
        ChapterEvent(id = "1", title = "Weekly Sync", location = "Room 404", chapter = "RAS", startTime = Date(), endTime = Date()),
        ChapterEvent(id = "2", title = "Project Deadline", location = "Online", chapter = "RAS", startTime = Date(), endTime = Date())
    )
    REIEEEUFJFTheme {
        CalendarPage(events = sampleEvents, userChapters = listOf("RAS", "IAS"), onAddEvent = {}, onDeleteEvent = {})
    }
}

@Preview(showBackground = true, name = "Event Creation Dialog")
@Composable
fun EventDialogPreview() {
    REIEEEUFJFTheme {
        EventDialog(userChapters = listOf("RAS", "IAS"), onDismiss = {}, onConfirm = {})
    }
}
