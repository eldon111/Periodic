package com.emathias.periodic.ui.scheduleditem

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
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.ui.scheduleditem.createdialog.ScheduledItemConfirmationDialog
import com.emathias.periodic.ui.scheduleditem.createdialog.ScheduledItemCreationDialog
import com.emathias.periodic.ui.shared.PeriodicTopAppBar
import java.time.Instant
import kotlin.random.Random

@Composable
fun ScheduledItemScreen(
    state: ScheduledItemState,
    onEvent: (ScheduledItemEvent) -> Unit,
    onMenuClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            PeriodicTopAppBar(
                title = "Scheduled Items",
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(ScheduledItemEvent.ShowAddDialog) }
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
        ScheduledItemList(
            state.scheduledItems,
            onEvent,
            modifier = Modifier.padding(innerPadding)
        )
        if (state.showingAddDialog) {
            ScheduledItemCreationDialog(onEvent)
        } else if (state.showingConfirmDialog) {
            ScheduledItemConfirmationDialog(state, onEvent)
        }
    }
}

@Composable
fun ScheduledItemList(
    scheduledItems: List<ScheduledItem>,
    onEvent: (ScheduledItemEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(scheduledItems) { scheduledItem ->
            CheckableTodoItem(
                scheduledItem,
                onEvent
            )
        }
    }
}

@Composable
fun CheckableTodoItem(
    scheduledItem: ScheduledItem,
    onEvent: (ScheduledItemEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .toggleable(
                value = false,
                onValueChange = {},
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = null,
            modifier = modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = scheduledItem.title,
            fontSize = 40.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduledItemScreenPreview() {
    ScheduledItemScreen(
        ScheduledItemState(
            (1..30).map { ScheduledItem(Random.nextLong(), "Item $it", "desc", Instant.now()) }
        ),
        onEvent = { },
        onMenuClick = { },
    )
}
