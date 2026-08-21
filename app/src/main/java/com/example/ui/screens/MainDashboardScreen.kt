package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AliasEntity
import com.example.ui.components.*
import com.example.ui.viewmodel.DuckAliasViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: DuckAliasViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showTokenDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedAliasForDetail by remember { mutableStateOf<AliasEntity?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // User Message Toast Effect
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppLeftDrawerSheet(
                tokens = uiState.tokens,
                activeToken = uiState.activeToken,
                isDarkMode = uiState.isDarkMode,
                onOpenAccountManager = { showTokenDialog = true },
                onSelectToken = { viewModel.selectActiveToken(it) },
                onUpdateToken = { id, label, valStr -> viewModel.updateToken(id, label, valStr) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.systemBars,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    snackbar = { snackbarData ->
                        Snackbar(
                            snackbarData = snackbarData,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            actionColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
            },
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Left Navigation Drawer",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.clickable {
                                coroutineScope.launch { drawerState.open() }
                            }
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_duckduckgo_logo),
                                        contentDescription = "DuckDuckGo Logo",
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Ducky Alias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.totalCount} Aliases Protected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Bearer Token Badge Button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    coroutineScope.launch { drawerState.open() }
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.activeToken != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                )
                                val activeLabel = uiState.activeToken?.label ?: "Accounts"
                                val displayActiveLabel = if (activeLabel.length > 12) activeLabel.take(12) + "…" else activeLabel
                                Text(
                                    text = displayActiveLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Filter Button (Replaced Theme Button position in Top Bar)
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filter & Sort Options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Duck Alias",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Search Bar Input (Single Line Placeholder)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search by service, note, account...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-selection Controls Banner if enabled
                if (uiState.isMultiSelectMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${uiState.selectedAliasIds.size} selected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TextButton(
                                    onClick = { viewModel.selectAllAliases(uiState.aliases) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Select All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (uiState.selectedAliasIds.isNotEmpty()) {
                                    Button(
                                        onClick = { viewModel.deleteSelectedAliases() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete (${uiState.selectedAliasIds.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.toggleMultiSelectMode() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Exit multi-select", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Alias List or Empty State
                if (uiState.aliases.isEmpty()) {
                    EmptyStateView(
                        searchQuery = searchQuery,
                        onCreateClick = { showCreateDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = innerPadding.calculateBottomPadding() + 96.dp)
                    ) {
                        items(
                            items = uiState.aliases,
                            key = { it.id }
                        ) { alias ->
                            AliasItemCard(
                                alias = alias,
                                onCopyClick = { viewModel.copyAliasToClipboard(context, it) },
                                onEditClick = { selectedAliasForDetail = it },
                                onDeleteClick = { viewModel.deleteAlias(it.id, it.serviceLabel) },
                                isSelectionMode = uiState.isMultiSelectMode,
                                isSelected = uiState.selectedAliasIds.contains(alias.id),
                                onSelectionToggle = { viewModel.toggleAliasSelection(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showCreateDialog) {
        CreateAliasDialog(
            tokens = uiState.tokens,
            activeToken = uiState.activeToken,
            isGenerating = uiState.isGenerating,
            onGenerate = { service, note, targetTokenId ->
                viewModel.generateNewAlias(service, note, targetTokenId) {
                    showCreateDialog = false
                }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showTokenDialog) {
        TokenManagementDialog(
            tokens = uiState.tokens,
            activeToken = uiState.activeToken,
            onSelectToken = { viewModel.selectActiveToken(it) },
            onAddToken = { label, token, active -> viewModel.addToken(label, token, active) },
            onUpdateToken = { id, label, valStr -> viewModel.updateToken(id, label, valStr) },
            onDeleteToken = { viewModel.deleteToken(it) },
            onDismiss = { showTokenDialog = false }
        )
    }

    if (showFilterDialog) {
        FilterSortDialog(
            tokens = uiState.tokens,
            selectedTokenIdFilter = uiState.selectedTokenIdFilter,
            currentSortOption = uiState.currentSortOption,
            isMultiSelectMode = uiState.isMultiSelectMode,
            onSelectTokenFilter = { viewModel.setAccountFilter(it) },
            onSelectSortOption = { viewModel.setSortOption(it) },
            onToggleMultiSelectMode = { viewModel.toggleMultiSelectMode() },
            onResetFilters = { viewModel.resetFilters() },
            onDismiss = { showFilterDialog = false }
        )
    }

    selectedAliasForDetail?.let { alias ->
        AliasDetailDialog(
            alias = alias,
            onCopyAddress = { viewModel.copyAliasToClipboard(context, it) },
            onSaveUpdate = { viewModel.updateAlias(it) },
            onDeleteAlias = { id, label -> viewModel.deleteAlias(id, label) },
            onDismiss = { selectedAliasForDetail = null }
        )
    }
}

@Composable
private fun EmptyStateView(
    searchQuery: String,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShieldMoon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = if (searchQuery.isNotEmpty()) "No Aliases Match Search" else "No Duck Aliases Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "Try clearing your search query."
                } else {
                    "Protect your real email address from spam and data breaches by generating custom @duck.com aliases."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (searchQuery.isEmpty()) {
                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Your First Duck Alias", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
