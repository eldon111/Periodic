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
import androidx.compose.material3.Checkbox
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
    Scaffold(
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
            .clickable(
                onClickLabel = "View scheduled item",
                onClick = { onScheduledItemClick(todoItem.scheduledItemId) },
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
