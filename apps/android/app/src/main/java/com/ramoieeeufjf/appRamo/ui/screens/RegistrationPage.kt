package com.ramoieeeufjf.appRamo.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.ramoieeeufjf.appRamo.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPage(
    onRegisterClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingProfilePictureUrl by remember { mutableStateOf<String?>(null) }
    var approvedChapterRoles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Solicitações do usuário. Permissões efetivas continuam em chapterRoles e devem ser aprovadas fora do app.
    var chapterRoles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val allChapters = listOf("RAS", "IAS", "PES", "WIE", "EdSoc", "Diretoria", "SIGHT")

    // Pre-fill data if in edit mode
    if (currentUser != null) {
        LaunchedEffect(Unit) {
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            name = userDoc.getString("name") ?: ""
            birthDate = userDoc.getDate("birthDate")
            email = userDoc.getString("email") ?: currentUser.email ?: ""
            phoneNumber = userDoc.getString("phoneNumber") ?: ""
            existingProfilePictureUrl = userDoc.getString("profilePictureUrl")
            approvedChapterRoles = readStringMap(userDoc.get("chapterRoles"))
            chapterRoles = readStringMap(userDoc.get("requestedChapterRoles")).ifEmpty { approvedChapterRoles }
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            birthDate = cal.time
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }
    val coroutineScope = rememberCoroutineScope()
    val storage = FirebaseStorage.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(id = R.string.create_account), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }) {
            Image(
                painter = if (imageUri != null || !existingProfilePictureUrl.isNullOrBlank()) {
                    rememberAsyncImagePainter(imageUri ?: existingProfilePictureUrl)
                } else {
                    painterResource(id = R.drawable.ic_launcher_foreground)
                },
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp).clip(CircleShape), contentScale = ContentScale.Crop
            )
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(id = R.string.nome)) }, modifier = Modifier.fillMaxWidth())

        Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
             OutlinedTextField(
                value = birthDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: stringResource(id = R.string.selecione_a_data_de_nascimento),
                onValueChange = { },
                label = { Text(stringResource(id = R.string.data_de_nascimento)) },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(id = R.string.e_mail)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), enabled = currentUser == null)
        OutlinedTextField(value = password,onValueChange = { password = it },label = { Text(stringResource(id = R.string.password)) },visualTransformation = PasswordVisualTransformation(),keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),modifier = Modifier.fillMaxWidth(),enabled = currentUser == null)

        ChapterSelection(allChapters, chapterRoles) { newRoles -> chapterRoles = newRoles }

        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text(stringResource(id = R.string.phone_number)) }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                try {
                    val uid = currentUser?.uid ?: auth.createUserWithEmailAndPassword(email, password).await().user!!.uid
                    var uploadedPictureUrl: String? = null
                    imageUri?.let { uri ->
                        val storageRef = storage.reference.child("profile_pictures/$uid")
                        storageRef.putFile(uri).await()
                        uploadedPictureUrl = storageRef.downloadUrl.await().toString()
                    }

                    val userData = mutableMapOf<String, Any>(
                        "name" to name,
                        "requestedChapterRoles" to chapterRoles,
                        "phoneNumber" to phoneNumber
                    )
                    birthDate?.let { userData["birthDate"] = it }
                    if (currentUser == null) {
                        userData["email"] = email
                        userData["chapterRoles"] = approvedChapterRoles
                    }
                    uploadedPictureUrl?.let { userData["profilePictureUrl"] = it }

                    db.collection("users").document(uid).set(userData, SetOptions.merge()).await()

                    val publicProfile = mutableMapOf<String, Any>(
                        "name" to name,
                        "chapterRoles" to approvedChapterRoles
                    )
                    val publicPictureUrl = uploadedPictureUrl ?: existingProfilePictureUrl
                    publicPictureUrl?.takeIf { it.isNotBlank() }?.let {
                        publicProfile["profilePictureUrl"] = it
                    }
                    db.collection("publicProfiles").document(uid).set(publicProfile).await()
                    onRegisterClick()

                } catch (e: Exception) {
                    Log.e("RegistrationPage", "Registration/Update failed", e)
                    Toast.makeText(context, "Action failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text(if (currentUser != null) "Update" else stringResource(id = R.string.register))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelection(allChapters: List<String>, selectedRoles: Map<String, String>, onRolesChanged: (Map<String, String>) -> Unit) {
    var chapterDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = chapterDropdownExpanded,
        onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = if (selectedRoles.isEmpty()) "" else selectedRoles.keys.joinToString(),
            onValueChange = {},
            label = { Text(stringResource(R.string.selecione_seu_capitulo_principal)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) }
        )
        ExposedDropdownMenu(
            expanded = chapterDropdownExpanded,
            onDismissRequest = { chapterDropdownExpanded = false }
        ) {
            allChapters.forEach { chapter ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = chapter in selectedRoles,
                                onCheckedChange = { isChecked ->
                                    val newRoles = selectedRoles.toMutableMap()
                                    if (isChecked) {
                                        val defaultRole = if (chapter == "Diretoria") "Presidente" else "Membro"
                                        newRoles[chapter] = defaultRole
                                    } else {
                                        newRoles.remove(chapter)
                                    }
                                    onRolesChanged(newRoles)
                                }
                            )
                            Text(chapter)
                        }
                    },
                    onClick = { /* Handle in checkbox */ }
                )
            }
        }
    }

    // Dynamically show role selectors for selected chapters
    selectedRoles.keys.forEach { chapter ->
        val rolesForChapter = if (chapter == "Diretoria") listOf("Presidente", "Vice Presidente", "Tesoureiro", "Marketing", "Webmaster") else listOf("Membro", "Presidente")
        var roleDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = roleDropdownExpanded,
            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedRoles[chapter] ?: "",
                onValueChange = {},
                label = { Text("Cargo em $chapter") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = roleDropdownExpanded,
                onDismissRequest = { roleDropdownExpanded = false }
            ) {
                rolesForChapter.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            val newRoles = selectedRoles.toMutableMap()
                            newRoles[chapter] = role
                            onRolesChanged(newRoles)
                            roleDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationPagePreview() {
    RegistrationPage(onRegisterClick = {})
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
