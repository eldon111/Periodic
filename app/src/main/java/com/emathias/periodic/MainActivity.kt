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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emathias.periodic.models.TodoItem
import com.emathias.periodic.ui.theme.PeriodicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeriodicTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) { innerPadding ->
                    TodoList(
                        todoItems = listOf(
                            TodoItem("Item 1"),
                            TodoItem("Item 2"),
                            TodoItem("Item 4")
                        ),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TodoList(todoItems: List<TodoItem>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(todoItems) { todoItem ->
            CheckableTodoItem(todoItem)
        }
    }
}

@Composable
fun CheckableTodoItem(todoItem: TodoItem, modifier: Modifier = Modifier) {
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = checkedState,
                onValueChange = { onStateChange(!checkedState) },
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState,
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
        TodoList(
            listOf(
                TodoItem("Item 1"),
                TodoItem("Item 2"),
                TodoItem("Item 4")
            ), modifier
        )
    }
}