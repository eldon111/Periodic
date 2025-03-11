package com.emathias.periodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emathias.periodic.models.TodoItem
import com.emathias.periodic.models.TodoListViewModel
import com.emathias.periodic.ui.theme.PeriodicTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeriodicTheme {
                Scaffold(
                    topBar = { PeriodicTopAppBar() },
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) { innerPadding ->
                    TodoList(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicTopAppBar() {
    TopAppBar(
        title = { Text("Periodic") },
        navigationIcon = {
            IconButton(onClick = { /* TODO: do something */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        }
    )
}

@Composable
fun TodoList(
    modifier: Modifier = Modifier,
    todoListViewModel: TodoListViewModel = TodoListViewModel()
) {
    TodoList(
        todoListViewModel.todoItems,
        todoListViewModel::updateItemChecked,
        modifier
    )
}

@Composable
fun TodoList(
    todoItems: List<TodoItem>,
    onItemCheckedUpdate: (UUID, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(todoItems) { todoItem ->
            CheckableTodoItem(
                todoItem,
                onItemCheckedUpdate = {
                    onItemCheckedUpdate.invoke(todoItem.id, it)
                }
            )
        }
    }
}

@Composable
fun CheckableTodoItem(
    todoItem: TodoItem,
    onItemCheckedUpdate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = todoItem.checked,
                onValueChange = onItemCheckedUpdate::invoke,
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
fun TodoListPreview(modifier: Modifier = Modifier) {
    PeriodicTheme {
        TodoList()
    }
}