package kivaa.app.utils

import androidx.compose.runtime.mutableStateOf

data class DeepLinkAction(
    val platform: String? = null,
    val targetScreen: String? = null, // "watch" -> Video, "news" -> NewsDetail
    val itemId: String? = null
)

object NavigationManager {
    var pendingAction = mutableStateOf<DeepLinkAction?>(null)

    fun clear() {
        pendingAction.value = null
    }
}
