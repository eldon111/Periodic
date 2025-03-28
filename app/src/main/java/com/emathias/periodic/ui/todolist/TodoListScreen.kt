package com.emathias.periodic.ui.todolist

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emathias.periodic.db.entities.TodoItem
import com.emathias.periodic.ui.shared.PeriodicTopAppBar
import com.emathias.periodic.ui.todolist.createdialog.TodoItemCreationDialog
import kotlin.random.Random

@Composable
fun TodoListScreen(
    state: TodoListState,
    onEvent: (TodoListEvent) -> Unit
) {
    Scaffold(
        topBar = { PeriodicTopAppBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TodoListEvent.ShowDialog) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add item"
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        TodoList(
            state.todoItems,
            onEvent,
            modifier = Modifier.padding(innerPadding)
        )
        if (state.showingDialog) {
            TodoItemCreationDialog(onEvent)
        }
    }
}

@Composable
fun TodoList(
    todoItems: List<TodoItem>,
    onEvent: (TodoListEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(todoItems) { todoItem ->
            CheckableTodoItem(
                todoItem,
                onEvent
            )
        }
    }
}

@Composable
fun CheckableTodoItem(
    todoItem: TodoItem,
    onEvent: (TodoListEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = todoItem.checked,
                onValueChange = { checked ->
                    when (checked) {
                        true -> onEvent(TodoListEvent.Check(todoItem))
                        false -> onEvent(TodoListEvent.Uncheck(todoItem))
                    }
                },
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todoItem.checked,
            onCheckedChange = null,
            modifier = modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = todoItem.text,
            fontSize = 40.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListScreenPreview() {
    TodoListScreen(
        TodoListState(
            (1..30).map { TodoItem(Random.nextLong(), "Item $it") }
        )
    ) { e -> }
}