package info.tsurutatakumi.chachat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 利用規約同意の管理クラス
 */
class TermsAgreementManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "terms_agreement_prefs"
        private const val KEY_TERMS_AGREED = "terms_agreed"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _termsAgreedFlow = MutableStateFlow(hasAgreedToTerms())

    /**
     * 利用規約に同意したかどうかの状態を返す
     */
    val termsAgreedFlow: StateFlow<Boolean> = _termsAgreedFlow

    /**
     * 利用規約に同意したかどうかを返す
     */
    fun hasAgreedToTerms(): Boolean {
        return prefs.getBoolean(KEY_TERMS_AGREED, false)
    }

    /**
     * 利用規約に同意する
     */
    fun agreeToTerms() {
        prefs.edit().putBoolean(KEY_TERMS_AGREED, true).apply()
        _termsAgreedFlow.value = true
    }

    /**
     * 同意をリセット（主にデバッグ用）
     */
    fun resetAgreement() {
        prefs.edit().putBoolean(KEY_TERMS_AGREED, false).apply()
        _termsAgreedFlow.value = false
    }
}
