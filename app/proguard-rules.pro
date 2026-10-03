# R8 / ProGuard rules.
#
# R8 was previously disabled entirely (isMinifyEnabled = false). With it on,
# these keep rules are mandatory: this app uses Moshi reflection over the
# StoreItem/CategoryItem data classes and Firestore reflection over the same
# classes. Strip them and documents deserialise to empty objects at runtime —
# a silent data failure, not a crash, so it will not show up in a smoke test.

# ---- Moshi (reflection-based adapters for model classes) ----
# Generated adapters live alongside the annotated classes.
-keep class kotlin.Metadata { *; }
-keepclassmembers class com.example.model.** { *; }
-keep @com.squareup.moshi.JsonQualifier @interface * { *; }
-keep @com.squareup.moshi.FromJson @interface * { *; }
-keep @com.squareup.moshi.ToJson @interface * { *; }

# Moshi's reflective adapter looks up constructors and fields by name.
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
}
-if @com.squareup.moshi.FromJson class *
-keep,allowobfuscation,allowshrinking class <1>

# KSP-generated Moshi adapters are referenced reflectively.
-keep class **JsonAdapter { *; }
-keepnames @com.squareup.moshi.FromJson class *
-keepnames @com.squareup.moshi.ToJson class *
-dontwarn org.jetbrains.annotations.**

# ---- Firestore: toObject() reflects over these data classes ----
# StoreItem and CategoryItem are deserialised by field name, so their field
# names must survive obfuscation.
-keepclassmembers class com.example.model.StoreItem { *; }
-keepclassmembers class com.example.model.CategoryItem { *; }
-keepclassmembers class com.example.model.UserAccount { *; }
-keep class com.example.model.** { *; }

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# ---- Kotlin coroutines ----
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ---- Retrofit / OkHttp (declared in dependencies) ----
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ---- Compose ----
-dontwarn androidx.compose.**