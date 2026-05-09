package com.ramoieeeufjf.appRamo.pages

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPage(onRegisterClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var chapterPosition by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    val chapters = listOf("RAS", "IAS", "PES", "WIE", "SIGHT", "EdSoc", "Diretoria")
    var expanded by remember { mutableStateOf(false) }
    var selectedChapters by remember { mutableStateOf(emptySet<String>()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val cal = Calendar.getInstance()
            cal.set(selectedYear, selectedMonth, selectedDay)
            birthDate = cal.time
        }, year, month, day
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = if (selectedChapters.isEmpty()) "" else selectedChapters.joinToString(),
                onValueChange = { },
                label = { Text("Capítulo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                chapters.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectionOption in selectedChapters,
                                    onCheckedChange = {
                                        selectedChapters = if (it) {
                                            selectedChapters + selectionOption
                                        } else {
                                            selectedChapters - selectionOption
                                        }
                                    }
                                )
                                Text(selectionOption)
                            }
                        },
                        onClick = {
                             selectedChapters = if (selectionOption in selectedChapters) {
                                selectedChapters - selectionOption
                            } else {
                                selectedChapters + selectionOption
                            }
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = chapterPosition,
            onValueChange = { chapterPosition = it },
            label = { Text("Cargo no Capítulo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = birthDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "Selecione a data de nascimento",
            onValueChange = { },
            label = { Text("Data de Nascimento") },
            readOnly = true,
            modifier = Modifier.clickable { datePickerDialog.show() }.fillMaxWidth()
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de Telefone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onRegisterClick) {
            Text("Registrar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationPagePreview() {
    RegistrationPage(onRegisterClick = {})
}
