package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.local.DuckAliasDatabase
import com.example.data.local.SecurityPreferences
import com.example.data.repository.DuckAliasRepository
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.DuckAliasTheme
import com.example.ui.viewmodel.DuckAliasViewModel
import com.example.ui.viewmodel.DuckAliasViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { DuckAliasDatabase.getDatabase(this) }
    private val securityPreferences by lazy { SecurityPreferences(this) }
    private val repository by lazy {
        DuckAliasRepository(
            tokenDao = database.tokenDao(),
            aliasDao = database.aliasDao()
        )
    }
    private val viewModel: DuckAliasViewModel by viewModels {
        DuckAliasViewModelFactory(repository, securityPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            DuckAliasTheme(darkTheme = uiState.isDarkMode) {
                MainDashboardScreen(viewModel = viewModel)
            }
        }
    }
}
