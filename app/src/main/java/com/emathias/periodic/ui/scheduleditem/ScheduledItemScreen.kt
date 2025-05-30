package com.emathias.periodic.ui.scheduleditem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.ui.scheduleditem.createdialog.ScheduledItemConfirmationDialog
import com.emathias.periodic.ui.scheduleditem.createdialog.ScheduledItemCreationDialog
import com.emathias.periodic.ui.shared.PeriodicTopAppBar
import com.emathias.periodic.util.CronUtils
import com.emathias.periodic.util.DateTimeUtils
import java.time.Instant
import java.time.ZonedDateTime
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
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(scheduledItems) { scheduledItem ->
            ScheduledItemInfoBox(
                scheduledItem = scheduledItem,
                onEvent = onEvent
            )
        }
    }
}

@Composable
fun ScheduledItemInfoBox(
    scheduledItem: ScheduledItem,
    onEvent: (ScheduledItemEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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

            // First occurrence
            Text(
                text = "Starts: ${DateTimeUtils.formatDateTime(scheduledItem.firstOccurrence)}",
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
                    scheduledItem.cronExpression?.let { cron ->
                        Text(
                            text = CronUtils.formatCronExpression(cron),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expiration (if set)
            scheduledItem.expiration?.let { expiration ->
                Text(
                    text = "Expires: ${DateTimeUtils.formatDateTime(expiration)}",
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
                    Instant.now(),
                    repeats = true,
                    cronExpression = CronUtils.parseCronExpression("20 13 * * 1-5"), // Every weekday at 1:20 PM
                ),
                ScheduledItem(
                    Random.nextLong(),
                    "Pick up dry cleaning",
                    "desc",
                    ZonedDateTime.now().plusDays(1).toInstant(),
                    repeats = false,
                ),
            )
        ),
        onEvent = { },
        onMenuClick = { },
    )
}
