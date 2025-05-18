package info.tsurutatakumi.chachat.ui.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
                    text = "ちゃちゃっとの利用規約",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = """
                        この利用規約（以下「本規約」）はちゃちゃっと（以下「本アプリ」）の利用条件を定めるものです。
                        
                        1. 本アプリの利用について
                        
                        本アプリを利用するには、本規約に同意する必要があります。また、本アプリの利用には、お客様ご自身でご用意いただいたセルフホストの WebRTC SFU Sora 環境、Sora Labo 環境、または Sora Cloud 環境（以下総称して「Soraサーバー」）への接続が必要です。本アプリを利用することにより、ユーザーは本規約に同意し、必要なSoraサーバー環境を準備しているものとみなされます。
                        
                        2. プライバシーとデータの取り扱い
                        
                        本アプリは、お客様が接続するSoraサーバーを通じてチャットメッセージ等のデータ（以下「送信データ」）の送受信を行います。
                        
                        データの共有範囲: 送信データは、Soraサーバーを介して、その時点で同じチャネルに接続している他のユーザーのクライアント端末に送信され、共有されます。
                        データの保存: 送信データは、外部のサーバーには一切保存されません。
                        データの一時記録と消去: 送信データは、同じチャネルに接続している他のユーザーのクライアント端末のメモリ上に一時的に記録されます。本アプリを終了すると、これらの記録は消去されます。
                        3. 免責事項
                        
                        本アプリの利用は自己責任で行ってください。Soraサーバーの構築・運用、および本アプリの利用によって生じたいかなる損害についても、開発者は責任を負いません。
                        
                        4. 利用について
                        
                        ユーザーは、本アプリおよび接続するSoraサーバーの利用規約や関連法規を遵守するものとします。ユーザーによる不正な利用や規約違反が確認された場合、接続先のSoraサーバーの提供者により、当該Soraサーバーの利用が制限される可能性があります。本アプリ開発者は、ユーザーの利用状況を監視したり、直接的に利用を制限したりするものではありません。
                        
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
