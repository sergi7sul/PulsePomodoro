# CherryTime ProGuard rules

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Room — keep entity + DAO classes for code generation
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase$Callback { *; }

# Room schema export
-keepattributes SourceFile,LineNumberTable

# MediaPipe Tasks
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# DataStore
-keep class androidx.datastore.** { *; }

# EncryptedSharedPreferences (Tink internals)
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
