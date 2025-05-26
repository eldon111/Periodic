package com.emathias.periodic.ui.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.service.BedrockAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoListViewModel(
    private val dao: TodoItemDao,
    private val aiService: BedrockAiService,
) : ViewModel() {

    private val _state = MutableStateFlow(TodoListState())
    private val _todoItems =
        dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _todoItems) { state, todoItems ->
        state.copy(todoItems = todoItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoListState())

    fun onEvent(event: TodoListEvent) {
        when (event) {
            is TodoListEvent.Check -> {
                viewModelScope.launch {
                    dao.update(event.todoItem.copy(checked = true))
                }
            }

            is TodoListEvent.Uncheck -> {
                viewModelScope.launch {
                    dao.update(event.todoItem.copy(checked = false))
                }
            }

            TodoListEvent.ShowAddDialog -> _state.update { it.copy(showingAddDialog = true) }

            TodoListEvent.HideAddDialog -> _state.update { it.copy(showingAddDialog = false) }

            TodoListEvent.ShowConfirmDialog -> _state.update { it.copy(showingConfirmDialog = true) }

            TodoListEvent.HideConfirmDialog -> _state.update { it.copy(showingConfirmDialog = false) }

            is TodoListEvent.AddItem -> viewModelScope.launch {
                dao.insert(event.todoItem)
            }

            is TodoListEvent.GenerateItem -> viewModelScope.launch {
                aiService.generateTextWithNova(event.prompt)
            }
        }
    }
}