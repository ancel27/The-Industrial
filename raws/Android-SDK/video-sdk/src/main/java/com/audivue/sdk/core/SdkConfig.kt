package com.audivue.sdk.core

import android.content.Context

data class SdkConfig(
    val context: Context,
    val baseUrl: String,
    val sdkKey: String? = null,
    val sdkVersion: String = "android-1.0.0"
)
