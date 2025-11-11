# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in $ANDROID_HOME/tools/proguard/proguard-android.txt

# Keep data classes for Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.lacomprago.data.** { *; }
-keep class com.lacomprago.models.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep security crypto
-keep class androidx.security.crypto.** { *; }
