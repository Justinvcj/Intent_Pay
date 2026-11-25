# ---- Gson (reflection on model classes, generic type tokens) ----
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# ---- Google API Client / Sheets v4 / Google Auth (reflection-heavy JSON factories) ----
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-keep class com.google.api.services.sheets.** { *; }
-dontwarn com.google.api.services.sheets.**
-keep class com.google.auth.** { *; }
-dontwarn com.google.auth.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- IntentPay Room entities ----
-keep class com.intentpay.data.local.*Entity { *; }

# ---- kotlinx.coroutines metadata / service loader ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-dontwarn kotlinx.coroutines.debug.**
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# ---- JDK-only APIs referenced by Apache HttpClient (pulled in via Google API Client); absent on Android ----
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
