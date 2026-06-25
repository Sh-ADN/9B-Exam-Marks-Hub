# Keep app's own classes (needed for entry points, Compose previews, etc.)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends androidx.activity.ComponentActivity
-keep class com.abutorab.teacher.hub.** { *; }
-keepclassmembers class com.abutorab.teacher.hub.** { *; }

# Compose - only keep what's needed for runtime reflection, let R8 shrink the rest
-dontwarn androidx.compose.**

# Retrofit - needs generic signatures for interface proxies
-keepattributes Signature, *Annotation*, RuntimeVisibleParameterAnnotations, RuntimeVisibleAnnotations
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn retrofit2.**

# OkHttp / Okio - platform classes only
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Moshi - needs JSON adapter classes kept for reflection-based parsing
-keep,allowobfuscation @interface com.squareup.moshi.JsonClass
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class kotlin.Metadata { *; }

# Room - entities and DAOs need their structure kept
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Kotlin coroutines internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Keep line numbers for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
