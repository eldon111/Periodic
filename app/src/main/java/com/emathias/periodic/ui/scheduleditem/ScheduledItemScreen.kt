package com.emathias.periodic.ui.scheduleditem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.ui.scheduleditem.createdialog.ScheduledItemCreationDialog
import com.emathias.periodic.ui.shared.PeriodicTopAppBar
import com.emathias.periodic.model.Schedule
import com.emathias.periodic.util.RecurrenceFormatter
import com.emathias.periodic.util.DateTimeUtils
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.random.Random

private const val SCHEDULED_ITEM_HIGHLIGHT_MILLIS = 1_500L

@Composable
fun ScheduledItemScreen(
    state: ScheduledItemState,
    onEvent: (ScheduledItemEvent) -> Unit,
    onMenuClick: () -> Unit = {},
    scheduledItemIdToFocus: Long? = null,
    onScheduledItemFocused: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    var highlightedItemId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.scheduledItems, scheduledItemIdToFocus) {
        scheduledItemIdToFocus?.let { itemId ->
            val index = state.scheduledItems.indexOfFirst { it.id == itemId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
                highlightedItemId = itemId
                onScheduledItemFocused()
            }
        }
    }
    LaunchedEffect(highlightedItemId) {
        if (highlightedItemId != null) {
            delay(SCHEDULED_ITEM_HIGHLIGHT_MILLIS)
            highlightedItemId = null
        }
    }

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
            listState = listState,
            modifier = Modifier.padding(innerPadding)
        )
        if (state.showingAddDialog) {
            ScheduledItemCreationDialog(onEvent)
        }
    }
}

@Composable
fun ScheduledItemList(
    scheduledItems: List<ScheduledItem>,
    onEvent: (ScheduledItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(scheduledItems) { scheduledItem ->
            ScheduledItemInfoBox(
                scheduledItem = scheduledItem,
                onEvent = onEvent,
                highlighted = scheduledItem.id == highlightedItemId,
            )
        }
    }
}

@Composable
fun ScheduledItemInfoBox(
    scheduledItem: ScheduledItem,
    onEvent: (ScheduledItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val containerColor by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "scheduled item highlight",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = scheduledItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val formattedFirstOccurrence =
                DateTimeUtils.formatDate(scheduledItem.firstOccurrence)
            val timeText = if (scheduledItem.repeats) "Starts" else "At"
            Text(
                text = "${timeText}: $formattedFirstOccurrence",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Repeats status with icon and schedule description
            if (scheduledItem.repeats) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Repeating",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = RecurrenceFormatter.describe(scheduledItem.schedule),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expiration (if set)
            scheduledItem.expiration?.let { expiration ->
                Text(
                    text = "Expires: ${DateTimeUtils.formatDate(expiration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Preview(
    showBackground = true,
    device = "id:pixel_7_pro",
    showSystemUi = true,
)
@Composable
fun ScheduledItemScreenPreview() {
    ScheduledItemScreen(
        ScheduledItemState(
            listOf(
                ScheduledItem(
                    Random.nextLong(),
                    "Pick up kid from school",
                    "desc",
                    Schedule.of(Instant.now(), "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR")!!,
                ),
                ScheduledItem(
                    Random.nextLong(),
                    "Pick up dry cleaning",
                    "desc",
                    Schedule.oneTime(OffsetDateTime.now().plusDays(1).toInstant()),
                ),
            )
        ),
        onEvent = { },
        onMenuClick = { },
    )
}
