package com.siddharth.datamonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.siddharth.datamonitor.utils.askGemini
import com.siddharth.datamonitor.utils.formatBytes

data class ChatMessage(val text: String, val isUser: Boolean, val isLoading: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotBottomSheet(
    viewModel: DataUsageViewModel,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    val todayMobile by viewModel.todayMobile.collectAsState()
    val todayWifi by viewModel.todayWifi.collectAsState()
    val topApps by viewModel.topApps.collectAsState()

    var messages by remember { mutableStateOf(listOf(
        ChatMessage("Hello! Ask me about your data usage.", isUser = false)
    )) }
    
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF161616), // Solid dark color instead of transparent surface
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Data Assistant",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    ChatBubble(msg)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ask something...") },
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val userText = inputText
                                inputText = ""
                                keyboardController?.hide()
                                
                                messages = messages + ChatMessage(userText, isUser = true)
                                messages = messages + ChatMessage("", isUser = false, isLoading = true)
                                
                                val contextText = """
                                    User's Data Usage Context:
                                    Today's Mobile Data: ${formatBytes(todayMobile)}
                                    Today's Wi-Fi Data: ${formatBytes(todayWifi)}
                                    Top Apps Usage: ${topApps.take(5).joinToString(", ") { "${it.appName} (${formatBytes(it.bytes)})" }}
                                """.trimIndent()

                                scope.launch {
                                    val response = askGemini(
                                        prompt = userText,
                                        systemPrompt = "You are a helpful data usage assistant. Help the user understand their network data usage based on the following context. Keep answers concise, and properly formatted.\n$contextText"
                                    )
                                    messages = messages.dropLast(1) + ChatMessage(response, isUser = false)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                                val userText = inputText
                                inputText = ""
                                keyboardController?.hide()
                                
                                messages = messages + ChatMessage(userText, isUser = true)
                                messages = messages + ChatMessage("", isUser = false, isLoading = true)
                                
                                val contextText = """
                                    User's Data Usage Context:
                                    Today's Mobile Data: ${formatBytes(todayMobile)}
                                    Today's Wi-Fi Data: ${formatBytes(todayWifi)}
                                    Top Apps Usage: ${topApps.take(5).joinToString(", ") { "${it.appName} (${formatBytes(it.bytes)})" }}
                                """.trimIndent()

                                scope.launch {
                                    val response = askGemini(
                                        prompt = userText,
                                        systemPrompt = "You are a helpful data usage assistant. Help the user understand their network data usage based on the following context. Keep answers concise, and properly formatted.\n$contextText"
                                    )
                                    messages = messages.dropLast(1) + ChatMessage(response, isUser = false)
                                }
                            }
                    }
                )
            )
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (message.isUser) MaterialTheme.colorScheme.primary else Color(0xFF2A2A2A),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
                .fillMaxWidth(if (message.isLoading) 0.2f else 0.85f)
        ) {
            if (message.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    text = message.text,
                    color = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
