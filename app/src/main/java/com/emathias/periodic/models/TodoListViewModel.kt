package com.emathias.periodic.models

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.util.UUID

class TodoListViewModel : ViewModel() {

    private val list = mutableStateListOf(*(1..30).map { TodoItem("Item $it") }.toTypedArray())

    // TODO: load items from somewhere
    val todoItems = list

    // Business logic
    fun updateItemChecked(id: UUID, checked: Boolean) {
        println("updating item $id to $checked")
        val index = list.indexOfFirst { it.id == id }
        list[index] = list[index].copy(checked = checked)
        /* ... */
        println("items: ${todoItems.toList()}")
    }
}