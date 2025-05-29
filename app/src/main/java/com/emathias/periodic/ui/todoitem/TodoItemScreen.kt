package com.emathias.periodic.ui.todoitem

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
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
import kotlin.random.Random

@Composable
fun TodoItemScreen(
    state: TodoItemState,
    onEvent: (TodoItemEvent) -> Unit,
    onMenuClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            PeriodicTopAppBar(
                title = "To-Do Items",
                onMenuClick = onMenuClick
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { onEvent(TodoItemEvent.ShowAddDialog) }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Add item"
//                )
//            }
//        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        TodoItemList(
            state.todoItems,
            onEvent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun TodoItemList(
    todoItems: List<TodoItem>,
    onEvent: (TodoItemEvent) -> Unit,
    modifier: Modifier = Modifier,
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
    onEvent: (TodoItemEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = todoItem.checked,
                onValueChange = { checked ->
                    when (checked) {
                        true -> onEvent(TodoItemEvent.Check(todoItem))
                        false -> onEvent(TodoItemEvent.Uncheck(todoItem))
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
fun TodoItemScreenPreview() {
    TodoItemScreen(
        TodoItemState(
            (1..30).map { TodoItem(Random.nextLong(), "Item $it") }
        ),
        onEvent = { },
        onMenuClick = { },
    )
}
