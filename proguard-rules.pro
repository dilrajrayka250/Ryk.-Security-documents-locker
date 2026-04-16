# ProGuard rules for RYK Secret Locker

# AdMob
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Billing
-keep class com.android.billingclient.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# App classes
-keep class com.securedocs.app.** { *; }

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
