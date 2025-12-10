package com.readmark.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.readmark.data.model.ConnectionState
import com.readmark.data.model.ProcessingResult
import com.readmark.data.model.WorkMode
import com.readmark.ui.viewmodel.MainViewModel
import com.readmark.utils.UndoState
import com.readmark.utils.keyboardShortcuts
import com.readmark.utils.pushToUndoStack

/**
 * MainScreen - ReadMark 앱의 메인 화면
 *
 * 화면 구성:
 * 1. 상단 앱바 (제목, 설정 버튼)
 * 2. 통계 카드 (총 세션, 페이지 수, 요약 수)
 * 3. 연결 상태 표시
 * 4. 텍스트 입력 필드
 * 5. 작업 모드 선택 (요약/이어서 읽기/자동)
 * 6. 처리 결과 표시 (로딩/성공/에러)
 * 7. 액션 버튼 (처리, 노트 저장)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    // ViewModel 상태 구독
    val appConfig by viewModel.appConfig.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val processingResult by viewModel.processingResult.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 로컬 UI 상태
    var inputText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(WorkMode.AUTO_DETECT) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReadMark") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 통계 카드
            StatisticsCard(statistics = statistics)

            // 2. 연결 상태 카드
            ConnectionCard(
                connectionState = connectionState,
                endpoint = appConfig.lmStudio.endpoint,
                onTestConnection = {
                    viewModel.testConnection()
                },
                onDisconnect = {
                    viewModel.disconnect()
                }
            )

            // 3. 입력 섹션
            InputSection(
                inputText = inputText,
                onInputChange = { inputText = it },
                selectedMode = selectedMode,
                onModeChange = { selectedMode = it },
                enabled = !isLoading
            )

            // 4. 액션 버튼
            ActionButtons(
                enabled = inputText.isNotBlank() && !isLoading,
                isLoading = isLoading,
                onProcess = {
                    viewModel.processText(inputText, selectedMode)
                },
                onClear = {
                    inputText = ""
                    viewModel.clearResults()
                }
            )

            // 5. 결과 표시
            ResultSection(
                processingResult = processingResult,
                onSaveNote = { content ->
                    viewModel.saveNote("ReadMark Note", content)
                }
            )
        }
    }

    // 설정 다이얼로그
    if (showSettingsDialog) {
        SettingsDialog(
            config = appConfig,
            onDismiss = { showSettingsDialog = false },
            onSave = { updates ->
                viewModel.updateConfig(updates)
                showSettingsDialog = false
            }
        )
    }
}

/**
 * 통계 카드 - 사용 통계 표시
 */
@Composable
fun StatisticsCard(statistics: com.readmark.data.model.AppStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 사용 통계",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("세션", statistics.totalSessions.toString())
                StatItem("페이지", statistics.pagesProcessed.toString())
                StatItem("요약", statistics.summariesCreated.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 연결 상태 카드
 */
@Composable
fun ConnectionCard(
    connectionState: ConnectionState,
    endpoint: String,
    onTestConnection: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = when (connectionState) {
            is ConnectionState.Connected -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            is ConnectionState.Error -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
            else -> CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔌 LM Studio 연결",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = endpoint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                when (connectionState) {
                    is ConnectionState.Disconnected -> {
                        Button(onClick = onTestConnection) {
                            Text("연결 테스트")
                        }
                    }
                    is ConnectionState.Connecting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    is ConnectionState.Connected -> {
                        TextButton(onClick = onDisconnect) {
                            Text("연결 해제")
                        }
                    }
                    is ConnectionState.Error -> {
                        Button(onClick = onTestConnection) {
                            Text("재연결")
                        }
                    }
                }
            }

            when (connectionState) {
                is ConnectionState.Connected -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ ${connectionState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (connectionState.models.isNotEmpty()) {
                        Text(
                            text = "사용 가능한 모델: ${connectionState.models.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                is ConnectionState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "❌ ${connectionState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    connectionState.suggestion?.let { suggestion ->
                        Text(
                            text = "💡 $suggestion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

/**
 * 입력 섹션 - 텍스트 입력 및 모드 선택
 * Enhanced with keyboard shortcuts support (Ctrl+C/V/X/A/Z)
 */
@Composable
fun InputSection(
    inputText: String,
    onInputChange: (String) -> Unit,
    selectedMode: WorkMode,
    onModeChange: (WorkMode) -> Unit,
    enabled: Boolean
) {
    // Clipboard manager for copy/paste operations
    val clipboardManager = LocalClipboardManager.current

    // TextFieldValue state for advanced features (selection, composition)
    val textFieldValueState = remember(inputText) {
        mutableStateOf(TextFieldValue(text = inputText))
    }

    // Undo stack and index for Ctrl+Z functionality
    val undoStack = remember { mutableStateListOf<UndoState>() }
    var undoIndex by remember { mutableStateOf(0) }

    // Track last undo push time for debouncing
    var lastUndoPushTime by remember { mutableStateOf(0L) }

    // Sync TextFieldValue.text changes to parent
    LaunchedEffect(textFieldValueState.value.text) {
        if (textFieldValueState.value.text != inputText) {
            onInputChange(textFieldValueState.value.text)
        }
    }

    // Handle external changes (e.g., clear button pressed in parent)
    LaunchedEffect(inputText) {
        if (inputText != textFieldValueState.value.text) {
            textFieldValueState.value = TextFieldValue(
                text = inputText,
                selection = TextRange(inputText.length) // Cursor at end
            )
            // Clear undo stack on external change
            undoStack.clear()
            undoIndex = 0
            lastUndoPushTime = 0L
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📝 텍스트 입력",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = textFieldValueState.value,
                onValueChange = { newValue ->
                    val currentTime = System.currentTimeMillis()

                    // Push to undo stack if enough time elapsed (debouncing)
                    if (currentTime - lastUndoPushTime > 500L) {
                        pushToUndoStack(textFieldValueState.value, undoStack) { undoIndex = it }
                        lastUndoPushTime = currentTime
                    }

                    textFieldValueState.value = newValue
                    onInputChange(newValue.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .keyboardShortcuts(
                        textFieldValueState = textFieldValueState,
                        clipboardManager = clipboardManager,
                        undoStack = undoStack,
                        undoIndex = undoIndex,
                        onUndoIndexChange = { undoIndex = it }
                    ),
                placeholder = {
                    Text("텍스트를 입력하거나 붙여넣으세요...")
                },
                enabled = enabled,
                maxLines = 10,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions.Default
            )

            Text(
                text = "🎯 작업 모드",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    label = "자동 감지",
                    icon = "🤖",
                    selected = selectedMode == WorkMode.AUTO_DETECT,
                    onClick = { onModeChange(WorkMode.AUTO_DETECT) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
                ModeChip(
                    label = "요약",
                    icon = "📄",
                    selected = selectedMode == WorkMode.SUMMARY,
                    onClick = { onModeChange(WorkMode.SUMMARY) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
                ModeChip(
                    label = "이어읽기",
                    icon = "📖",
                    selected = selectedMode == WorkMode.CONTINUE_READING,
                    onClick = { onModeChange(WorkMode.CONTINUE_READING) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeChip(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(icon)
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        },
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * 액션 버튼 - 처리 및 초기화
 */
@Composable
fun ActionButtons(
    enabled: Boolean,
    isLoading: Boolean,
    onProcess: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onProcess,
            enabled = enabled && !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isLoading) "처리 중..." else "✨ 처리 시작")
        }

        OutlinedButton(
            onClick = onClear,
            enabled = enabled,
            modifier = Modifier.weight(0.5f)
        ) {
            Text("초기화")
        }
    }
}

/**
 * 결과 표시 섹션
 */
@Composable
fun ResultSection(
    processingResult: ProcessingResult,
    onSaveNote: (String) -> Unit
) {
    when (processingResult) {
        is ProcessingResult.Idle -> {
            // 결과 없음 - 아무것도 표시 안함
        }
        is ProcessingResult.Loading -> {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = processingResult.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        is ProcessingResult.Success -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅ 처리 완료",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = when (processingResult.mode) {
                                com.readmark.data.model.ProcessingMode.SUMMARY -> "요약"
                                com.readmark.data.model.ProcessingMode.CONTINUE_READING -> "이어읽기"
                                com.readmark.data.model.ProcessingMode.AUTO_DETECT -> "자동"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Divider()

                    Text(
                        text = processingResult.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // 메타데이터
                    processingResult.metadata.let { metadata ->
                        if (metadata.tokensUsed > 0) {
                            Text(
                                text = "🎯 토큰: ${metadata.tokensUsed} | 모델: ${metadata.modelUsed ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = { onSaveNote(processingResult.content) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("노트로 저장")
                    }
                }
            }
        }
        is ProcessingResult.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "❌ 오류 발생",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = processingResult.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    processingResult.suggestion?.let { suggestion ->
                        Text(
                            text = "💡 $suggestion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * 설정 다이얼로그
 */
@Composable
fun SettingsDialog(
    config: com.readmark.data.model.AppConfig,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var endpoint by remember { mutableStateOf(config.lmStudio.endpoint) }
    var apiKey by remember { mutableStateOf(config.lmStudio.apiKey) }
    var temperature by remember { mutableStateOf(config.lmStudio.temperature) }
    var maxTokens by remember { mutableStateOf(config.lmStudio.maxTokens) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚙️ 설정") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("LM Studio Endpoint") },
                    placeholder = { Text("http://10.0.2.2:1234") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions.Default
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions.Default
                )
                Text(
                    text = "Temperature: ${temperature}",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0f..1f
                )
                OutlinedTextField(
                    value = maxTokens.toString(),
                    onValueChange = { maxTokens = it.toIntOrNull() ?: maxTokens },
                    label = { Text("Max Tokens") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions.Default
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(mapOf(
                        "endpoint" to endpoint,
                        "apiKey" to apiKey,
                        "temperature" to temperature,
                        "maxTokens" to maxTokens
                    ))
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
