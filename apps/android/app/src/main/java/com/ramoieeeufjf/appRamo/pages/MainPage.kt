package com.ramoieeeufjf.appRamo.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ramoieeeufjf.appRamo.R

@Composable
fun MainPage(
    userName: String?,
    profilePictureUrl: String?,
    onProfileClick: () -> Unit,
    onChapterTasksClick: () -> Unit,
    onChapterCalendarClick: () -> Unit,
    onRoomControlClick: () -> Unit,
    onBranchMembersClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onProfileClick() },
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = profilePictureUrl ?: R.drawable.ic_launcher_foreground
                    ),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bem-vindo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userName ?: "Usuário",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ieeelogo),
                contentDescription = "IEEE Logo",
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(96.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeActionButton(text = stringResource(id = R.string.tarefas_do_capitulo), onClick = onChapterTasksClick)
                HomeActionButton(text = stringResource(id = R.string.calendario_do_capitulo), onClick = onChapterCalendarClick)
                Button(
                    onClick = onRoomControlClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(id = R.string.controle_da_sala))
                }
                HomeActionButton(text = stringResource(id = R.string.membros_do_ramo), onClick = onBranchMembersClick)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.cslogo),
                contentDescription = "CS Logo",
                modifier = Modifier
                    .padding(horizontal = 22.dp, vertical = 14.dp)
                    .size(132.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun HomeActionButton(text: String, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    MainPage("Rafael", null, {}, {}, {}, {}, {})
}
