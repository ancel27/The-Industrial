# AudiVue Android Video SDK

AudiVue Android Video SDK is a Kotlin Android Library that provides a ready-made video player, Media3 playback, repository contracts, SDK initialization, and UI components for AudiVue-powered video apps.

The API layer is intentionally pluggable at this stage. Endpoint annotations and final response mapping should be added after the public API contract is frozen.

## Quick Integration

```kotlin
VideoSDK.initialize(
    context = applicationContext,
    baseUrl = "https://api.example.com"
)

videoView.loadVideo("videoId")
```

XML:

```xml
<com.audivue.sdk.ui.VideoView
    android:id="@+id/videoView"
    android:layout_width="match_parent"
    android:layout_height="220dp" />
```

## Build AAR

```bash
./gradlew :video-sdk:assembleRelease
```

AAR output:

```text
video-sdk/build/outputs/aar/video-sdk-release.aar
```

## API Plug-in Point

Implement `VideoRemoteDataSource` and pass it to the SDK:

```kotlin
VideoSDK.initialize(
    context = applicationContext,
    baseUrl = "https://api.example.com",
    remoteDataSource = YourRemoteDataSource()
)
```
