package info.tsurutatakumi.chachat.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStoreの名前
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_credentials")

class UserCredentialsManager(private val context: Context) {
    // Preferenceのキー
    companion object {
        val TAG: String = this::class.java.simpleName
        val SIGNALING_URL = stringPreferencesKey("signaling_url")
        val TOKEN = stringPreferencesKey("token")
        val CLIENT_ID = stringPreferencesKey("client_id")
        val CHANNEL_ID = stringPreferencesKey("channel_id")
    }

    // Signaling URLのFlow
    val signalingUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SIGNALING_URL] ?: ""
        }

    // Tokenの Flow
    val token: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN] ?: ""
        }

    // Client IDの Flow
    val clientId: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CLIENT_ID] ?: ""
        }

    // Channel IDの Flow
    val channelId: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CHANNEL_ID] ?: ""
        }

    // Signaling URLの保存
    suspend fun saveSignalingUrl(signalingUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[SIGNALING_URL] = signalingUrl
        }
    }

    // Tokenの保存
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
    }

    // Client IDの保存
    suspend fun saveClientId(clientId: String) {
        context.dataStore.edit { preferences ->
            preferences[CLIENT_ID] = clientId
        }
    }

    // Channel IDの保存
    suspend fun saveChannelId(channelId: String) {
        context.dataStore.edit { preferences ->
            preferences[CHANNEL_ID] = channelId
        }
    }

    // すべての認証情報を一度に保存
    suspend fun saveCredentials(signalingUrl: String, token: String, clientId: String, channelId: String) {
        Log.i(TAG, "Saving credentials: signalingUrl=$signalingUrl, token=$token, clientId=$clientId, channelId=$channelId")
        context.dataStore.edit { preferences ->
            preferences[SIGNALING_URL] = signalingUrl
            preferences[TOKEN] = token
            preferences[CLIENT_ID] = clientId
            preferences[CHANNEL_ID] = channelId
        }
    }

    // 認証情報をクリア
    suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
