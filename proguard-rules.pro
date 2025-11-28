# Add project specific ProGuard rules here.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep MDM app classes
-keep class com.mdm.app.** { *; }

# Keep model classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
