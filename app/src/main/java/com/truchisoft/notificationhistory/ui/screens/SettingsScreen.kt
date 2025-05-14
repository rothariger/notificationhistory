package com.truchisoft.notificationhistory.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.truchisoft.notificationhistory.ThemeMode
import com.truchisoft.notificationhistory.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeSettings by viewModel.themeSettings.collectAsState()
    val ignoredMessages by viewModel.ignoredMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Tema",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ThemeOption(
                title = "Predeterminado del sistema",
                selected = themeSettings == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
            )

            ThemeOption(
                title = "Claro",
                selected = themeSettings == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
            )

            ThemeOption(
                title = "Oscuro",
                selected = themeSettings == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
            )

            Text(
                text = "Mensajes ignorados",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            if (ignoredMessages.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hay mensajes en el diccionario de ignorados",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn {
                    items(ignoredMessages) { ignoredMessage ->
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                        ListItem(
                            headlineContent = { Text(ignoredMessage.pattern) },
                            supportingContent = {
                                Text("Añadido: ${dateFormat.format(ignoredMessage.dateAdded)}")
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeIgnoredMessage(ignoredMessage.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        val (radioButton, text) = createRefs()

        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.constrainAs(radioButton) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.constrainAs(text) {
                start.linkTo(radioButton.end, 16.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
    }
}
