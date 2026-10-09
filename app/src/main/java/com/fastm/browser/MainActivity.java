package com.fastm.browser;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;

public class MainActivity extends Activity {
    private WebView web; private EditText address; private LinearLayout favoritesPanel; private boolean desktop=false;
    private android.content.SharedPreferences prefs; private ValueCallback<Uri[]> uploadCallback; private static final int FILE_REQUEST=41;
    private final Executor proxyExecutor=command -> new Thread(command,"proxy-config").start();
    private int blue=Color.rgb(21,101,192);

    @Override public void onCreate(Bundle state) { super.onCreate(state); prefs=getSharedPreferences("fastwb",MODE_PRIVATE); buildUI();
        applySavedProxy();
        Uri incoming=getIntent().getData(); if(incoming!=null) load(incoming.toString()); else load("https://www.google.com"); }
    private void buildUI(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); setContentView(root);
        HorizontalScrollView topScroll=new HorizontalScrollView(this); topScroll.setHorizontalScrollBarEnabled(false); LinearLayout top=row();
        add(top,"ارسال",v->shareCurrent()); add(top,"فایل",v->chooseFile()); add(top,"Copy MW",v->copyUrl()); add(top,"PC/Android",v->toggleDesktop()); add(top,"Past",v->pasteUrl()); topScroll.addView(top); root.addView(topScroll,new LinearLayout.LayoutParams(-1,48));
        web=new WebView(this); web.setBackgroundColor(Color.WHITE); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.getSettings().setAllowFileAccess(true); web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView view,ValueCallback<Uri[]> callback,FileChooserParams params){ if(uploadCallback!=null) uploadCallback.onReceiveValue(null); uploadCallback=callback; try{startActivityForResult(params.createIntent(),FILE_REQUEST);}catch(Exception e){uploadCallback=null; callback.onReceiveValue(null); toast("امکان انتخاب فایل نیست");} return true; }
        });
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return false;}
            @Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap icon){address.setText(url);}
            @Override public void onPageFinished(WebView view,String url){address.setText(url); setTitle(view.getTitle()==null?"Fast WB 4":view.getTitle());}
        });
        web.setDownloadListener((url,userAgent,contentDisposition,mimeType,contentLength)->{try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url)); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i);}catch(Exception e){toast("برنامه‌ای برای دریافت این فایل پیدا نشد");}});
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout addr=row(); TextView globe=new TextView(this); globe.setText("🌐"); globe.setTextSize(20); globe.setGravity(Gravity.CENTER); addr.addView(globe,new LinearLayout.LayoutParams(40,-1));
        address=new EditText(this); address.setSingleLine(true); address.setHint("نشانی وب یا جستجو"); address.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI); address.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_GO); addr.addView(address,new LinearLayout.LayoutParams(0,48,1)); address.setOnEditorActionListener((v,a,event)->{goAddress();return true;});
        add(addr,"☆",v->addFavorite()); add(addr,"⋮",v->showMenu()); root.addView(addr,new LinearLayout.LayoutParams(-1,50));
        HorizontalScrollView bottomScroll=new HorizontalScrollView(this); bottomScroll.setHorizontalScrollBarEnabled(false); LinearLayout bottom=row();
        add(bottom,"Back",v->{if(web.canGoBack())web.goBack();}); add(bottom,"Forward",v->{if(web.canGoForward())web.goForward();}); add(bottom,"Enter",v->goAddress()); add(bottom,"Favorites",v->showFavorites()); add(bottom,"Downloads",v->openDownloads()); add(bottom,"New Tab",v->load("about:blank")); add(bottom,"Save",v->savePage()); add(bottom,"IP Set",v->showIPSet()); bottomScroll.addView(bottom); root.addView(bottomScroll,new LinearLayout.LayoutParams(-1,50));
        favoritesPanel=new LinearLayout(this); favoritesPanel.setOrientation(LinearLayout.VERTICAL); favoritesPanel.setVisibility(View.GONE); root.addView(favoritesPanel);
    }
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void add(LinearLayout l,String text,View.OnClickListener click){Button b=new Button(this);b.setText(text);b.setTextSize(12);b.setAllCaps(false);b.setPadding(7,0,7,0);l.addView(b,new LinearLayout.LayoutParams(-2,-1));b.setOnClickListener(click);}
    private void load(String url){if(web==null)return; if(url==null||url.trim().isEmpty())return; String u=url.trim(); if(!u.equals("about:blank")&&!u.matches("(?i)^[a-z][a-z0-9+.-]*://.*")&&!u.startsWith("file:")){ if(u.contains(" ")||!u.contains("."))u="https://www.google.com/search?q="+Uri.encode(u); else u="https://"+u; } web.loadUrl(u);}
    private void goAddress(){String u=address.getText().toString(); load(u); ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void showMenu(){String[] opts={"بازخوانی / Refresh","خانه / Home","اشتراک‌گذاری صفحه","ذخیره صفحه HTML","تنظیم IP Set"};new AlertDialog.Builder(this).setItems(opts,(d,n)->{if(n==0)web.reload();else if(n==1)load("https://www.google.com");else if(n==2)shareCurrent();else if(n==3)savePage();else showIPSet();}).show();}
    private void shareCurrent(){String u=web.getUrl();if(u==null)u="";Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,u);startActivity(Intent.createChooser(i,"اشتراک‌گذاری صفحه"));}
    private void copyUrl(){String u=web.getUrl();if(u==null)u="";((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("URL",u));toast("نشانی کپی شد");}
    private void pasteUrl(){try{CharSequence t=((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).getPrimaryClip().getItemAt(0).coerceToText(this);address.setText(t);load(t.toString());}catch(Exception e){toast("متنی در کلیپ‌بورد نیست");}}
    private void toggleDesktop(){desktop=!desktop;web.getSettings().setUserAgentString(desktop?"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/120 Safari/537.36":WebSettings.getDefaultUserAgent(this));web.reload();toast(desktop?"نمایش دسکتاپ":"نمایش موبایل");}
    private void chooseFile(){Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(i,FILE_REQUEST);}catch(Exception e){toast("انتخاب فایل در دسترس نیست");}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==FILE_REQUEST&&uploadCallback!=null){Uri[] r=null;if(result==RESULT_OK&&data!=null){if(data.getClipData()!=null){int n=data.getClipData().getItemCount();r=new Uri[n];for(int j=0;j<n;j++)r[j]=data.getClipData().getItemAt(j).getUri();}else if(data.getData()!=null)r=new Uri[]{data.getData()};}uploadCallback.onReceiveValue(r);uploadCallback=null;}}
    private void addFavorite(){String u=web.getUrl();if(u==null||u.isEmpty())return;Set<String>s=new HashSet<>(prefs.getStringSet("favorites",new HashSet<>()));s.add(u);prefs.edit().putStringSet("favorites",s).apply();toast("به علاقه‌مندی‌ها افزوده شد");}
    private void showFavorites(){Set<String>s=prefs.getStringSet("favorites",new HashSet<>());String[] a=s.toArray(new String[0]);if(a.length==0){toast("موردی ذخیره نشده");return;}new AlertDialog.Builder(this).setTitle("علاقه‌مندی‌ها").setItems(a,(d,n)->load(a[n])).setNeutralButton("حذف مورد جاری",(d,n)->{String u=web.getUrl();Set<String>x=new HashSet<>(prefs.getStringSet("favorites",new HashSet<>()));x.remove(u);prefs.edit().putStringSet("favorites",x).apply();}).show();}
    private void openDownloads(){try{startActivity(new Intent(android.provider.Settings.ACTION_DOWNLOAD_SETTINGS));}catch(Exception e){toast("تنظیمات دانلود در دسترس نیست");}}
    private void savePage(){try{File dir=new File(getFilesDir(),"saved_pages");if(!dir.exists())dir.mkdirs();String name="page_"+System.currentTimeMillis()+".mhtml";File out=new File(dir,name);web.saveWebArchive(out.getAbsolutePath(),false,path->{if(path==null){toast("ذخیره صفحه ناموفق بود");return;}try{Uri uri=FileProvider.getUriForFile(this,getPackageName()+".files",new File(path));Intent i=new Intent(Intent.ACTION_SEND);i.setType("message/rfc822");i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"اشتراک / خروجی صفحه ذخیره‌شده"));}catch(Exception e){toast("صفحه ذخیره شد: "+path);}});}catch(Exception e){toast("ذخیره صفحه ناموفق بود");}}
    private void showIPSet(){String[] sections={"Proxy Port","Vray","Tunnel","DNS"};new AlertDialog.Builder(this).setTitle("تنظیمات IP").setItems(sections,(d,n)->{if(n==0)proxyDialog();if(n==1)textConfigDialog("Vray","vray", "پیکربندی یا لینک اشتراک را وارد کنید. هسته V2Ray/Xray همراه برنامه نیست؛ خروجی به برنامه سازگار خارجی سپرده می‌شود.");if(n==2)textConfigDialog("Tunnel","tunnel","پروفایل OpenVPN/WireGuard را وارد کنید. به برنامه VPN سازگار نیاز است و Android برای اتصال تأیید کاربر می‌خواهد.");if(n==3)dnsDialog();}).show();}
    private EditText edit(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);return e;}
    private void proxyDialog(){LinearLayout box=new LinearLayout(this);box.setPadding(18,4,18,0);box.setOrientation(LinearLayout.VERTICAL);EditText host=edit("Host");host.setText(prefs.getString("proxy_host",""));EditText port=edit("Port");port.setInputType(2);port.setText(prefs.getString("proxy_port",""));box.addView(host);box.addView(port);new AlertDialog.Builder(this).setTitle("Proxy Port").setMessage("اعمال پروکسی در WebView نیازمند پشتیبانی WebView است.").setView(box).setPositiveButton("Apply",(d,n)->{String h=host.getText().toString().trim();int p;try{p=Integer.parseInt(port.getText().toString().trim());if(h.isEmpty()||p<1||p>65535)throw new Exception();}catch(Exception e){toast("Host یا Port نامعتبر است");return;}prefs.edit().putString("proxy_host",h).putString("proxy_port",String.valueOf(p)).putBoolean("proxy_enabled",true).apply();applyProxy(h,p);}).setNegativeButton("Disable / Clear",(d,n)->{prefs.edit().putBoolean("proxy_enabled",false).remove("proxy_host").remove("proxy_port").apply();clearProxy();}).setNeutralButton("Cancel",null).show();}
    private void applySavedProxy(){if(prefs.getBoolean("proxy_enabled",false)){String h=prefs.getString("proxy_host","");int port;try{port=Integer.parseInt(prefs.getString("proxy_port","0"));if(!h.isEmpty()&&port>0&&port<=65535)applyProxy(h,port);}catch(Exception ignored){}}}
    private void applyProxy(String host,int port){if(!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)){toast("این نسخه WebView از Proxy override پشتیبانی نمی‌کند؛ تنظیم ذخیره شد اما اعمال نشد.");return;}try{ProxyConfig config=new ProxyConfig.Builder().addProxyRule("http://"+host+":"+port).build();ProxyController.getInstance().setProxyOverride(config,proxyExecutor,()->runOnUiThread(()->toast("پروکسی برای WebView اعمال شد")));}catch(Exception e){toast("اعمال پروکسی ممکن نشد؛ نسخه WebView را بررسی کنید");}}
    private void clearProxy(){if(!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)){toast("WebView این قابلیت را ندارد؛ پروکسی محلی پاک شد ولی لغو override در دسترس نیست.");return;}ProxyController.getInstance().clearProxyOverride(proxyExecutor,()->runOnUiThread(()->toast("Proxy override پاک شد")));}
    private void textConfigDialog(String title,String key,String info){LinearLayout box=new LinearLayout(this);box.setPadding(16,0,16,0);box.setOrientation(LinearLayout.VERTICAL);TextView msg=new TextView(this);msg.setText(info);box.addView(msg);EditText input=new EditText(this);input.setGravity(Gravity.TOP|Gravity.START);input.setMinLines(4);input.setMaxLines(8);input.setSingleLine(false);input.setHint("متن پیکربندی یا لینک اشتراک");input.setText(prefs.getString(key,""));box.addView(input,new LinearLayout.LayoutParams(-1,150));new AlertDialog.Builder(this).setTitle(title).setView(box).setPositiveButton("ذخیره",(d,n)->{prefs.edit().putString(key,input.getText().toString()).apply();toast("ذخیره شد");}).setNegativeButton("لغو",null).setNeutralButton("Share / Export",(d,n)->shareConfig(input.getText().toString(),title)).show();}
    private void shareConfig(String text,String title){if(text==null||text.trim().isEmpty()){toast("ابتدا پیکربندی را وارد کنید");return;}Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,text);try{startActivity(Intent.createChooser(i,"ارسال به برنامه سازگار "+title));}catch(Exception e){toast("برنامه‌ای برای دریافت پیکربندی پیدا نشد");}}
    private void dnsDialog(){LinearLayout box=new LinearLayout(this);box.setPadding(15,0,15,0);box.setOrientation(LinearLayout.VERTICAL);TextView info=new TextView(this);info.setText("این برنامه DNS سیستم را بی‌صدا تغییر نمی‌دهد. مقدار را ذخیره/کپی کنید و در تنظیمات Private DNS اندروید به‌صورت دستی اعمال کنید.");box.addView(info);EditText host=edit("Private DNS hostname");host.setText(prefs.getString("dns_host","dns.google"));box.addView(host);LinearLayout presets=row();for(String d:new String[]{"dns.google","one.one.one.one","dns.quad9.net"})add(presets,d,v->host.setText(d));box.addView(presets);new AlertDialog.Builder(this).setTitle("DNS / Private DNS").setView(box).setPositiveButton("ذخیره",(d,n)->{String h=host.getText().toString().trim();if(h.isEmpty()){toast("نام میزبان خالی است");return;}prefs.edit().putString("dns_host",h).apply();toast("ذخیره شد؛ برای اعمال سیستم‌wide تنظیمات را باز کنید");}).setNeutralButton("Copy",(d,n)->{String h=host.getText().toString().trim();((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Private DNS",h));toast("نام میزبان کپی شد");}).setNegativeButton("Private DNS settings",(d,n)->openDnsSettings()).show();}
    private void openDnsSettings(){try{startActivity(new Intent("android.settings.PRIVATE_DNS_SETTINGS"));}catch(Exception e){try{startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));}catch(Exception ex){toast("تنظیمات شبکه در دسترس نیست");}}}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
