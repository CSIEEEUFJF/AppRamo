package com.ramoieeeufjf.appRamo.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramoieeeufjf.appRamo.R
import com.ramoieeeufjf.appRamo.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// For local VM test:
// private const val BASE_URL = "http://192.168.0.123:3000"
// For Cloudflare:
private val BASE_URL = BuildConfig.DOOR_RELAY_BASE_URL
private val API_KEY = BuildConfig.DOOR_RELAY_API_KEY
private val DEVICE_ID = BuildConfig.DOOR_RELAY_DEVICE_ID

// Matches JSON from relay GET /status
@Serializable
data class DeviceStatus(
    val device_id: String,
    val door: Int? = null,
    val light: Int? = null,
    val last_seen: Long? = null
)

@Composable
fun DoorControlPage() {
    var doorOpen by remember { mutableStateOf<Boolean?>(null) }
    var lightOn by remember { mutableStateOf<Boolean?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val client = remember {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    fun refreshStatus() {
        coroutineScope.launch {
            try {
                loading = true
                errorMsg = null
                val status: DeviceStatus = client.get("$BASE_URL/status") {
                    parameter("device_id", DEVICE_ID)
                    header("X-API-KEY", API_KEY)
                }.body()
                status.door?.let { d ->
                    when (d) {
                        1 -> doorOpen = true
                        0 -> doorOpen = false
                        // any other value: do nothing
                    }
                }

                status.light?.let { l ->
                    when (l) {
                        1 -> lightOn = true
                        0 -> lightOn = false
                        // any other value: do nothing
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "Falha ao buscar status"
            } finally {
                loading = false
            }
        }
    }

    fun sendCommand(action: String) {
        coroutineScope.launch {
            try {
                loading = true
                errorMsg = null

                // Optimistic update
                when (action) {
                    "door_on"  -> doorOpen = true
                    "door_off" -> doorOpen = false
                    "light_on" -> lightOn = true
                    "light_off"-> lightOn = false
                }

                client.post("$BASE_URL/send") {
                    contentType(ContentType.Application.Json)
                    header("X-API-KEY", API_KEY)
                    setBody(
                        """
                    {
                      "device_id": "$DEVICE_ID",
                      "command": { "action": "$action" }
                    }
                    """.trimIndent()
                    )
                }

                // Give the ESP a small window to POST /status
                kotlinx.coroutines.delay(300)

                // Then fetch the real status
                refreshStatus()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = "Erro ao enviar comando: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    // Fetch status when screen opens
    LaunchedEffect(Unit) {
        refreshStatus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Controle de porta e luz via servidor (relay).")
            Spacer(modifier = Modifier.height(16.dp))

            // STATUS INDICATORS
            Text(
                text = "Porta: " + when (doorOpen) {
                    true -> "Aberta"
                    false -> "Fechada"
                    null -> "Desconhecida"
                }
            )
            Text(
                text = "Luz: " + when (lightOn) {
                    true -> "Ligada"
                    false -> "Desligada"
                    null -> "Desconhecida"
                }
            )

            if (loading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Atualizando...")
            }

            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Erro: $it")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { sendCommand("door_on") }
            ) {
                Text("Abrir a Porta")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val action = if (lightOn == true) "light_off" else "light_on"
                    sendCommand(action)
                }
            ) {
                Text(
                    if (lightOn == true) "Apagar a Luz" else "Acender a Luz"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { refreshStatus() }
            ) {
                Text("Atualizar status")
            }
        }

        Image(
            painter = painterResource(id = R.drawable.rasieee),
            contentDescription = "RAS Logo",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(80.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoorControlPagePreview() {
    DoorControlPage()
}
