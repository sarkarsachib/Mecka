package ai.mecka.runtime

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class StateManager(private val context: Context) {

    private val stateStore = ConcurrentHashMap<String, Any>()
    private val listeners = ConcurrentHashMap<String, MutableList<StateChangeListener>>()
    private val stateScope = CoroutineScope(Dispatchers.Default + Job())
    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("mecka_state", Context.MODE_PRIVATE)
    }

    fun setState(key: String, value: Any) {
        stateStore[key] = value
        stateScope.launch {
            saveStateToPreferences(key, value)
            notifyListeners(key, value)
        }
    }

    fun getState(key: String): Any? {
        return stateStore[key]
    }

    fun removeState(key: String) {
        stateStore.remove(key)
        stateScope.launch {
            sharedPrefs.edit().remove(key).apply()
            notifyListeners(key, null)
        }
    }

    fun registerListener(key: String, listener: StateChangeListener) {
        listeners.getOrPut(key) { mutableListOf() }.add(listener)
    }

    fun unregisterListener(key: String, listener: StateChangeListener) {
        listeners[key]?.remove(listener)
    }

    private suspend fun saveStateToPreferences(key: String, value: Any) {
        withContext(Dispatchers.IO) {
            try {
                when (value) {
                    is String -> sharedPrefs.edit().putString(key, value).apply()
                    is Int -> sharedPrefs.edit().putInt(key, value).apply()
                    is Long -> sharedPrefs.edit().putLong(key, value).apply()
                    is Float -> sharedPrefs.edit().putFloat(key, value).apply()
                    is Boolean -> sharedPrefs.edit().putBoolean(key, value).apply()
                    else -> {
                        // For complex objects, we could use JSON serialization
                        Timber.w("State value type not supported for persistence: ${value.javaClass.simpleName}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save state to preferences")
            }
        }
    }

    private fun notifyListeners(key: String, newValue: Any?) {
        stateScope.launch {
            listeners[key]?.forEach { listener ->
                try {
                    listener.onStateChanged(key, newValue)
                } catch (e: Exception) {
                    Timber.e(e, "State listener failed for key: $key")
                }
            }
        }
    }

    fun clearAllState() {
        stateStore.clear()
        stateScope.launch {
            sharedPrefs.edit().clear().apply()
        }
    }

    companion object {
        private var instance: StateManager? = null

        fun getInstance(context: Context): StateManager {
            return instance ?: synchronized(this) {
                instance ?: StateManager(context).also {
                    instance = it
                }
            }
        }
    }
}

interface StateChangeListener {
    fun onStateChanged(key: String, newValue: Any?)
}