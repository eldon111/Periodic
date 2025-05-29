package com.emathias.periodic.ui.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.service.AiEnhancedTodoService
import com.emathias.periodic.ui.todolist.TodoListEvent.AddItem
import com.emathias.periodic.ui.todolist.TodoListEvent.Check
import com.emathias.periodic.ui.todolist.TodoListEvent.ConfirmScheduledItem
import com.emathias.periodic.ui.todolist.TodoListEvent.GenerateItem
import com.emathias.periodic.ui.todolist.TodoListEvent.HideAddDialog
import com.emathias.periodic.ui.todolist.TodoListEvent.HideConfirmDialog
import com.emathias.periodic.ui.todolist.TodoListEvent.ShowAddDialog
import com.emathias.periodic.ui.todolist.TodoListEvent.ShowConfirmDialog
import com.emathias.periodic.ui.todolist.TodoListEvent.Uncheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoListViewModel(
    private val todoItemDao: TodoItemDao,
    private val scheduledItemDao: ScheduledItemDao,
    private val aiService: AiEnhancedTodoService,
) : ViewModel() {

    private val _state = MutableStateFlow(TodoListState())
    private val _todoItems =
        todoItemDao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _todoItems) { state, todoItems ->
        state.copy(todoItems = todoItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoListState())

    fun onEvent(event: TodoListEvent) {
        when (event) {
            is Check -> {
                viewModelScope.launch {
                    todoItemDao.update(event.todoItem.copy(checked = true))
                }
            }

            is Uncheck -> {
                viewModelScope.launch {
                    todoItemDao.update(event.todoItem.copy(checked = false))
                }
            }

            ShowAddDialog -> _state.update { it.copy(showingAddDialog = true) }

            HideAddDialog -> _state.update { it.copy(showingAddDialog = false) }

            is ShowConfirmDialog -> _state.update { it.copy(showingConfirmDialog = true) }

            HideConfirmDialog -> _state.update {
                it.copy(
                    showingConfirmDialog = false,
                    pendingScheduledItem = null,
                )
            }

            is AddItem -> viewModelScope.launch {
                todoItemDao.insert(event.todoItem)
            }

            is GenerateItem -> viewModelScope.launch {
                val scheduledItem = aiService.generateScheduledItem(event.prompt).getOrThrow()
                _state.update {
                    it.copy(
                        showingConfirmDialog = true,
                        pendingScheduledItem = scheduledItem,
                    )
                }
                onEvent(HideAddDialog)
                onEvent(ShowConfirmDialog)
            }

            is ConfirmScheduledItem -> viewModelScope.launch {
                scheduledItemDao.insert(event.scheduledItem)
                onEvent(HideConfirmDialog)
            }
        }
    }
}