# AudiVue SDK Public API

## Initialize

```kotlin
VideoSDK.initialize(
    context = applicationContext,
    baseUrl = "https://api.example.com",
    sdkKey = "av_pub_xxxxx"
)
```

## Player View

```xml
<com.audivue.sdk.ui.VideoView
    android:id="@+id/videoView"
    android:layout_width="match_parent"
    android:layout_height="220dp" />
```

```kotlin
videoView.bindLifecycle(this)
videoView.loadVideo("videoId")
```

## Repository

```kotlin
val repository = VideoSDK.repository()
val details = repository.getVideoDetails("videoId")
val playback = repository.getPlaybackInfo("videoId")
repository.like("videoId")
repository.addComment("videoId", "Great video")
```

## API Layer Status

The SDK ships with the networking foundation and repository interfaces. Implement `VideoRemoteDataSource` when the AudiVue API documentation is finalized.
