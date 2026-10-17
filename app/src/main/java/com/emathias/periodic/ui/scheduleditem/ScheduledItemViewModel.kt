package com.emathias.periodic.ui.scheduleditem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.util.CronUtils
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ConfirmScheduledItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.CreateItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideAddDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideConfirmDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.RefreshScheduledItems
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ShowAddDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ShowConfirmDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

class ScheduledItemViewModel(
    private val scheduledItemDao: ScheduledItemDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledItemState())
    private val _scheduledItems =
        scheduledItemDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _scheduledItems) { state, scheduledItems ->
        state.copy(scheduledItems = scheduledItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduledItemState())

    fun onEvent(event: ScheduledItemEvent) {
        when (event) {
            ShowAddDialog -> _state.update { it.copy(showingAddDialog = true) }

            HideAddDialog -> _state.update { it.copy(showingAddDialog = false) }

            is ShowConfirmDialog -> _state.update {
                it.copy(
                    showingConfirmDialog = true,
                    pendingScheduledItem = event.scheduledItem,
                )
            }

            HideConfirmDialog -> _state.update {
                it.copy(
                    showingConfirmDialog = false,
                    pendingScheduledItem = null,
                )
            }

            is CreateItem -> {
                val cron = event.cron.trim().takeIf { it.isNotEmpty() }
                    ?.let { CronUtils.parseCronExpression(it) }
                val item = ScheduledItem(
                    title = event.title.trim(),
                    description = event.description.trim().ifEmpty { event.title.trim() },
                    startsAt = Instant.now(),
                    repeats = cron != null,
                    cronExpression = cron,
                )
                onEvent(ShowConfirmDialog(item))
                onEvent(HideAddDialog)
            }

            is ConfirmScheduledItem -> viewModelScope.launch {
                scheduledItemDao.insert(event.scheduledItem)
                onEvent(HideConfirmDialog)
            }

            RefreshScheduledItems -> Unit
        }
    }
}
