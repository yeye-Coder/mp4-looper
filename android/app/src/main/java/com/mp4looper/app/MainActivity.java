package com.mp4looper.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.widget.FrameLayout;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shows mp4-looper.html (bundled as assets/index.html) full screen in a WebView, and adds what a
 * plain web page can't do on a phone: the system video picker, full-screen video, and saving
 * clips to the Download folder.
 */
public class MainActivity extends Activity {

    private static final int REQ_PICK_VIDEO = 1;
    // Where the video picker starts: the phone's Download folder.
    private static final Uri DOWNLOADS_FOLDER = Uri.parse(
            "content://com.android.externalstorage.documents/document/primary%3ADownload");

    private FrameLayout root;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    // Full-screen video (the ⛶ button in the video controls)
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private OnBackInvokedCallback exitFullscreenOnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Learners watch loops without touching the screen – don't let it turn off.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0E0E12"));
        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        applySystemBarPadding();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                    // saved sentences (localStorage)
        s.setMediaPlaybackRequiresUserGesture(false);    // loop restarts play() without a tap
        s.setAllowContentAccess(true);                   // videos picked from the phone

        web.addJavascriptInterface(new Saver(), "AndroidSaver");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new ChromeClient());

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl("file:///android_asset/index.html");
    }

    /** Android 15+ draws apps edge-to-edge; keep the page clear of the status and navigation bars. */
    private void applySystemBarPadding() {
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            if (customView != null) { v.setPadding(0, 0, 0, 0); return insets; }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets i = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                v.setPadding(i.left, i.top, i.right, i.bottom);
                return WindowInsets.CONSUMED;
            } else {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                return insets.consumeSystemWindowInsets();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }

    // ---------------------------------------------------------------- picking a video

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_VIDEO || fileCallback == null) return;
        Uri[] result = null;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            result = new Uri[]{data.getData()};
        }
        fileCallback.onReceiveValue(result);
        fileCallback = null;
    }

    private class ChromeClient extends WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                         FileChooserParams params) {
            if (fileCallback != null) fileCallback.onReceiveValue(null);
            fileCallback = callback;
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("*/*");
            pick.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*"});
            pick.putExtra(DocumentsContract.EXTRA_INITIAL_URI, DOWNLOADS_FOLDER);   // a hint; ignored if unsupported
            try {
                startActivityForResult(pick, REQ_PICK_VIDEO);
            } catch (ActivityNotFoundException e) {
                fileCallback = null;
                return false;
            }
            return true;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) { callback.onCustomViewHidden(); return; }
            customView = view;
            customViewCallback = callback;
            root.setPadding(0, 0, 0, 0);
            root.addView(view, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            web.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= 33) {
                exitFullscreenOnBack = this::onHideCustomView;
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                        OnBackInvokedDispatcher.PRIORITY_DEFAULT, exitFullscreenOnBack);
            }
        }

        @Override
        public void onHideCustomView() {
            if (customView == null) return;
            root.removeView(customView);
            customView = null;
            web.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= 33 && exitFullscreenOnBack != null) {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(exitFullscreenOnBack);
                exitFullscreenOnBack = null;
            }
            if (customViewCallback != null) customViewCallback.onCustomViewHidden();
            customViewCallback = null;
            root.requestApplyInsets();
        }
    }

    // Android 12 and older: the back button leaves full-screen video first.
    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (customView != null) {
            ((ChromeClient) web.getWebChromeClient()).onHideCustomView();
            return;
        }
        super.onBackPressed();
    }

    // ---------------------------------------------------------------- saving clips

    /** Called from the page (window.AndroidSaver). Writes a clip to the Download folder. */
    private class Saver {
        private final Map<String, Uri> uris = new HashMap<>();
        private final Map<String, OutputStream> streams = new HashMap<>();

        @JavascriptInterface
        public synchronized String begin(String name, String mimeType) {
            try {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                v.put(MediaStore.MediaColumns.IS_PENDING, 1);
                ContentResolver cr = getContentResolver();
                Uri uri = cr.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), v);
                if (uri == null) return "";
                OutputStream out = cr.openOutputStream(uri);
                if (out == null) { cr.delete(uri, null, null); return ""; }
                String id = UUID.randomUUID().toString();
                uris.put(id, uri);
                streams.put(id, out);
                return id;
            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public synchronized boolean append(String id, String base64) {
            OutputStream out = streams.get(id);
            if (out == null) return false;
            try {
                out.write(Base64.decode(base64, Base64.DEFAULT));
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public synchronized String finish(String id) {
            OutputStream out = streams.remove(id);
            Uri uri = uris.remove(id);
            if (out == null || uri == null) return "";
            try {
                out.close();
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContentResolver().update(uri, v, null, null);
                return "the Download folder";
            } catch (Exception e) {
                getContentResolver().delete(uri, null, null);
                return "";
            }
        }

        @JavascriptInterface
        public synchronized void abort(String id) {
            OutputStream out = streams.remove(id);
            Uri uri = uris.remove(id);
            try { if (out != null) out.close(); } catch (Exception ignored) { }
            if (uri != null) getContentResolver().delete(uri, null, null);
        }
    }
}
