package info.tsurutatakumi.sorachatlabo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import info.tsurutatakumi.sorachatlabo.ui.theme.SoraChatLaboTheme
import info.tsurutatakumi.sorachatlabo.data.TermsAgreementManager
import info.tsurutatakumi.sorachatlabo.ui.terms.TermsAgreementDialog
import jp.shiguredo.sora.sdk.channel.SoraMediaChannel
import jp.shiguredo.sora.sdk.channel.option.SoraMediaOption
import jp.shiguredo.sora.sdk.error.SoraErrorReason
import jp.shiguredo.sora.sdk.channel.SoraMediaChannel.Listener
import jp.shiguredo.sora.sdk.channel.SoraCloseEvent
import jp.shiguredo.sora.sdk.channel.option.SoraChannelRole
import jp.shiguredo.sora.sdk.error.SoraMessagingError
import jp.shiguredo.sora.sdk.util.SoraLogger
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// チャットメッセージを表すデータクラス
data class ChatMessage(
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false
) {
    fun getFormattedTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}

class TextChatActivity : ComponentActivity() {
    companion object {
        const val TAG = "TextChatActivity"
        const val EXTRA_SIGNALING_URL = "signalingUrl"
        const val EXTRA_CHANNEL_ID = "channelId"
    }

    private var mediaChannel: SoraMediaChannel? = null
    private var connectionState by mutableStateOf(ConnectionState.DISCONNECTED)

    // チャットメッセージを保持するリスト
    private val chatMessages = mutableStateListOf<ChatMessage>()

    // 利用規約への同意状態を管理
    private lateinit var termsAgreementManager: TermsAgreementManager
    private var showTermsDialog by mutableStateOf(false)

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sora SDK のログを有効化
        // debug 時以外は不要なログのため、本番用アプリでは無効化推奨
        SoraLogger.enabled = true

        // 利用規約同意マネージャーの初期化
        termsAgreementManager = TermsAgreementManager(applicationContext)

        val signalingUrl = intent.getStringExtra(EXTRA_SIGNALING_URL) ?: ""
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: ""

        if (signalingUrl.isEmpty() || channelId.isEmpty()) {
            Log.e(TAG, "Invalid signaling URL or channel ID")
            finish()
            return
        }

        // 利用規約の同意状態を確認
        showTermsDialog = !termsAgreementManager.hasAgreedToTerms()

        enableEdgeToEdge()
        setContent {
            SoraChatLaboTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 利用規約ダイアログを表示
                    if (showTermsDialog) {
                        TermsAgreementDialog(
                            onAccept = {
                                termsAgreementManager.agreeToTerms()
                                showTermsDialog = false
                                // 同意したらSora接続を開始
                                connectToSora(signalingUrl, channelId)
                            },
                            onDecline = {
                                // 同意しなかった場合は何もせず、接続は開始しない
                                showTermsDialog = false
                            }
                        )
                    }

                    TextChatScreen(
                        signalingUrl = signalingUrl,
                        channelId = channelId,
                        connectionState = connectionState,
                        onConnect = {
                            // 利用規約に同意している場合のみ接続開始
                            if (termsAgreementManager.hasAgreedToTerms()) {
                                connectToSora(signalingUrl, channelId)
                            }
                        },
                        onDisconnect = { disconnectSora() },
                        messages = chatMessages,
                        onSendMessage = { message -> sendMessage(channelId, message) },
                        showTermsAgreement = { showTermsDialog = true },
                        termsAgreed = termsAgreementManager.hasAgreedToTerms(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        disconnectSora()
        super.onDestroy()
    }

    private fun connectToSora(signalingUrl: String, channelId: String) {
        if (mediaChannel != null) {
            Log.d(TAG, "Already connected or connecting to Sora")
            return
        }

        connectionState = ConnectionState.CONNECTING

        try {
            // チャンネルへの接続のためのリスナーを作成
            val listener = object : Listener {
                override fun onConnect(mediaChannel: SoraMediaChannel) {
                    Log.d(TAG, "Connected to Sora channel")
                    connectionState = ConnectionState.CONNECTED
                }

                override fun onClose(mediaChannel: SoraMediaChannel, closeEvent: SoraCloseEvent?) {
                    when {
                        closeEvent == null -> Log.d(TAG, "Disconnected from Sora channel")
                        closeEvent.code == 1000 -> Log.e(TAG, "Disconnected from Sora: ${closeEvent.reason} (${closeEvent.code})")
                        else -> Log.d(TAG, "Disconnected with error: ${closeEvent.reason} (${closeEvent.code})")
                    }
                    this@TextChatActivity.mediaChannel = null
                    connectionState = ConnectionState.DISCONNECTED
                }

                override fun onError(mediaChannel: SoraMediaChannel, reason: SoraErrorReason, message: String) {
                    Log.e(TAG, "Error occurred: $reason - $message")
                    this@TextChatActivity.mediaChannel = null
                    connectionState = ConnectionState.ERROR
                }

                // データチャンネルメッセージ受信時の処理
                override fun onDataChannelMessage(mediaChannel: SoraMediaChannel, label: String, data: ByteBuffer) {
                    val bytes = ByteArray(data.remaining())
                    data.get(bytes)
                    val message = String(bytes)
                    Log.d(TAG, "Received message: $message on channel: $label")

                    // 受信したメッセージをリストに追加（UIスレッドで実行）
                    runOnUiThread {
                        chatMessages.add(ChatMessage(text = message, isFromMe = false))
                    }
                }
            }

            // DataChannel を利用したリアルタイムメッセージングのみで接続するために音声と映像を有効化しない
            val mediaOption = SoraMediaOption()
            mediaOption.role = SoraChannelRole.SENDRECV

            // リアルタイムメッセージング用の DataChannel 設定
            // チャット用のラベルは暗黙的に #{$channelId} にする
            // つまり、同じ Sora チャンネルに接続しているクライアント同士でテキストチャットができる
            // NOTE: Sora に詳しくないユーザーに Label まで意識させるのもどうかと思ってこのような仕様にしている
            val dataChannels = listOf(
                mapOf(
                    "label" to "#${channelId}",
                    "direction" to "sendrecv",
                )
            )

            // Soraへの接続
            mediaChannel = SoraMediaChannel(
                context = applicationContext,
                signalingEndpoint = signalingUrl,
                channelId = channelId,
                mediaOption = mediaOption,
                listener = listener,
                dataChannelSignaling = true,
                dataChannels = dataChannels,
            )

            // 接続開始
            mediaChannel?.connect()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Sora: ${e.message}")
            connectionState = ConnectionState.ERROR
            mediaChannel = null
        }
    }

    private fun disconnectSora() {
        mediaChannel?.disconnect()
        mediaChannel = null
        connectionState = ConnectionState.DISCONNECTED
    }

    // メッセージ送信処理
    private fun sendMessage(channelId: String, message: String) {
        if (message.isBlank() || connectionState != ConnectionState.CONNECTED) {
            return
        }

        val label = "#$channelId"
        val error = mediaChannel?.sendDataChannelMessage(label, message)

        if (isSendDataChannelMessageSuccess(error)) {
            chatMessages.add(ChatMessage(text = message, isFromMe = true))
        }
    }

    /**
     * SoraMediaChannel.sendDataChannelMessage が成功したかどうかを判定する
     *
     * @param error SoraMediaChannel.sendDataChannelMessage の戻り値
     *
     * 引数は error という名前ではあるのだが、error (enum) が ok の場合は
     * SoraMediaChannel.sendDataChannelMessage が成功したことを意味する
     */
    private fun isSendDataChannelMessageSuccess(error: SoraMessagingError?): Boolean {
        return when (error) {
            null -> false // この場合は SoraMediaChannel が null であったときのため送信できていない
            SoraMessagingError.OK -> true
            else -> false
        }
    }
}

@Composable
fun TextChatScreen(
    signalingUrl: String,
    channelId: String,
    connectionState: TextChatActivity.ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    showTermsAgreement: () -> Unit,
    termsAgreed: Boolean,
    modifier: Modifier = Modifier
) {
    // 接続状態を監視し、接続が必要かつ利用規約に同意している場合は
    // LaunchedEffect で接続処理を呼び出す
    LaunchedEffect(signalingUrl, channelId, termsAgreed) {
        if (termsAgreed) {
            onConnect()
        }
    }

    // 画面が破棄されるときに接続を解除するための DisposableEffect
    DisposableEffect(Unit) {
        onDispose {
            onDisconnect()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // ヘッダー部分
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "チャンネル: $channelId",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when {
                        !termsAgreed -> "利用規約への同意が必要です"
                        else -> "接続状態: ${connectionState.name}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                // 利用規約未同意の場合は同意ボタンを表示
                if (!termsAgreed) {
                    androidx.compose.material3.Button(
                        onClick = showTermsAgreement,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("利用規約に同意する")
                    }
                }
            }
        }

        // 利用規約に同意していない場合は接続前の説明を表示
        if (!termsAgreed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "チャットを利用するには利用規約に同意する必要があります。\n上の「利用規約に同意する」ボタンをタップしてください。",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // チャットメッセージ表示部分
            val listState = rememberLazyListState()

            // 新しいメッセージが来たら自動スクロール
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
            }
        }

        // メッセージ入力UI (利用規約に同意済みかつ接続済みの場合のみ有効)
        MessageInputBar(
            onSendMessage = onSendMessage,
            enabled = termsAgreed && connectionState == TextChatActivity.ConnectionState.CONNECTED,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun MessageInputBar(
    onSendMessage: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text("メッセージを入力") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            enabled = enabled,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                }
            )
        )

        IconButton(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSendMessage(messageText)
                    messageText = ""
                }
            },
            enabled = enabled && messageText.isNotBlank(),
            modifier = Modifier
                .background(
                    color = if (enabled && messageText.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "送信",
                tint = if (enabled && messageText.isNotBlank())
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isFromMe)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = message.getFormattedTime(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(if (message.isFromMe) Alignment.End else Alignment.Start),
                textAlign = if (message.isFromMe) TextAlign.End else TextAlign.Start
            )
        }
    }
}
