package info.tsurutatakumi.sorachatlabo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.tsurutatakumi.sorachatlabo.data.UserCredentialsManager
import info.tsurutatakumi.sorachatlabo.ui.login.LoginScreen
import info.tsurutatakumi.sorachatlabo.ui.theme.SoraChatLaboTheme
import jp.shiguredo.sora.sdk.channel.SoraCloseEvent
import jp.shiguredo.sora.sdk.channel.SoraMediaChannel

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    // UserCredentialsManagerをアクティビティのプロパティとして管理
    private lateinit var userCredentialsManager: UserCredentialsManager
    // SoraMediaChannelをアクティビティのプロパティとして管理
    private lateinit var mediaChannel: SoraMediaChannel

    private val channelListener = object : SoraMediaChannel.Listener {
        override fun onConnect(mediaChannel: SoraMediaChannel) {
            Log.i(TAG, "Connected to Sora")
        }

        override fun onClose(mediaChannel: SoraMediaChannel, closeEvent: SoraCloseEvent?) {
            when {
                closeEvent == null -> Log.i(TAG, "Disconnected from Sora")
                closeEvent.code == 1000 -> Log.i(TAG, "Sora から切断されました: ${closeEvent.code}: ${closeEvent.reason}")
                else -> Log.e(TAG, "Error: ${closeEvent.code}: ${closeEvent.reason}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UserCredentialsManagerのインスタンスを作成
        userCredentialsManager = UserCredentialsManager(applicationContext)

        enableEdgeToEdge()
        setContent {
            SoraChatLaboTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        userCredentialsManager = userCredentialsManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    userCredentialsManager: UserCredentialsManager,
    modifier: Modifier = Modifier
) {
    // アプリの状態管理
    var isLoggedIn by remember { mutableStateOf(false) }

    // ログイン状態によって画面を切り替え
    if (isLoggedIn) {
        MainScreen(
            userCredentialsManager = userCredentialsManager,
            modifier = modifier
        )
    } else {
        LoginScreen(
            userCredentialsManager = userCredentialsManager,
            onLoginSuccess = { isLoggedIn = true },
            modifier = modifier
        )
    }
}

@Composable
fun MainScreen(
    userCredentialsManager: UserCredentialsManager,
    modifier: Modifier = Modifier
) {
    // DataStoreから認証情報を取得して表示
    val signalingUrl by userCredentialsManager.signalingUrl.collectAsState(initial = "")
    val channelId by userCredentialsManager.channelId.collectAsState(initial = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ログインに成功しました！",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Signaling URL: $signalingUrl",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Channel ID: $channelId",
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val dummyUserCredentialsManager = UserCredentialsManager(androidx.compose.ui.platform.LocalContext.current)
    SoraChatLaboTheme {
        LoginScreen(
            onLoginSuccess = {},
            userCredentialsManager = dummyUserCredentialsManager,
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val dummyUserCredentialsManager = UserCredentialsManager(androidx.compose.ui.platform.LocalContext.current)
    SoraChatLaboTheme {
        MainScreen(
            userCredentialsManager = dummyUserCredentialsManager
        )
    }
}

