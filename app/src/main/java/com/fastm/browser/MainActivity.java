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
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // تنظیمات اولیه WebView
        webView = new WebView(this);
        setContentView(webView);
        
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true); // برای سایت‌های مدرن ضروری است
        
        webView.loadUrl("https://www.google.com");
    }

    // متد اصلاح شده برای باز کردن لیست دانلودها
    private void openDownloads() {
        try {
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            try {
                // اگر دانلود منیجر در دسترس نبود، برو به تنظیمات
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "خطا در باز کردن تنظیمات", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // این متد را برای استفاده در جاهای دیگر کپی کن
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // این بخش برای مدیریت دکمه بازگشت در مرورگر است (اگر دکمه Back را زدی به جای بستن، به صفحه قبل برگردد)
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
} // این آکولاد پایانی بسیار حیاتی است
