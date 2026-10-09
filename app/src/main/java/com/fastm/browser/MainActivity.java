package com.fastm.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.DownloadManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // بسیار مهم

public class MainActivity extends AppCompatActivity { // حتماً این خط باید باشد

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // اینجا کد اصلی WebView شما قرار دارد
        webView = new WebView(this);
        setContentView(webView);
        
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.google.com");
    }

    // متد اصلاح شده برای دانلودها
    private void openDownloads() {
        try {
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "خطا در سیستم", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // متد کمکی برای نمایش پیام (در صورت نیاز در جاهای دیگر)
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ... بقیه متدها و کدهای خودت را اینجا قرار بده ...
}
