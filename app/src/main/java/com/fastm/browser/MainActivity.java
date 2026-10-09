package com.fastm.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.download.DownloadManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // ... (بقیه متغیرها و کدهای خودت را اینجا نگه دار) ...

    // این دقیقاً همان متدی است که در خط ۶۵ خطا می‌داد. 
    // من آن را کاملاً بازنویسی کردم تا دیگر خطا ندهد.
    private void openDownloads() {
        try {
            // استفاده از استاندارد اندروید برای باز کردن دانلودها
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // اگر به هر دلیلی نشد، به تنظیمات برود تا برنامه کرش نکند
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "خطا در سیستم", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ... (بقیه کدهای کلاس تو) ...
}
