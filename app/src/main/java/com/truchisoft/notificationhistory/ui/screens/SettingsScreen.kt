package com.truchisoft.notificationhistory.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truchisoft.notificationhistory.LocaleMode
import com.truchisoft.notificationhistory.R
import com.truchisoft.notificationhistory.ThemeMode
import com.truchisoft.notificationhistory.ui.components.SettingsItem
import com.truchisoft.notificationhistory.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeSettings.collectAsState()
    val localeMode by viewModel.localeSettings.collectAsState()
    val ignoredMessages by viewModel.ignoredMessages.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Estado para PullToRefresh
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = pullToRefreshState
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                item {
                    Text(
                        text = stringResource(R.string.appearance),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(R.string.theme),
                        subtitle = when (themeMode) {
                            ThemeMode.LIGHT -> stringResource(R.string.light)
                            ThemeMode.DARK -> stringResource(R.string.dark)
                            ThemeMode.SYSTEM -> stringResource(R.string.system_default)
                        },
                        onClick = {
                            // Acciones para cambiar el tema
                        }
                    )
                }

                item {
                    SettingsItem(
                        title = stringResource(R.string.language),
                        subtitle = when (localeMode) {
                            LocaleMode.ENGLISH -> stringResource(R.string.english)
                            LocaleMode.SPANISH -> stringResource(R.string.spanish)
                            LocaleMode.GERMAN -> stringResource(R.string.german)
                            LocaleMode.FRENCH -> stringResource(R.string.french)
                            LocaleMode.ITALIAN -> stringResource(R.string.italian)
                            LocaleMode.PORTUGUESE -> stringResource(R.string.portuguese)
                            else -> stringResource(R.string.system_language)
                        },
                        onClick = {
                            // Acciones para cambiar el idioma
                        }
                    )
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = stringResource(R.string.ignored_messages),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (ignoredMessages.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_ignored_messages),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(ignoredMessages) { ignoredMessage ->
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                        ListItem(
                            headlineContent = { Text(ignoredMessage.pattern) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.added_on, dateFormat.format(ignoredMessage.dateAdded))
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeIgnoredMessage(ignoredMessage.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
