package com.example.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AliasEntity
import com.example.data.local.BearerTokenEntity
import com.example.data.repository.DuckAliasRepository
import com.example.data.repository.GenerationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.data.local.SecurityPreferences

data class DuckAliasUiState(
    val aliases: List<AliasEntity> = emptyList(),
    val tokens: List<BearerTokenEntity> = emptyList(),
    val activeToken: BearerTokenEntity? = null,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val userMessage: String? = null,
    val isDarkMode: Boolean = true,
    val selectedTokenIdFilter: Long? = null,
    val currentSortOption: String = "NEWEST",
    val isMultiSelectMode: Boolean = false,
    val selectedAliasIds: Set<Long> = emptySet()
)

class DuckAliasViewModel(
    private val repository: DuckAliasRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    private val _isGenerating = MutableStateFlow(false)
    private val _isDarkMode = MutableStateFlow(securityPreferences.isDarkMode)

    // Filtering & Sorting State
    private val _selectedTokenIdFilter = MutableStateFlow<Long?>(null)
    private val _currentSortOption = MutableStateFlow("NEWEST") // NEWEST, OLDEST, TITLE_AZ, TITLE_ZA

    // Multi-select deletion State
    private val _isMultiSelectMode = MutableStateFlow(false)
    private val _selectedAliasIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    private val filterStateFlow = combine(_selectedTokenIdFilter, _currentSortOption) { tokenFilter, sortOpt ->
        Pair(tokenFilter, sortOpt)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val appStateFlow = combine(
        _searchQuery,
        _userMessage,
        _isGenerating,
        _isDarkMode,
        filterStateFlow
    ) { query, message, generating, dark, filterPair ->
        LocalState(query, message, generating, dark, filterPair.first, filterPair.second)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DuckAliasUiState> = combine(
        appStateFlow,
        _isMultiSelectMode,
        _selectedAliasIds
    ) { state, multiSelect, selIds ->
        Tuple3(state, multiSelect, selIds)
    }.flatMapLatest { (state, multiSelect, selIds) ->
        combine(
            repository.searchAliasesFlow(state.query, "ALL"),
            repository.allTokensFlow,
            repository.activeTokenFlow,
            repository.totalAliasCountFlow
        ) { rawAliases, tokens, activeToken, total ->
            // Filter by token if specified
            val filteredList = if (state.tokenFilter != null) {
                rawAliases.filter { it.tokenId == state.tokenFilter }
            } else {
                rawAliases
            }

            // Apply Sorting
            val sortedList = when (state.sortOpt) {
                "OLDEST" -> filteredList.sortedBy { it.createdAt }
                "TITLE_AZ" -> filteredList.sortedBy { it.serviceLabel.lowercase() }
                "TITLE_ZA" -> filteredList.sortedByDescending { it.serviceLabel.lowercase() }
                else -> filteredList.sortedByDescending { it.createdAt } // "NEWEST"
            }

            DuckAliasUiState(
                aliases = sortedList,
                tokens = tokens,
                activeToken = activeToken,
                totalCount = total,
                isGenerating = state.generating,
                userMessage = state.message,
                isDarkMode = state.dark,
                selectedTokenIdFilter = state.tokenFilter,
                currentSortOption = state.sortOpt,
                isMultiSelectMode = multiSelect,
                selectedAliasIds = selIds
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DuckAliasUiState(
            isDarkMode = securityPreferences.isDarkMode
        )
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDarkMode() {
        val nextMode = !_isDarkMode.value
        _isDarkMode.value = nextMode
        securityPreferences.isDarkMode = nextMode
    }

    fun generateNewAlias(
        serviceLabel: String,
        note: String,
        targetTokenId: Long? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGenerating.value = true
            when (val result = repository.generateNewAlias(serviceLabel, note, targetTokenId)) {
                is GenerationResult.Success -> {
                    val notice = if (result.isRealApi) {
                        "✨ Duck address generated via DDG API (${result.alias.tokenLabel}): ${result.alias.address}"
                    } else {
                        "⚡ Generated Duck address (${result.alias.tokenLabel}): ${result.alias.address}"
                    }
                    _userMessage.value = notice
                    onComplete()
                }
                is GenerationResult.Error -> {
                    _userMessage.value = "Error: ${result.message}"
                }
            }
            _isGenerating.value = false
        }
    }

    fun copyAliasToClipboard(context: Context, alias: AliasEntity) {
        viewModelScope.launch {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Duck Email Alias", alias.address)
                clipboard.setPrimaryClip(clip)
                repository.incrementCopyCount(alias.id)
                _userMessage.value = "Copied ${alias.address} to clipboard!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to copy to clipboard"
            }
        }
    }

    fun toggleAliasStatus(alias: AliasEntity) {
        viewModelScope.launch {
            repository.toggleAliasStatus(alias)
            val newStatus = if (alias.status == "ACTIVE") "deactivated" else "activated"
            _userMessage.value = "Alias for ${alias.serviceLabel} $newStatus"
        }
    }

    fun updateAlias(alias: AliasEntity) {
        viewModelScope.launch {
            repository.updateAlias(alias)
            _userMessage.value = "Updated alias details for ${alias.serviceLabel}"
        }
    }

    fun deleteAlias(aliasId: Long, serviceLabel: String) {
        viewModelScope.launch {
            repository.deleteAlias(aliasId)
            _userMessage.value = "Deleted alias for $serviceLabel"
        }
    }

    fun addToken(label: String, tokenValue: String, makeActive: Boolean = true) {
        viewModelScope.launch {
            repository.addToken(label, tokenValue, makeActive)
            _userMessage.value = "Added Bearer Token: ${label.ifBlank { "Duck Account" }}"
        }
    }

    fun updateToken(tokenId: Long, label: String, tokenValue: String) {
        viewModelScope.launch {
            repository.updateToken(tokenId, label, tokenValue)
            _userMessage.value = "Updated Bearer Token: ${label.ifBlank { "Duck Account" }}"
        }
    }

    fun selectActiveToken(tokenId: Long) {
        viewModelScope.launch {
            repository.selectActiveToken(tokenId)
            _userMessage.value = "Switched active Bearer Token"
        }
    }

    fun deleteToken(tokenId: Long) {
        viewModelScope.launch {
            repository.deleteToken(tokenId)
            _userMessage.value = "Removed Bearer Token"
        }
    }

    // Filter & Sort Control
    fun setAccountFilter(tokenId: Long?) {
        _selectedTokenIdFilter.value = tokenId
    }

    fun setSortOption(sortOption: String) {
        _currentSortOption.value = sortOption
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedTokenIdFilter.value = null
        _currentSortOption.value = "NEWEST"
        _isMultiSelectMode.value = false
        _selectedAliasIds.value = emptySet()
        _userMessage.value = "Filters reset to default"
    }

    // Multi-select Deletion Control
    fun toggleMultiSelectMode() {
        val nextState = !_isMultiSelectMode.value
        _isMultiSelectMode.value = nextState
        if (!nextState) {
            _selectedAliasIds.value = emptySet()
        }
    }

    fun toggleAliasSelection(aliasId: Long) {
        val currentSet = _selectedAliasIds.value.toMutableSet()
        if (currentSet.contains(aliasId)) {
            currentSet.remove(aliasId)
        } else {
            currentSet.add(aliasId)
        }
        _selectedAliasIds.value = currentSet
    }

    fun selectAllAliases(aliases: List<AliasEntity>) {
        _selectedAliasIds.value = aliases.map { it.id }.toSet()
    }

    fun clearAliasSelection() {
        _selectedAliasIds.value = emptySet()
    }

    fun deleteSelectedAliases() {
        val idsToDelete = _selectedAliasIds.value.toList()
        if (idsToDelete.isEmpty()) return
        viewModelScope.launch {
            repository.deleteAliases(idsToDelete)
            _userMessage.value = "Deleted ${idsToDelete.size} selected alias(es)"
            _selectedAliasIds.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}

private data class Tuple3<A, B, C>(
    val a: A, val b: B, val c: C
)

private data class LocalState(
    val query: String,
    val message: String?,
    val generating: Boolean,
    val dark: Boolean,
    val tokenFilter: Long?,
    val sortOpt: String
)

class DuckAliasViewModelFactory(
    private val repository: DuckAliasRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DuckAliasViewModel::class.java)) {
            return DuckAliasViewModel(repository, securityPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
