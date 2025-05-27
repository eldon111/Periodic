package com.emathias.periodic.ui.todolist.createdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.TodoItem
import com.emathias.periodic.ui.todolist.TodoListEvent
import com.emathias.periodic.ui.todolist.TodoListEvent.ConfirmScheduledItem
import com.emathias.periodic.ui.todolist.TodoListEvent.HideConfirmDialog
import com.emathias.periodic.ui.todolist.TodoListState
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

@Composable
fun ScheduledItemConfirmationDialog(state: TodoListState, onEvent: (TodoListEvent) -> Unit) {
    var pendingScheduledItem by remember {
        mutableStateOf(
            state.pendingScheduledItem ?: ScheduledItem(
                title = "",
                description = "",
                firstOccurrence = Instant.now()
            )
        )
    }

    Dialog(onDismissRequest = { onEvent(HideConfirmDialog) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextField(
                    value = pendingScheduledItem.title,
                    singleLine = true,
                    onValueChange = {
                        pendingScheduledItem = pendingScheduledItem.copy(title = it)
                    },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                TextField(
                    value = pendingScheduledItem.description,
                    singleLine = false,
                    onValueChange = {
                        pendingScheduledItem = pendingScheduledItem.copy(description = it)
                    },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .toggleable(
                            value = pendingScheduledItem.repeats,
                            onValueChange = {
                                pendingScheduledItem = pendingScheduledItem.copy(repeats = it)
                            },
                            role = Role.Checkbox
                        )
                ) {
                    Checkbox(
                        checked = pendingScheduledItem.repeats,
                        onCheckedChange = null
                    )
                    Text(
                        text = "Repeats",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (pendingScheduledItem.repeats) {
                    TextField(
                        value = pendingScheduledItem.firstOccurrence.atZone(ZoneId.systemDefault())
                            .toString(),
                        singleLine = true,
//                    onValueChange = { pendingScheduledItem = pendingScheduledItem.copy(description = it) },
                        onValueChange = { },
                        label = { Text("First Occurrence") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    TextField(
                        value = pendingScheduledItem.intervalInMinutes.toString(),
                        singleLine = true,
//                        onValueChange = { pendingScheduledItem = pendingScheduledItem.copy(description = it) },
                        onValueChange = { },
                        label = { Text("Interval in Minutes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                    TextField(
                        value = pendingScheduledItem.expiration?.atZone(ZoneId.systemDefault())
                            ?.toString()
                            ?: "never",
                        singleLine = true,
//                    onValueChange = { pendingScheduledItem = pendingScheduledItem.copy(description = it) },
                        onValueChange = { },
                        label = { Text("Expiration") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                } else {
                    TextField(
                        value = pendingScheduledItem.firstOccurrence.atZone(ZoneId.systemDefault())
                            .toString(),
                        singleLine = true,
//                    onValueChange = { pendingScheduledItem = pendingScheduledItem.copy(description = it) },
                        onValueChange = { },
                        label = { Text("Time") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
                Button(
                    onClick = { onEvent(ConfirmScheduledItem(pendingScheduledItem)) },
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScheduledItemConfirmationDialogPreview() {
    ScheduledItemConfirmationDialog(
        TodoListState(
            (1..30).map { TodoItem(Random.nextLong(), "Item $it") }
        )
    ) { f -> }
}