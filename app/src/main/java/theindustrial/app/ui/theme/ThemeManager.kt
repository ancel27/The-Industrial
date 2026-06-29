package theindustrial.app.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import theindustrial.app.data.model.PlatformConfig

object ThemeManager {
    private val _currentConfig = mutableStateOf<PlatformConfig?>(null)
    val currentConfig: State<PlatformConfig?> = _currentConfig

    // Added Global User ID and Name State
    private val _userId = mutableStateOf<Int?>(null)
    val userId: State<Int?> = _userId

    private val _userName = mutableStateOf<String?>(null)
    val userName: State<String?> = _userName

    fun updateConfig(config: PlatformConfig) {
        _currentConfig.value = config
    }

    fun setUserId(id: Int?) {
        _userId.value = id
    }

    fun setUserName(name: String?) {
        _userName.value = name
    }

    fun getColor(hex: String?, default: Color): Color {
        if (hex.isNullOrBlank()) return default
        return try {
            val colorString = if (hex.startsWith("#")) hex else "#$hex"
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            default
        }
    }
}
