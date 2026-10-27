# Adivery Android bridge setup (SINGULARITY)

The HTML game now exposes an `AdiveryBridge` JavaScript interface and calls:
- `showRewardedAd(requestId, placementId)`
- `showInterstitialAd(requestId, placementId)`
- `window.onAdiveryAdResult(requestId, success)`

## Important
This repository currently contains an HTML game, not a complete Android Gradle project. The bridge alone does not load or show ads. A native Android WebView host must add the official Adivery Android SDK, initialize it with the app ID, prepare/load the requested placement, and implement the bridge methods.

## Required dashboard values
- Adivery App ID: supplied by the owner (do not hard-code publicly in source if the dashboard treats it as a secret).
- Rewarded placement ID: needed for rewarded ads.
- Interstitial placement ID: optional, needed for interstitial ads.

## Reward integrity
Only call `window.onAdiveryAdResult(requestId, true)` from the native SDK's verified rewarded-completion callback. Do not grant game coins/revive rewards when an ad merely opens, closes, or fails.

## Next steps
1. Create/obtain the Rewarded placement ID in the Adivery dashboard.
2. Build a native Android WebView project and add the current official Adivery SDK dependency using Adivery's official integration documentation.
3. Expose an `AdiveryBridge` interface to the WebView and implement SDK callbacks.
4. Configure a GitHub Actions Android build and test with Adivery test ads before switching to live inventory.

The HTML bridge is committed, but a real Android APK with working ads has not yet been built.
