package com.ramoieeeufjf.appRamo.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramoieeeufjf.appRamo.BuildConfig
import com.ramoieeeufjf.appRamo.R
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val API_BASE_URL = BuildConfig.DOOR_API_BASE_URL.trimEnd('/')
private val API_KEY = BuildConfig.DOOR_API_KEY

@Serializable
private data class DoorApiResponse(
    val ok: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@Serializable
private data class MeetingScheduleRequest(
    @SerialName("delay_seconds") val delaySeconds: Long,
    @SerialName("profile_indices") val profileIndices: List<Int>,
    val recurrence: String = "none",
    val weekdays: List<Int>? = null
)

@Serializable
private data class MeetingCancelRequest(
    val id: Long
)

@Serializable
private data class MeetingScheduleResponse(
    val ok: Boolean = false,
    val error: String? = null,
    val id: Long = 0,
    @SerialName("pending_count") val pendingCount: Int = 0,
    val active: Boolean = false,
    @SerialName("time_synced") val timeSynced: Boolean = false,
    @SerialName("now_unix") val nowUnix: Long = 0,
    @SerialName("start_unix") val startUnix: Long = 0,
    @SerialName("delay_seconds") val delaySeconds: Long = 0,
    @SerialName("profile_count") val profileCount: Int = 0,
    val recurrence: String = "none",
    @SerialName("weekdays_mask") val weekdaysMask: Int = 0
)

@Serializable
private data class MeetingCancelResponse(
    val ok: Boolean = false,
    val error: String? = null,
    @SerialName("canceled_count") val canceledCount: Int = 0,
    @SerialName("pending_count") val pendingCount: Int = 0,
    val active: Boolean = false
)

@Serializable
private data class MeetingStatusResponse(
    val ok: Boolean = false,
    val error: String? = null,
    val active: Boolean = false,
    @SerialName("pending_count") val pendingCount: Int = 0,
    @SerialName("time_synced") val timeSynced: Boolean = false,
    @SerialName("now_unix") val nowUnix: Long = 0,
    @SerialName("active_selected_profiles") val activeSelectedProfiles: Int = 0,
    @SerialName("active_allowed_cards") val activeAllowedCards: Int = 0,
    @SerialName("last_id") val lastId: Long = 0,
    @SerialName("last_start_unix") val lastStartUnix: Long = 0,
    @SerialName("last_selected_profiles") val lastSelectedProfiles: Int = 0,
    @SerialName("last_allowed_cards") val lastAllowedCards: Int = 0,
    @SerialName("last_status") val lastStatus: String = "idle",
    val schedules: List<MeetingScheduleItem> = emptyList()
)

@Serializable
private data class MeetingScheduleItem(
    val id: Long = 0,
    @SerialName("start_unix") val startUnix: Long = 0,
    @SerialName("profile_count") val profileCount: Int = 0,
    val recurrence: String = "none",
    @SerialName("weekdays_mask") val weekdaysMask: Int = 0
)

@Composable
fun DoorControlPage() {
    var meetingStatus by remember { mutableStateOf<MeetingStatusResponse?>(null) }
    var delayMinutes by remember { mutableStateOf("5") }
    var profileIndices by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("none") }
    var weekdays by remember { mutableStateOf("") }
    var cancelId by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val client = remember {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    fun endpoint(path: String) = "$API_BASE_URL$path"

    fun apiError(prefix: String, error: String?): String {
        return "$prefix: ${error ?: "resposta inesperada"}"
    }

    fun parseProfileIndices(): List<Int> {
        val tokens = profileIndices.split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) {
            throw IllegalArgumentException("Informe ao menos um índice de perfil.")
        }
        return tokens.map { token ->
            val value = token.toIntOrNull()
            if (value == null || value < 0) {
                throw IllegalArgumentException("Use apenas índices de perfil válidos.")
            }
            value
        }
    }

    fun parseWeekdays(): List<Int>? {
        val tokens = weekdays.split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) {
            return null
        }
        return tokens.map { token ->
            val value = token.toIntOrNull()
            if (value == null || value !in 0..6) {
                throw IllegalArgumentException("Dias da semana devem ficar entre 0 e 6.")
            }
            value
        }
    }

    fun refreshMeetingStatus() {
        coroutineScope.launch {
            try {
                loading = true
                errorMsg = null
                val response: MeetingStatusResponse = client.get(endpoint("/api/meeting/status")) {
                    header("X-API-KEY", API_KEY)
                }.body()
                if (response.ok) {
                    meetingStatus = response
                } else {
                    errorMsg = apiError("Falha ao buscar modo reunião", response.error)
                }
            } catch (e: Exception) {
                errorMsg = "Falha ao buscar modo reunião: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun openDoor() {
        coroutineScope.launch {
            try {
                loading = true
                errorMsg = null
                successMsg = null
                val response: DoorApiResponse = client.post(endpoint("/api/door/open")) {
                    header("X-API-KEY", API_KEY)
                }.body()
                if (response.ok) {
                    successMsg = response.message ?: "Comando de abertura enviado."
                } else {
                    errorMsg = apiError("Falha ao abrir porta", response.error)
                }
            } catch (e: Exception) {
                errorMsg = "Erro ao abrir porta: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun scheduleMeeting() {
        coroutineScope.launch {
            try {
                val minutes = delayMinutes.toLongOrNull()
                if (minutes == null || minutes <= 0 || minutes > 1440) {
                    throw IllegalArgumentException("O atraso deve ficar entre 1 e 1440 minutos.")
                }

                val request = MeetingScheduleRequest(
                    delaySeconds = minutes * 60,
                    profileIndices = parseProfileIndices(),
                    recurrence = recurrence,
                    weekdays = if (recurrence == "weekly") parseWeekdays() else null
                )

                loading = true
                errorMsg = null
                successMsg = null
                val response: MeetingScheduleResponse = client.post(endpoint("/api/meeting/schedule")) {
                    contentType(ContentType.Application.Json)
                    header("X-API-KEY", API_KEY)
                    setBody(request)
                }.body()
                if (response.ok) {
                    successMsg = "Reunião agendada. ID ${response.id}."
                    refreshMeetingStatus()
                } else {
                    errorMsg = apiError("Falha ao agendar reunião", response.error)
                }
            } catch (e: Exception) {
                errorMsg = e.message ?: "Erro ao agendar reunião."
            } finally {
                loading = false
            }
        }
    }

    fun cancelMeeting() {
        coroutineScope.launch {
            try {
                val id = cancelId.trim().takeIf { it.isNotEmpty() }?.toLongOrNull()
                if (cancelId.isNotBlank() && id == null) {
                    throw IllegalArgumentException("ID de cancelamento inválido.")
                }

                loading = true
                errorMsg = null
                successMsg = null
                val response: MeetingCancelResponse = if (id == null) {
                    client.post(endpoint("/api/meeting/cancel")) {
                        header("X-API-KEY", API_KEY)
                    }.body()
                } else {
                    client.post(endpoint("/api/meeting/cancel")) {
                        contentType(ContentType.Application.Json)
                        header("X-API-KEY", API_KEY)
                        setBody(MeetingCancelRequest(id))
                    }.body()
                }
                if (response.ok) {
                    successMsg = "${response.canceledCount} agendamento(s) cancelado(s)."
                    cancelId = ""
                    refreshMeetingStatus()
                } else {
                    errorMsg = apiError("Falha ao cancelar reunião", response.error)
                }
            } catch (e: Exception) {
                errorMsg = e.message ?: "Erro ao cancelar reunião."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshMeetingStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.rasieee),
            contentDescription = "RAS Logo",
            modifier = Modifier.size(72.dp)
        )

        Text("Controle da sala", style = MaterialTheme.typography.headlineSmall)

        StatusMessages(loading = loading, errorMsg = errorMsg, successMsg = successMsg)

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Porta", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { openDoor() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir porta")
                }
            }
        }

        MeetingStatusCard(meetingStatus = meetingStatus, onRefresh = { refreshMeetingStatus() }, loading = loading)

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Agendar modo reunião", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = delayMinutes,
                    onValueChange = { delayMinutes = it },
                    label = { Text("Atraso em minutos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = profileIndices,
                    onValueChange = { profileIndices = it },
                    label = { Text("Índices dos perfis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                RecurrenceSelector(recurrence = recurrence, onChange = { recurrence = it })

                if (recurrence == "weekly") {
                    OutlinedTextField(
                        value = weekdays,
                        onValueChange = { weekdays = it },
                        label = { Text("Dias 0-6") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(onClick = { scheduleMeeting() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                    Text("Agendar reunião")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cancelar agendamento", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = cancelId,
                    onValueChange = { cancelId = it },
                    label = { Text("ID opcional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { cancelMeeting() }, enabled = !loading, modifier = Modifier.weight(1f)) {
                        Text(if (cancelId.isBlank()) "Cancelar todos" else "Cancelar ID")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusMessages(loading: Boolean, errorMsg: String?, successMsg: String?) {
    if (loading) {
        Text("Atualizando...", style = MaterialTheme.typography.bodyMedium)
    }
    errorMsg?.let {
        Text("Erro: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
    successMsg?.let {
        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MeetingStatusCard(meetingStatus: MeetingStatusResponse?, onRefresh: () -> Unit, loading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo reunião", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onRefresh, enabled = !loading) {
                    Text("Atualizar")
                }
            }

            if (meetingStatus == null) {
                Text("Status indisponível.")
            } else {
                Text(if (meetingStatus.active) "Ativo" else "Inativo")
                Text("Agendamentos pendentes: ${meetingStatus.pendingCount}")
                Text("Relógio sincronizado: ${if (meetingStatus.timeSynced) "sim" else "não"}")

                if (meetingStatus.active) {
                    Text("Perfis ativos: ${meetingStatus.activeSelectedProfiles}")
                    Text("Cartões liberados: ${meetingStatus.activeAllowedCards}")
                }

                if (meetingStatus.lastStatus != "idle") {
                    Text("Último status: ${meetingStatus.lastStatus}")
                }

                if (meetingStatus.schedules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Próximos agendamentos", style = MaterialTheme.typography.labelLarge)
                    meetingStatus.schedules.forEach { schedule ->
                        Text(
                            "ID ${schedule.id}: ${formatUnix(schedule.startUnix)} · ${schedule.profileCount} perfil(is) · ${recurrenceLabel(schedule.recurrence)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurrenceSelector(recurrence: String, onChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("none" to "Única", "daily" to "Diária", "weekly" to "Semanal").forEach { (value, label) ->
            Button(
                onClick = { onChange(value) },
                enabled = recurrence != value,
                modifier = Modifier.weight(1f)
            ) {
                Text(label)
            }
        }
    }
}

private fun formatUnix(unix: Long): String {
    if (unix <= 0L) {
        return "-"
    }
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(unix * 1000L))
}

private fun recurrenceLabel(value: String): String {
    return when (value) {
        "daily" -> "diária"
        "weekly" -> "semanal"
        else -> "única"
    }
}

@Preview(showBackground = true)
@Composable
fun DoorControlPagePreview() {
    DoorControlPage()
}
