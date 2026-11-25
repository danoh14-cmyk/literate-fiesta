package com.fittimer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowManager;
import android.graphics.Color;
import android.webkit.JavascriptInterface;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.speech.tts.Voice;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends Activity {
    private WebView webView;
    private android.media.ToneGenerator tone;
    private long lastBackPressedAt = 0L;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    public class VoiceBridge {
        @JavascriptInterface public void beep() {
            mainHandler.post(() -> { try {
                if (tone == null) tone = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 75);
                tone.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 180);
            } catch (RuntimeException ignored) {} });
        }
        @JavascriptInterface public void setLanguage(String language) {
            mainHandler.post(() -> { if (ttsReady && tts != null) {
                tts.setLanguage("en".equals(language) ? Locale.US : Locale.KOREAN);
            }});
        }
        @JavascriptInterface public void speak(String message) {
            mainHandler.post(() -> {
                if (ttsReady && tts != null) {
                    tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "fit_timer_voice");
                }
            });
        }
        @JavascriptInterface public void stop() {
            mainHandler.post(() -> { if (tts != null) tts.stop(); });
        }
        @JavascriptInterface public boolean isReady() { return ttsReady; }
        @JavascriptInterface public String getVoices() {
            JSONArray result = new JSONArray();
            if (!ttsReady || tts == null) return result.toString();
            try {
                java.util.Set<Voice> voices = tts.getVoices();
                if (voices != null) for (Voice voice : voices) {
                    if (!voice.getLocale().getLanguage().equals("en") && !voice.getLocale().getLanguage().equals("ko")) continue;
                    JSONObject item = new JSONObject();
                    item.put("id", voice.getName());
                    item.put("label", voice.getName());
                    item.put("network", voice.isNetworkConnectionRequired());
                    result.put(item);
                }
            } catch (Exception ignored) {}
            return result.toString();
        }
        @JavascriptInterface public void configure(String voiceId, float rate, float pitch) {
            mainHandler.post(() -> {
                if (!ttsReady || tts == null) return;
                tts.setSpeechRate(Math.max(0.6f, Math.min(1.5f, rate)));
                tts.setPitch(Math.max(0.7f, Math.min(1.5f, pitch)));
                if (voiceId != null && !voiceId.isEmpty()) {
                    try {
                        for (Voice v : tts.getVoices()) {
                            if (v.getName().equals(voiceId) && ( "ko".equals(v.getLocale().getLanguage()) || "en".equals(v.getLocale().getLanguage()) )) {
                                tts.setVoice(v); break;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            });
        }
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 11, 20));
        getWindow().setNavigationBarColor(Color.rgb(5, 11, 20));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) {
                int result = tts.setLanguage(Locale.KOREAN);
                tts.setSpeechRate(1.0f);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(5, 11, 20));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.addJavascriptInterface(new VoiceBridge(), "FitTimerVoice");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }
    @Override public void onBackPressed() {
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastBackPressedAt > 2000L) {
            lastBackPressedAt = now;
            Toast.makeText(this, "종료하려면 뒤로가기를 한 번 더 누르세요", Toast.LENGTH_SHORT).show();
            return;
        }
        lastBackPressedAt = 0L;
        new AlertDialog.Builder(this)
            .setTitle("FIT TIMER 종료")
            .setMessage("종료하시겠습니까?")
            .setPositiveButton("확인", (dialog, which) -> finish())
            .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
            .show();
    }
    @Override protected void onDestroy() {
        if (tone != null) { tone.release(); tone = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
