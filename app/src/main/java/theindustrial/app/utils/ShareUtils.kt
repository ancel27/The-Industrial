package theindustrial.app.utils

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareLink(context: Context, title: String?, url: String?) {
        if (url.isNullOrBlank()) return
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title ?: "Check this out")
            putExtra(Intent.EXTRA_TEXT, "${title ?: "Read this industrial news"}\n\n$url")
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share News via"))
    }
}
