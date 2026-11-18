package com.example.myvideodownloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private EditText urlInput, profileInput;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
        handleIncoming(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncoming(intent);
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(22), dp(20), dp(28));
        page.setBackgroundColor(Color.rgb(246,248,252));
        scroll.addView(page);

        TextView title = new TextView(this);
        title.setText("Video Downloader");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.rgb(20,45,80));
        add(page,title,0);

        TextView subtitle = new TextView(this);
        subtitle.setText("Facebook खोलें • Reel शेयर करें • अनुमति वाले वीडियो सेव करें");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.rgb(75,85,100));
        add(page,subtitle,8);

        section(page,"Facebook प्रोफाइल");
        profileInput = new EditText(this);
        profileInput.setSingleLine(true);
        profileInput.setTextSize(14);
        profileInput.setHint("https://www.facebook.com/username");
        add(page,profileInput,6);
        button(page,"प्रोफाइल खोलें",v -> openFacebookProfile());
        button(page,"Facebook Reels खोलें",v -> openUrl("https://www.facebook.com/reel/"));

        section(page,"Reel लिंक");
        TextView instructions = new TextView(this);
        instructions.setText("Facebook में Reel खोलें → Share → इस ऐप को चुनें। शेयर किया गया लिंक नीचे दिखाई देगा। जरूरत हो तो Paste दबाएँ।");
        instructions.setTextSize(14);
        instructions.setTextColor(Color.rgb(65,75,90));
        add(page,instructions,6);

        urlInput = new EditText(this);
        urlInput.setMinLines(2);
        urlInput.setTextSize(14);
        urlInput.setHint("यहाँ Reel या direct video link आएगा");
        add(page,urlInput,8);
        button(page,"लिंक Paste करें",v -> pasteFromClipboard());
        button(page,"लिंक Copy करें",v -> copyLink());
        button(page,"लिंक Share करें",v -> shareLink());

        section(page,"वीडियो डाउनलोड");
        TextView downloadInfo = new TextView(this);
        downloadInfo.setText("यह संस्करण केवल ऐसे सीधे HTTPS वीडियो फ़ाइल लिंक डाउनलोड करता है जिनके अंत में .mp4, .m4v या .webm हो। Facebook Reel का पेज लिंक वीडियो फ़ाइल नहीं है, इसलिए उससे अपने-आप डाउनलोड नहीं होगा।");
        downloadInfo.setTextSize(13);
        downloadInfo.setTextColor(Color.rgb(65,75,90));
        add(page,downloadInfo,6);
        button(page,"वीडियो डाउनलोड करें",v -> startDownload());

        status = new TextView(this);
        status.setText("तैयार है। Reel को Share करके इस ऐप में भेजें।");
        status.setTextSize(14);
        status.setTextColor(Color.rgb(25,90,65));
        add(page,status,18);

        TextView privacy = new TextView(this);
        privacy.setText("सुरक्षा: यह ऐप Facebook पासवर्ड नहीं माँगता और न ही Facebook में लॉगिन करवाता है। Private वीडियो या प्लेटफ़ॉर्म की सुरक्षा को bypass नहीं करता। केवल वही वीडियो सेव करें जिन्हें डाउनलोड करने की अनुमति हो।");
        privacy.setTextSize(12);
        privacy.setTextColor(Color.rgb(105,105,115));
        add(page,privacy,20);
        setContentView(scroll);
    }

    private void section(LinearLayout p,String s) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(17);
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setTextColor(Color.rgb(35,55,80));
        add(p,t,20);
    }

    private void button(LinearLayout p,String label,View.OnClickListener listener) {
        Button b=new Button(this); b.setText(label); b.setOnClickListener(listener); add(p,b,7);
    }

    private void openFacebookProfile() {
        String raw=profileInput.getText().toString().trim();
        Uri u=validHttps(raw);
        if(u==null){setStatus("कृपया सही HTTPS Facebook प्रोफाइल लिंक डालें।");return;}
        String h=u.getHost().toLowerCase(Locale.ROOT);
        if(!(h.equals("facebook.com")||h.endsWith(".facebook.com"))){
            setStatus("यह Facebook प्रोफाइल लिंक नहीं है।");return;
        }
        openUrl(raw);
    }

    private void openUrl(String value) {
        try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(value))); }
        catch(Exception e){setStatus("लिंक नहीं खुल सका। इंटरनेट कनेक्शन जाँचें।");}
    }

    private Uri validHttps(String value) {
        try {
            Uri u=Uri.parse(value);
            if("https".equalsIgnoreCase(u.getScheme()) && u.getHost()!=null) return u;
        } catch(Exception ignored) {}
        return null;
    }

    private void pasteFromClipboard() {
        ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(cm!=null && cm.hasPrimaryClip()) {
            ClipData d=cm.getPrimaryClip();
            if(d!=null && d.getItemCount()>0) {
                CharSequence s=d.getItemAt(0).coerceToText(this);
                if(s!=null){urlInput.setText(s.toString().trim());setStatus("लिंक Paste हो गया।");return;}
            }
        }
        setStatus("Clipboard खाली है।");
    }

    private void copyLink() {
        String s=urlInput.getText().toString().trim();
        if(s.isEmpty()){setStatus("पहले लिंक डालें।");return;}
        ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Video link",s));
        setStatus("लिंक Copy हो गया।");
    }

    private void shareLink() {
        String s=urlInput.getText().toString().trim();
        if(s.isEmpty()){setStatus("पहले Reel लिंक शेयर या Paste करें।");return;}
        Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,s);
        startActivity(Intent.createChooser(i,"लिंक शेयर करें"));
    }

    private void handleIncoming(Intent intent) {
        if(intent==null || urlInput==null) return;
        String incoming=null;
        if(Intent.ACTION_SEND.equals(intent.getAction())) incoming=intent.getStringExtra(Intent.EXTRA_TEXT);
        else if(Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData()!=null) incoming=intent.getData().toString();
        if(incoming!=null) {
            String link=findHttpLink(incoming);
            if(link!=null){urlInput.setText(link);setStatus("Facebook से शेयर किया गया लिंक मिल गया। डाउनलोड लिंक के प्रकार पर निर्भर है।");}
            else setStatus("शेयर किए गए टेक्स्ट में लिंक नहीं मिला।");
        }
    }

    private String findHttpLink(String value) {
        String s=value.trim();
        int a=s.indexOf("https://"), b=s.indexOf("http://");
        int start=(a<0)?b:(b<0?a:Math.min(a,b));
        if(start<0)return null;
        String link=s.substring(start).split("\\s+")[0];
        while(link.endsWith(")")||link.endsWith("]")||link.endsWith(",")||link.endsWith(".")) link=link.substring(0,link.length()-1);
        return link;
    }

    private void startDownload() {
        String raw=urlInput.getText().toString().trim();
        Uri u=validHttps(raw);
        if(u==null){setStatus("सही HTTPS वीडियो लिंक डालें।");return;}
        String host=u.getHost().toLowerCase(Locale.ROOT);
        if(host.equals("facebook.com")||host.endsWith(".facebook.com")||host.equals("fb.watch")||host.endsWith(".fb.watch")){
            setStatus("यह Facebook Reel का पेज लिंक है, direct video file नहीं। इस ऐप में इससे वीडियो फ़ाइल नहीं निकाली जा सकती।");return;
        }
        String path=u.getPath()==null?"":u.getPath().toLowerCase(Locale.ROOT);
        String ext=path.endsWith(".mp4")?".mp4":path.endsWith(".m4v")?".m4v":path.endsWith(".webm")?".webm":null;
        if(ext==null){setStatus("सीधा .mp4, .m4v या .webm फ़ाइल लिंक ही समर्थित है।");return;}
        try {
            DownloadManager.Request req=new DownloadManager.Request(u);
            req.setTitle("Video Downloader"); req.setDescription("वीडियो सेव हो रहा है");
            req.setMimeType(ext.equals(".webm")?"video/webm":"video/mp4");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setAllowedOverMetered(true); req.setAllowedOverRoaming(false);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"MyVideo_"+System.currentTimeMillis()+ext);
            DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            if(dm==null){setStatus("Android Download सेवा उपलब्ध नहीं है।");return;}
            dm.enqueue(req); setStatus("डाउनलोड शुरू हुआ। पूरा होने पर Downloads फ़ोल्डर देखें।");
        } catch(Exception e){setStatus("डाउनलोड शुरू नहीं हुआ। लिंक जाँचकर दोबारा कोशिश करें।");}
    }

    private void setStatus(String s){if(status!=null)status.setText(s);}
    private void add(LinearLayout p,View v,int top){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(top);p.addView(v,lp);
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}
