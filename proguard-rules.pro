# ════════════════════════════════════════════════════════════
#  SecureDocsApp — Production ProGuard Rules
#  Covers: AdMob, ML Kit, Billing, Biometric, CameraX, Kotlin
# ════════════════════════════════════════════════════════════

# ── Google AdMob ─────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.**             { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.ads.**

# ── Google Play Billing ───────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ── ML Kit Barcode ────────────────────────────────────────────────────────────
-keep class com.google.mlkit.**           { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.mlkit.**

# ── CameraX ───────────────────────────────────────────────────────────────────
-keep class androidx.camera.**            { *; }
-dontwarn androidx.camera.**

# ── Biometric ─────────────────────────────────────────────────────────────────
-keep class androidx.biometric.**         { *; }

# ── AndroidX Security (EncryptedSharedPreferences) ───────────────────────────
-keep class androidx.security.crypto.**   { *; }

# ── App: keep ONLY what must survive obfuscation ──────────────────────────────
# Billing classes must not be obfuscated (product ID string matching)
-keep class com.securedocs.app.billing.** { *; }

# Ad classes must not be obfuscated (AdMob callback matching)
-keep class com.securedocs.app.ads.**     { *; }

# Security helpers kept for KeyStore alias string matching
-keep class com.securedocs.app.security.EncryptionHelper { *; }
-keep class com.securedocs.app.security.RootCheck        { *; }
-keep class com.securedocs.app.security.SignatureValidator { *; }

# Prefs kept for SharedPreferences key matching
-keep class com.securedocs.app.utils.Prefs { *; }

# All other app classes → obfuscate freely (reduces reverse-engineering risk)

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.**                     { *; }
-keep class kotlin.Metadata               { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings  { <fields>; }
-keepclassmembers class kotlin.Lazy      { *; }

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler      { *; }
-dontwarn kotlinx.coroutines.**

# ── Source line info for crash logs ──────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Enums ─────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ────────────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ── Serializable ─────────────────────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
