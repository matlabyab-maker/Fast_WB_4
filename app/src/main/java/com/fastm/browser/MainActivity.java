package com.fastm.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.DownloadManager;
// سایر importهای لازم...

// در متد مربوط به باز کردن تنظیمات دانلود (جایی که قبلاً ACTION_DOWNLOAD_SETTINGS بود):
private void openDownloadSettings() {
    try {
        // روش جایگزین و استاندارد برای باز کردن لیست دانلودها
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    } catch (Exception e) {
        // اگر باز نشد، به تنظیمات کلی بروید
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }
}
