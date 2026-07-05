# Add project specific ProGuard rules here.

# Hilt rules
-keep class com.google.dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep class * extends androidx.activity.ComponentActivity
-keep class * extends androidx.fragment.app.Fragment

# Retrofit / OkHttp
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.Platform$Java8

# Firebase
-keep class com.google.firebase.** { *; }

# MediaPipe / TFLite
-keep class com.google.mediapipe.** { *; }
-keep class org.tensorflow.lite.** { *; }
-dontwarn com.google.mediapipe.proto.**
-dontwarn com.google.mediapipe.framework.**

# Shaded AutoValue / Javapoet (often cause R8 warnings)
-dontwarn autovalue.shaded.**
-dontwarn javax.lang.model.**

# Glide
-keep public class * extends com.github.bumptech.glide.module.AppGlideModule
-keep class com.github.bumptech.glide.GeneratedAppGlideModuleImpl { *; }
-keep public enum com.github.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Material components
-keep class com.google.android.material.** { *; }

# Navigation components
-keep class androidx.navigation.** { *; }

# Lottie (Corrected Package Name)
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# General
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
