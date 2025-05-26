package com.emathias.periodic.ui.todolist.createdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.emathias.periodic.ui.todolist.TodoListEvent
import com.emathias.periodic.ui.todolist.TodoListEvent.HideAddDialog

@Composable
fun TodoItemCreationDialog(onEvent: (TodoListEvent) -> Unit) {
    Dialog(onDismissRequest = { onEvent(HideAddDialog) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "This will create a new item.",
                    modifier = Modifier.padding(16.dp),
                )
                TextField(
                    value = "",
                    singleLine = false,
                    onValueChange = { },
                    label = { Text("Item description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Button(
                    onClick = { onEvent(HideAddDialog) },
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text("Create")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TodoItemCreationDialogPreview() {
    TodoItemCreationDialog() { f -> }
}