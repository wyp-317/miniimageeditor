# Keep GLSurfaceView and renderer to avoid stripping reflective calls
-keep class android.opengl.** { *; }
-keep class javax.microedition.khronos.** { *; }

# Coil keep rules (safe minimal)
-keep class coil.** { *; }
-dontwarn coil.**

# Room schema classes
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep Kotlin coroutines debug names
-keepclassmembers class kotlinx.coroutines.** { *; }

# Avoid obfuscating parcelable models
-keep class com.example.miniimageeditor.** implements android.os.Parcelable { *; }
