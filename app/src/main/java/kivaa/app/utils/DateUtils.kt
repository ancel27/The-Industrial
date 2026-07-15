package kivaa.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    private val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    fun formatDate(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return ""
        return try {
            val date = inputFormat.parse(rawDate)
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            // Handle fallback for other formats if necessary, or just return raw
            rawDate.split("T").firstOrNull() ?: rawDate
        }
    }
}
