package com.example.tumblrlite

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.memoryCacheMaxSizePercentWhileInBackground
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class TumblrLiteApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.20)
                .build()
        }
        .memoryCacheMaxSizePercentWhileInBackground(0.10)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                .maxSizePercent(0.02)
                .build()
        }
        .build()
}
