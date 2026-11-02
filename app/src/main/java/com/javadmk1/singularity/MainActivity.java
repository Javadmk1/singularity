package com.javadmk1.singularity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

public class MainActivity extends Activity {
    // Official Google sample IDs: test ads only.
    private static final String SAMPLE_APP_ID = "ca-app-pub-3940256099942544~3347511713";
    private static final String TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741";

    private WebView web;
    private AdView bannerAdView;
    private LinearLayout root;
    private Thread.UncaughtExceptionHandler previousCrashHandler;
    private boolean recoveringRenderer = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installCrashLogger();
        showPreviousCrashIfAny();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF070A24);

        web = new WebView(this);
        web.setBackgroundColor(0xFF070A24);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view, android.webkit.WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
                if (isFinishing() || isDestroyed() || recoveringRenderer) return true;
                recoveringRenderer = true;
                runOnUiThread(() -> {
                    try {
                        if (view.getParent() instanceof LinearLayout) {
                            ((LinearLayout) view.getParent()).removeView(view);
                        }
                        view.destroy();
                        createGameWebView(assetLoader);
                        Toast.makeText(MainActivity.this, "بازی پس از خطای گرافیکی دوباره بارگذاری شد", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        logDiagnostic("WebView renderer recovery failed", e);
                    } finally {
                        recoveringRenderer = false;
                    }
                });
                return true;
            }
        });

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        web.setWebChromeClient(new WebChromeClient());

        root.addView(web, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html");

        initializeTestAds();
    }

    private void installCrashLogger() {
        previousCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            logDiagnostic("UNCAUGHT CRASH on thread " + thread.getName(), error);
            if (previousCrashHandler != null) previousCrashHandler.uncaughtException(thread, error);
            else Process.killProcess(Process.myPid());
        });
    }

    private void logDiagnostic(String title, Throwable error) {
        try (FileWriter writer = new FileWriter(new File(getFilesDir(), "last_crash.txt"), false)) {
            writer.write(title + "\\n");
            if (error != null) {
                StringWriter trace = new StringWriter();
                error.printStackTrace(new PrintWriter(trace));
                writer.write(trace.toString());
            }
        } catch (Exception ignored) { }
    }

    private void showPreviousCrashIfAny() {
        File report = new File(getFilesDir(), "last_crash.txt");
        if (report.exists()) {
            Toast.makeText(this, "خطای قبلی ثبت شد؛ فایل last_crash.txt برای بررسی ذخیره شده است", Toast.LENGTH_LONG).show();
        }
    }

    private void createGameWebView(WebViewAssetLoader assetLoader) {
        web = new WebView(this);
        web.setBackgroundColor(0xFF070A24);
        web.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        web.setWebChromeClient(new WebChromeClient());
        root.addView(web, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private void initializeTestAds() {
        new Thread(() -> {
            try {
                MobileAds.initialize(
                        getApplicationContext(),
                        new InitializationConfig.Builder(SAMPLE_APP_ID).build(),
                        status -> runOnUiThread(this::loadTestBanner));
            } catch (Exception ignored) {
                // Ad initialization failure must not prevent the game from starting.
            }
        }).start();
    }

    private void loadTestBanner() {
        if (isFinishing() || isDestroyed() || root == null) return;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        AdSize size = AdSize.getLargeAnchoredAdaptiveBannerAdSize(this, widthDp);

        bannerAdView = new AdView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(bannerAdView, params);

        bannerAdView.loadAd(
                new BannerAdRequest.Builder(TEST_BANNER_ID, size).build(),
                new AdLoadCallback<BannerAd>() {
                    @Override
                    public void onAdLoaded(BannerAd ad) {
                        // Test banner loaded.
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        if (bannerAdView != null && root != null) {
                            root.removeView(bannerAdView);
                            bannerAdView.destroy();
                            bannerAdView = null;
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
        if (web != null) {
            web.stopLoading();
            web.loadUrl("about:blank");
            web.destroy();
            web = null;
        }
        root = null;
        super.onDestroy();
    }
}
