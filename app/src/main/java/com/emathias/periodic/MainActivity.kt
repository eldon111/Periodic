package com.emathias.periodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.emathias.periodic.db.dao.AppDatabase
import com.emathias.periodic.service.AiEnhancedTodoService
import com.emathias.periodic.ui.scheduleditem.ScheduledItemScreen
import com.emathias.periodic.ui.scheduleditem.ScheduledItemViewModel
import com.emathias.periodic.ui.theme.PeriodicTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var aiService: AiEnhancedTodoService

    @Suppress("UNCHECKED_CAST")
    private val viewModel by viewModels<ScheduledItemViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ScheduledItemViewModel(
                        db.scheduledItemDao(),
                        aiService
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
                val state by viewModel.state.collectAsState()
                ScheduledItemScreen(state, viewModel::onEvent)
            }
        }

    }


}
