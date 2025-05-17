package info.tsurutatakumi.sorachatlabo.ui.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 利用規約の同意ダイアログ
 */
@Composable
fun TermsAgreementDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* 何もしない - ダイアログを閉じない */ },
        title = {
            Text(
                text = "利用規約",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "SoraChatLaboの利用規約",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = """
                        この利用規約（以下「本規約」）は、SoraChatLaboアプリケーション（以下「本アプリ」）の利用条件を定めるものです。
                        
                        1. 本アプリの利用について
                        本アプリを利用するには、本規約に同意する必要があります。本アプリを利用することにより、ユーザーは本規約に同意したものとみなされます。
                        
                        2. プライバシーと個人情報
                        本アプリはSoraサーバーを通じてチャットメッセージの送受信を行います。送信されたメッセージは他のユーザーに共有される可能性があります。
                        
                        3. 免責事項
                        本アプリの利用は自己責任で行ってください。開発者は本アプリの利用によって生じたいかなる損害についても責任を負いません。
                        
                        4. 利用制限
                        本アプリを不正に利用した場合、予告なくサービスの利用を制限することがあります。
                        
                        5. 規約の変更
                        本規約は予告なく変更される場合があります。変更後も本アプリを継続して利用する場合、変更後の規約に同意したものとみなされます。
                        
                        以上の規約に同意いただける場合は「同意する」ボタンを、同意いただけない場合は「同意しない」ボタンを押してください。
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("同意しない")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("同意する")
                }
            }
        },
        dismissButton = null
    )
}
