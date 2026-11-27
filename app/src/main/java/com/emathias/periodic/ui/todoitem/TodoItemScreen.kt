package com.emathias.periodic.ui.todoitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.ZoneOffset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emathias.periodic.db.entities.TodoItem
import com.emathias.periodic.ui.shared.PeriodicTopAppBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@Composable
fun TodoItemScreen(
    state: TodoItemState,
    onEvent: (TodoItemEvent) -> Unit,
    onMenuClick: () -> Unit = {},
    onScheduledItemClick: (Long) -> Unit = {},
) {
    var showAddDialog by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add to-do")
            }
        },
        topBar = {
            PeriodicTopAppBar(
                title = "To-Do Items",
                onMenuClick = onMenuClick
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        TodoItemList(
            state,
            onEvent,
            modifier = Modifier.padding(innerPadding),
            onScheduledItemClick = onScheduledItemClick,
        )
    }
    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, dueDate ->
                onEvent(TodoItemEvent.Add(title, dueDate))
                showAddDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New to-do") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs doing?") },
                    singleLine = true,
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Due " + dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onConfirm(title.trim(), dueDate) },
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
fun TodoItemList(
    state: TodoItemState,
    onEvent: (TodoItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    onScheduledItemClick: (Long) -> Unit = {},
) {
    LazyColumn(modifier = modifier) {
        item { SectionHeader("Open") }
        if (state.openItems.isEmpty()) {
            item {
                Text(
                    "Nothing to do",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        items(state.openItems, key = { it.id }) { todoItem ->
            TodoItemRow(todoItem, state.today, onEvent, onScheduledItemClick = onScheduledItemClick)
        }
        if (state.completedItems.isNotEmpty()) {
            item { SectionHeader("Completed") }
            items(state.completedItems, key = { it.id }) { todoItem ->
                TodoItemRow(todoItem, state.today, onEvent, onScheduledItemClick = onScheduledItemClick)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun TodoItemRow(
    todoItem: TodoItem,
    today: LocalDate,
    onEvent: (TodoItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    onScheduledItemClick: (Long) -> Unit = {},
) {
    val overdue = !todoItem.completed && todoItem.dueDate.isBefore(today)
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .then(
                todoItem.scheduledItemId?.let { id ->
                    Modifier.clickable(
                        onClickLabel = "View scheduled item",
                        onClick = { onScheduledItemClick(id) },
                    )
                } ?: Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todoItem.completed,
            // Completing is one-way for now; completed items cannot be reopened.
            enabled = !todoItem.completed,
            onCheckedChange = { onEvent(TodoItemEvent.Check(todoItem)) },
        )
        Column(Modifier.padding(start = 8.dp)) {
            Text(text = todoItem.title, style = MaterialTheme.typography.titleMedium)
            if (todoItem.description.isNotBlank() && todoItem.description != todoItem.title) {
                Text(text = todoItem.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = dueLabel(todoItem, today),
                style = MaterialTheme.typography.bodySmall,
                color = if (overdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun dueLabel(todoItem: TodoItem, today: LocalDate): String {
    if (todoItem.completed) {
        return "Due " + todoItem.dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    val days = ChronoUnit.DAYS.between(today, todoItem.dueDate)
    return when {
        days == 0L -> "Due today"
        days == -1L -> "1 day overdue"
        days < 0 -> "${-days} days overdue"
        days == 1L -> "Due tomorrow"
        else -> "Due in $days days"
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemScreenPreview() {
    val today = LocalDate.now()
    TodoItemScreen(
        TodoItemState(
            openItems = (1..5).map {
                TodoItem(it.toLong(), 1, "Item $it", "Description $it", today.minusDays(it - 2L))
            },
            today = today,
        ),
        onEvent = { },
        onMenuClick = { },
    )
}
