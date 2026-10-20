package com.emathias.periodic.ui.todoitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.ui.todoitem.TodoItemEvent.Check
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class TodoItemViewModel(
    private val todoItemDao: TodoItemDao,
) : ViewModel() {

    private val _openItems =
        todoItemDao.getOpen()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    private val _completedItems =
        todoItemDao.getCompleted()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_openItems, _completedItems) { open, completed ->
        TodoItemState(openItems = open, completedItems = completed, today = LocalDate.now())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoItemState())

    fun onEvent(event: TodoItemEvent) {
        when (event) {
            is Check -> viewModelScope.launch {
                todoItemDao.update(event.todoItem.copy(completedAt = Instant.now()))
            }
        }
    }
}
