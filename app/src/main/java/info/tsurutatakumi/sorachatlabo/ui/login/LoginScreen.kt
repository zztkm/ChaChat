package info.tsurutatakumi.sorachatlabo.ui.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import info.tsurutatakumi.sorachatlabo.data.UserCredentialsManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    userCredentialsManager: UserCredentialsManager,  // UserCredentialsManagerを外部から注入
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // 保存されている認証情報を取得
    val savedSignalingUrl by userCredentialsManager.signalingUrl.collectAsState(initial = "")
    val savedToken by userCredentialsManager.token.collectAsState(initial = "")
    val savedClientId by userCredentialsManager.clientId.collectAsState(initial = "")
    val savedChannelId by userCredentialsManager.channelId.collectAsState(initial = "")

    // 入力値の状態
    var signalingUrl by remember { mutableStateOf(savedSignalingUrl) }
    var token by remember { mutableStateOf(savedToken) }
    var clientId by remember { mutableStateOf(savedClientId) }
    var channelId by remember { mutableStateOf(savedChannelId) }
    var isSignalingUrlError by remember { mutableStateOf(false) }
    var isChannelIdError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Signaling URL入力フィールド（必須）
        OutlinedTextField(
            value = signalingUrl,
            onValueChange = {
                signalingUrl = it
                isSignalingUrlError = it.isEmpty()
            },
            label = { Text("Signaling URL (必須)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = isSignalingUrlError,
            supportingText = {
                if (isSignalingUrlError) {
                    Text("Signaling URLは必須項目です")
                }
            }
        )

        // Channel ID入力フィールド（必須）
        OutlinedTextField(
            value = channelId,
            onValueChange = {
                channelId = it
                isChannelIdError = it.isEmpty()
            },
            label = { Text("Channel ID (必須)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            isError = isChannelIdError,
            supportingText = {
                if (isChannelIdError) {
                    Text("Channel IDは必須項目です")
                }
            }
        )

        // Token入力フィールド（オプション、パスワードとして表示）
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Token (オプション)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Client ID入力フィールド（オプション）
        OutlinedTextField(
            value = clientId,
            onValueChange = { clientId = it },
            label = { Text("Client ID (オプション)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // ログインボタン
        Button(
            onClick = {
                val isSignalingUrlValid = signalingUrl.isNotEmpty()
                val isChannelIdValid = channelId.isNotEmpty()

                isSignalingUrlError = !isSignalingUrlValid
                isChannelIdError = !isChannelIdValid

                if (isSignalingUrlValid && isChannelIdValid) {
                    coroutineScope.launch {
                        Log.i("LoginScreen", "Saving credentials: signalingUrl=$signalingUrl, channelId=$channelId, token=$token, clientId=$clientId")
                        userCredentialsManager.saveCredentials(
                            signalingUrl = signalingUrl,
                            token = token,
                            clientId = clientId,
                            channelId = channelId
                        )
                        onLoginSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ログイン")
        }
    }
}

// プレビュー用のダミーのUserCredentialsManager
@Composable
fun LoginScreenPreview() {
    // プレビューではダミーのUserCredentialsManagerを使用する
    val dummyUserCredentialsManager = UserCredentialsManager(androidx.compose.ui.platform.LocalContext.current)

    LoginScreen(
        userCredentialsManager = dummyUserCredentialsManager,
        onLoginSuccess = {}
    )
}

