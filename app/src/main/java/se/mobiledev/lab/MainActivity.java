package se.mobiledev.lab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class MainActivity extends Activity {
    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/index.html";
    private static final String FONT_CSS_HOST = "fonts.googleapis.com";
    private static final String FONT_CSS_ASSET = "fonts/fredoka.css";
    private static final long BACK_EXIT_WINDOW_MS = 2000;

    // Closes the open game modal or leaves a running challenge. Returns "handled" if it did either.
    private static final String BACK_SCRIPT =
        "(function(){"
            + "var l=document.getElementById('modalLayer');"
            + "if(l&&!l.hidden){var c=l.querySelector('.mclose');if(c)c.click();return 'handled'}"
            + "var e=document.getElementById('exitBtn');"
            + "if(e&&!e.hidden){e.click();return 'handled'}"
            + "return 'none'})()";

    // The game saves to localStorage on 'pagehide'; fire it before the WebView is paused.
    private static final String SAVE_SCRIPT = "window.dispatchEvent(new Event('pagehide'))";

    private WebView webView;
    private long lastBackAt;
    private OnBackInvokedCallback backCallback;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allowDisplayCutout();

        webView = findViewById(R.id.gameView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setGeolocationEnabled(false);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setLongClickable(false);
        webView.setHapticFeedbackEnabled(false);
        webView.setOnLongClickListener(v -> true);
        webView.setWebViewClient(new GameClient());

        if (Build.VERSION.SDK_INT >= 33) {
            backCallback = this::handleBack;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, backCallback);
        }

        webView.loadUrl(START_URL);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        handleBack();
    }

    private void handleBack() {
        webView.evaluateJavascript(BACK_SCRIPT, result -> {
            if ("\"handled\"".equals(result)) {
                lastBackAt = 0;
                return;
            }
            long now = SystemClock.elapsedRealtime();
            if (lastBackAt != 0 && now - lastBackAt <= BACK_EXIT_WINDOW_MS) {
                finish();
            } else {
                lastBackAt = now;
                Toast.makeText(this, R.string.back_exit_hint, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        webView.evaluateJavascript(SAVE_SCRIPT, null);
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= 33 && backCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback);
        }
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void allowDisplayCutout() {
        if (Build.VERSION.SDK_INT < 28) {
            return;
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = Build.VERSION.SDK_INT >= 30
            ? WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            : WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(lp);
    }

    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) {
            w.setDecorFitsSystemWindows(false);
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            w.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private WebResourceResponse blocked() {
        return new WebResourceResponse("text/plain", "utf-8", 403, "Blocked",
            Collections.<String, String>emptyMap(), new ByteArrayInputStream(new byte[0]));
    }

    private WebResourceResponse asset(String path, String mime, String encoding) {
        try {
            InputStream in = getAssets().open(path, AssetManager.ACCESS_STREAMING);
            return new WebResourceResponse(mime, encoding, 200, "OK",
                Collections.<String, String>emptyMap(), in);
        } catch (IOException e) {
            return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
                Collections.<String, String>emptyMap(), new ByteArrayInputStream(new byte[0]));
        }
    }

    private static String mimeFor(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".woff2")) return "font/woff2";
        return null;
    }

    private class GameClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String host = request.getUrl().getHost();
            if (FONT_CSS_HOST.equals(host)) {
                return asset(FONT_CSS_ASSET, "text/css", "utf-8");
            }
            if (!HOST.equals(host) || !"https".equals(request.getUrl().getScheme())) {
                return blocked();
            }
            String path = request.getUrl().getPath();
            if (path == null || path.equals("/")) {
                path = "/index.html";
            }
            String mime = mimeFor(path);
            if (mime == null || path.contains("..")) {
                return blocked();
            }
            return asset(path.substring(1), mime, mime.equals("font/woff2") ? null : "utf-8");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // Everything stays inside the app; links to other origins are dropped.
            return !HOST.equals(request.getUrl().getHost());
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            recreate();
            return true;
        }
    }
}
