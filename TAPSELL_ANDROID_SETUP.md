# SINGULARITY — Tapsell integration status

## Current state

- `index.html` contains a fail-closed JavaScript bridge for a native Android host.
- The game calls `window.PlayAds.rewarded({ unit })` and `window.PlayAds.interstitial({ unit })`.
- The native Android host is expected to expose `window.TapsellBridge.showRewardedAd(requestId, zoneId)` and `window.TapsellBridge.showInterstitialAd(requestId, zoneId)`.
- After the SDK callback, the native host must call `window.onTapsellAdResult(requestId, true)` only when a rewarded ad is completed successfully; otherwise call it with `false`.
- If no native bridge is present, the game does not show a fake ad and does not grant a reward.

## Not yet complete

This repository is currently an HTML game, not an Android Gradle project. The Tapsell Android SDK has not been added or initialized yet, and real ads will not display in a normal browser or from GitHub Pages.

## Needed to finish the Android SDK integration

1. Create/add an Android app host using WebView and a JavaScript interface.
2. Add the currently supported Tapsell SDK dependency and initialization code from Tapsell's official developer documentation / dashboard. Do not guess a dependency version.
3. Configure the real Tapsell App ID and ad Zone IDs (rewarded and interstitial) in the Android app. Do not commit private credentials.
4. Ensure the rewarded callback only returns success after the SDK's verified reward/completion callback.
5. Build and test the APK on a device before publishing.

## App details

- App name: SINGULARITY
- Suggested Android application ID: `com.javadmk1.singularity`
- Important: the application ID must match the one registered in the Tapsell dashboard.

## JavaScript bridge contract

    window.TapsellBridge.showRewardedAd(requestId, zoneId);
    window.TapsellBridge.showInterstitialAd(requestId, zoneId);
    window.onTapsellAdResult(requestId, true);  // rewarded completion only
    window.onTapsellAdResult(requestId, false); // unavailable, skipped, or failed
