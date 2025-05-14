package com.truchisoft.notificationhistory.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope

@Composable
fun LaunchedEffect(
    key1: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    val rememberKey = remember { key1 }
    LaunchedEffect(rememberKey) {
        block()
    }
}
