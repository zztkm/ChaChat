package info.tsurutatakumi.sorachatlabo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import info.tsurutatakumi.sorachatlabo.data.TermsAgreementManager
import info.tsurutatakumi.sorachatlabo.data.UserCredentialsManager
import info.tsurutatakumi.sorachatlabo.ui.login.LoginScreen
import info.tsurutatakumi.sorachatlabo.ui.terms.TermsAgreementDialog
import info.tsurutatakumi.sorachatlabo.ui.theme.SoraChatLaboTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }
    // UserCredentialsManagerをアクティビティのプロパティとして管理
    private lateinit var userCredentialsManager: UserCredentialsManager

    // 利用規約への同意状態を管理
    private lateinit var termsAgreementManager: TermsAgreementManager
    private var showTermsDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UserCredentialsManagerのインスタンスを作成
        userCredentialsManager = UserCredentialsManager(applicationContext)

        // 利用規約同意マネージャーの初期化
        termsAgreementManager = TermsAgreementManager(applicationContext)

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
                            },
                            onDecline = {
                                // 同意しなかった場合はアプリを終了
                                finish()
                            }
                        )
                    }

                    AppContent(
                        userCredentialsManager = userCredentialsManager,
                        termsAgreedFlow = termsAgreementManager.termsAgreedFlow,
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
    termsAgreedFlow: kotlinx.coroutines.flow.StateFlow<Boolean>,
    modifier: Modifier = Modifier
) {
    // アプリの状態管理
    var isLoggedIn by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 利用規約への同意状態を取得
    val termsAgreed by termsAgreedFlow.collectAsState()

    // 利用規約に同意していない場合は何も表示しない
    if (!termsAgreed) {
        return
    }

    // ログイン状態によって画面を切り替え
    if (isLoggedIn) {
        // ログイン成功時にTextChatActivityを起動する処理
        val signalingUrl by userCredentialsManager.signalingUrl.collectAsState(initial = "")
        val channelId by userCredentialsManager.channelId.collectAsState(initial = "")

        // URLとチャンネルIDが有効であれば、TextChatActivityを起動
        LaunchedEffect(signalingUrl, channelId) {
            if (signalingUrl.isNotEmpty() && channelId.isNotEmpty()) {
                val intent = Intent(context, TextChatActivity::class.java).apply {
                    putExtra(TextChatActivity.EXTRA_SIGNALING_URL, signalingUrl)
                    putExtra(TextChatActivity.EXTRA_CHANNEL_ID, channelId)
                }
                context.startActivity(intent)
                // MainActivity自体は終了する（バックボタンで戻らないように）
                if (context is Activity) {
                    context.finish()
                }
            }
        }
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

