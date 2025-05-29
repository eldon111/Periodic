package com.emathias.periodic.ui.todoitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.ui.todoitem.TodoItemEvent.AddItem
import com.emathias.periodic.ui.todoitem.TodoItemEvent.Check
import com.emathias.periodic.ui.todoitem.TodoItemEvent.Uncheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoItemViewModel(
    private val todoItemDao: TodoItemDao,
) : ViewModel() {

    private val _state = MutableStateFlow(TodoItemState())
    private val _todoItems =
        todoItemDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _todoItems) { state, scheduledItems ->
        state.copy(todoItems = scheduledItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoItemState())

    fun onEvent(event: TodoItemEvent) {
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

            is AddItem -> viewModelScope.launch {
                todoItemDao.insert(event.todoItem)
            }
        }
    }
}
