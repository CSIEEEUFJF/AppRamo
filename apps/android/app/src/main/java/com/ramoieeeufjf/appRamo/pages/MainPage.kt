package com.ramoieeeufjf.appRamo.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clickable { onProfileClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = profilePictureUrl ?: R.drawable.ic_launcher_foreground
                ),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(text = "Bem vindo, ${userName ?: "Usuário"}")
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ieeelogo),
                contentDescription = "IEEE Logo"
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onChapterTasksClick) {
                Text(stringResource(id = R.string.tarefas_do_capitulo))
            }
            Button(onClick = onChapterCalendarClick) {
                Text(stringResource(id = R.string.calendario_do_capitulo))
            }
            Button(onClick = onRoomControlClick) {
                Text(stringResource(id = R.string.controle_da_sala))
            }
            Button(onClick = onBranchMembersClick) {
                Text(stringResource(id = R.string.membros_do_ramo))
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.rasieee),
                contentDescription = "RAS Logo",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.iaslogo),
                contentDescription = "IAS Logo",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    MainPage("Rafael", null, {}, {}, {}, {}, {})
}
