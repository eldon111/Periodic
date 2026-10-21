package com.emathias.periodic.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emathias.periodic.ui.shared.PeriodicTopAppBar
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    reminderTime: LocalTime,
    onReminderTimeChange: (LocalTime) -> Unit,
    onSendNotificationsNow: () -> Unit,
    onRunTodoCreationNow: () -> Unit,
    onMenuClick: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PeriodicTopAppBar(title = "Settings", onMenuClick = onMenuClick) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Daily reminder time")
                TextButton(onClick = { showPicker = true }) {
                    Text(reminderTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                }
            }

            Text(
                text = "Developer",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            Button(onClick = onSendNotificationsNow) {
                Text("Send today's notifications now")
            }
            Button(
                onClick = onRunTodoCreationNow,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Run to-do creation now")
            }
        }
    }

    if (showPicker) {
        val state = rememberTimePickerState(reminderTime.hour, reminderTime.minute)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Daily reminder time") },
            text = { Column { TimePicker(state = state) } },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeChange(LocalTime.of(state.hour, state.minute))
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        )
    }
}
