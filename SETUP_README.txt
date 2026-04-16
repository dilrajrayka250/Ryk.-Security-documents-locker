===========================================================================
  SecureDocsApp — Complete Android Studio Kotlin Project
  Package: com.securedocs.app
===========================================================================

WHAT'S INCLUDED
───────────────
✅ build.gradle          (Project level)
✅ app/build.gradle      (App level — all deps included)
✅ settings.gradle
✅ gradle.properties
✅ gradlew + gradlew.bat
✅ gradle/wrapper/gradle-wrapper.jar   ← binary included
✅ gradle/wrapper/gradle-wrapper.properties
✅ AndroidManifest.xml   (All permissions + meta-data)
✅ SecureDocsApplication.kt (AdMob initialized here)
✅ SplashActivity.kt
✅ LoginActivity.kt      (PIN set on first run, verify on return)
✅ HomeActivity.kt       (Banner Ad + Interstitial pre-load)
✅ NormalFolderActivity.kt
✅ PrivateFolderActivity.kt (Biometric + AES-256)
✅ ScannerActivity.kt    (CameraX + ML Kit)
✅ BackupActivity.kt
✅ EncryptionHelper.kt   (AES-256/GCM, Android KeyStore)
✅ FileManager.kt
✅ AdManager.kt          (Banner + Interstitial, TEST IDs)
✅ BillingManager.kt     (Google Play Billing placeholder)
✅ Prefs.kt              (EncryptedSharedPreferences)
✅ PermissionHelper.kt   (Runtime permissions, API 24–34)
✅ All 7 layout XMLs     (ViewBinding ready)
✅ strings / colors / themes / drawables

===========================================================================
  STEP 1 — Import into Android Studio
===========================================================================
1. Android Studio → File → Open → select "SecureDocsApp" folder
2. Wait for Gradle Sync (requires Internet)

===========================================================================
  STEP 2 — Fix local.properties
===========================================================================
Update sdk.dir in local.properties:
  Mac:     sdk.dir=/Users/NAME/Library/Android/sdk
  Windows: sdk.dir=C\:\\Users\\NAME\\AppData\\Local\\Android\\Sdk
  Linux:   sdk.dir=/home/NAME/Android/Sdk

===========================================================================
  STEP 3 — AdMob (TEST IDs already set — safe to run)
===========================================================================
Current IDs are Google's OFFICIAL TEST IDs. Ads will show test banners.

Before publishing, replace in AdManager.kt:
  BANNER_TEST_ID       → your real banner unit ID
  INTERSTITIAL_TEST_ID → your real interstitial unit ID

And in AndroidManifest.xml meta-data:
  ca-app-pub-3940256099942544~3347511713 → your real App ID

===========================================================================
  STEP 4 — Google Play Billing (₹9 or any price)
===========================================================================
1. Publish app on Google Play Console (Internal testing)
2. Monetize → In-app products → Create product
3. Product ID: secure_docs_premium
4. Set price, activate
5. Test with a licensed test Gmail account

===========================================================================
  STEP 5 — Build & Run
===========================================================================
  Run → Select device → Build and Run
  Release: Build → Generate Signed Bundle/APK

===========================================================================
  ARCHITECTURE
===========================================================================
  ui/          → All Activities (ViewBinding)
  ads/         → AdManager (banner + interstitial)
  billing/     → BillingManager (Play Billing)
  security/    → EncryptionHelper (AES-256/GCM)
  storage/     → FileManager (normal + encrypted files)
  utils/       → Prefs, PermissionHelper

===========================================================================
  Ad Placement
===========================================================================
  Banner      → HomeActivity bottom (hidden for premium)
  Interstitial→ Shown before opening Normal Documents
===========================================================================
