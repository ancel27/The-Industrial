package com.audivue.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

class LiveChatView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    init {
        orientation = VERTICAL
        setBackgroundColor(0xFF0C0C0C.toInt())
        addView(TextView(context).apply {
            text = "Live chat"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 18f
        })
    }

    fun appendMessage(name: String, message: String) {
        addView(TextView(context).apply {
            text = "$name: $message"
            setTextColor(0xFF9CA3AF.toInt())
            textSize = 14f
        })
    }
}
