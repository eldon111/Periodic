package com.emathias.periodic.ui.scheduleditem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.api.ScheduledItemApiService
import com.emathias.periodic.service.AiEnhancedTodoService
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ConfirmScheduledItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.GenerateItem
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

class ScheduledItemViewModel(
    private val scheduledItemApiService: ScheduledItemApiService,
    private val aiService: AiEnhancedTodoService,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledItemState())
    private val _scheduledItems =
        scheduledItemApiService.getAllScheduledItems()
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

            is GenerateItem -> viewModelScope.launch {
                val jsonResult = aiService.generateScheduledItem(event.prompt).getOrThrow()
                onEvent(ShowConfirmDialog(jsonResult))
                onEvent(HideAddDialog)
            }

            is ConfirmScheduledItem -> viewModelScope.launch {
                scheduledItemApiService.insertScheduledItem(event.scheduledItem)
                onEvent(HideConfirmDialog)
            }

            RefreshScheduledItems -> {
                scheduledItemApiService.refreshScheduledItems()
            }
        }
    }
}
