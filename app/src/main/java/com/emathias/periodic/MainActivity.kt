package com.emathias.periodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emathias.periodic.api.ScheduledItemApiService
import com.emathias.periodic.db.dao.AppDatabase
import com.emathias.periodic.service.AiEnhancedTodoService
import com.emathias.periodic.ui.navigation.NavigationDrawerContent
import com.emathias.periodic.ui.navigation.Screen
import com.emathias.periodic.ui.scheduleditem.ScheduledItemScreen
import com.emathias.periodic.ui.scheduleditem.ScheduledItemViewModel
import com.emathias.periodic.ui.theme.PeriodicTheme
import com.emathias.periodic.ui.todoitem.TodoItemScreen
import com.emathias.periodic.ui.todoitem.TodoItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var scheduledItemApiService: ScheduledItemApiService

    @Inject
    lateinit var aiService: AiEnhancedTodoService

    @Suppress("UNCHECKED_CAST")
    private val scheduledItemViewModel by viewModels<ScheduledItemViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScheduledItemViewModel(
                        scheduledItemApiService
                    ) as T
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val todoItemViewModel by viewModels<TodoItemViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TodoItemViewModel(
                        db.todoItemDao()
                    ) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeriodicTheme {
                var currentScreen by remember { mutableStateOf(Screen.ScheduledItems) }
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        NavigationDrawerContent(
                            currentScreen = currentScreen,
                            onScreenSelected = { screen ->
                                currentScreen = screen
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                    }
                ) {
                    when (currentScreen) {
                        Screen.ScheduledItems -> {
                            val state by scheduledItemViewModel.state.collectAsState()
                            ScheduledItemScreen(
                                state = state,
                                onEvent = scheduledItemViewModel::onEvent,
                                onMenuClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        }

                        Screen.TodoList -> {
                            val state by todoItemViewModel.state.collectAsState()
                            TodoItemScreen(
                                state = state,
                                onEvent = todoItemViewModel::onEvent,
                                onMenuClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }


}
