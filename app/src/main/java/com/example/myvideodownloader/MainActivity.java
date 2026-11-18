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
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {
    private EditText urlInput;
    private EditText profileInput;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
        handleIncoming(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncoming(intent);
    }

    private void buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(24), dp(20), dp(24));
        page.setBackgroundColor(Color.rgb(246, 248, 252));

        TextView title = new TextView(this);
        title.setText("Video Downloader");
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.rgb(20, 45, 80));
        add(page, title, 0);

        TextView subtitle = new TextView(this);
        subtitle.setText("Facebook खोलें • Reel share करें • उपलब्ध वीडियो फ़ाइल सेव करें");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.rgb(80, 90, 105));
        add(page, subtitle, 8);

        TextView profileLabel = new TextView(this);
        profileLabel.setText("Facebook profile link");
        profileLabel.setTextSize(16);
        profileLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        profileLabel.setTextColor(Color.rgb(35, 55, 80));
        add(page, profileLabel, 22);

        profileInput = new EditText(this);
        profileInput.setSingleLine(true);
        profileInput.setTextSize(14);
        profileInput.setHint("https://www.facebook.com/username");
        profileInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        add(page, profileInput, 8);

        Button openProfile = new Button(this);
        openProfile.setText("Open Facebook profile");
        openProfile.setOnClickListener(v -> openFacebookProfile());
        add(page, openProfile, 8);

        Button openReels = new Button(this);
        openReels.setText("Open Facebook Reels");
        openReels.setOnClickListener(v -> openFacebookReels());
        add(page, openReels, 6);

        TextView howTo = new TextView(this);
        howTo.setText("Facebook में Reel खोलें और Share → इस ऐप को चुनें। लिंक अपने आप यहाँ आएगा। अगर Facebook केवल Reel का पेज लिंक देता है, तो उससे सीधे वीडियो फ़ाइल डाउनलोड नहीं होगी।");
        howTo.setTextSize(13);
        howTo.setTextColor(Color.rgb(65, 75, 90));
        add(page, howTo, 18);

        TextView downloadLabel = new TextView(this);
        downloadLabel.setText("Video link");
        downloadLabel.setTextSize(16);
        downloadLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        downloadLabel.setTextColor(Color.rgb(35, 55, 80));
        add(page, downloadLabel, 16);

        urlInput = new EditText(this);
        urlInput.setMinLines(2);
        urlInput.setTextSize(14);
        urlInput.setHint("Direct MP4 / M4V / WebM link");
        urlInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        add(page, urlInput, 8);

        Button paste = new Button(this);
        paste.setText("Paste video link");
        paste.setOnClickListener(v -> pasteFromClipboard());
        add(page, paste, 8);

        Button download = new Button(this);
        download.setText("Download video");
        download.setOnClickListener(v -> startDownload());
        add(page, download, 8);

        status = new TextView(this);
        status.setText("Ready. Shared links will appear above.");
        status.setTextSize(14);
        status.setTextColor(Color.rgb(35, 75, 110));
        add(page, status, 16);

        TextView note = new TextView(this);
        note.setText("जरूरी जानकारी: यह ऐप Facebook पासवर्ड नहीं माँगता। Facebook की हर Reel डाउनलोड के लिए उपलब्ध नहीं होती। यह संस्करण केवल ऐसे HTTPS direct video-file links सेव करता है जिन्हें डाउनलोड करने की आपको अनुमति है। यह private content या Facebook की सुरक्षा को bypass नहीं करता।");
        note.setTextSize(12);
        note.setTextColor(Color.rgb(105, 105, 115));
        add(page, note, 18);

        setContentView(page);
    }

    private void openFacebookProfile() {
        String raw = profileInput.getText().toString().trim();
        if (raw.isEmpty()) {
            setStatus("Pehle Facebook profile link paste karein.");
            return;
        }
        Uri uri;
        try { uri = Uri.parse(raw); } catch (Exception e) {
            setStatus("Profile link sahi nahi hai.");
            return;
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !(host.equals("facebook.com") || host.endsWith(".facebook.com"))) {
            setStatus("Sirf https://facebook.com ka profile link use karein.");
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            setStatus("Facebook profile browser/app mein khul raha hai. Reel ko Share karke is app mein bhejein.");
        } catch (Exception e) {
            setStatus("Facebook link open nahi ho saka.");
        }
    }

    private void openFacebookReels() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/reel/")));
            setStatus("Facebook Reels खुल रहा है। Reel को Share करके इस ऐप में भेजें।");
        } catch (Exception e) {
            setStatus("Facebook Reels नहीं खुल सका।");
        }
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence value = clip.getItemAt(0).coerceToText(this);
                if (value != null) {
                    urlInput.setText(value.toString().trim());
                    setStatus("Link pasted. Download availability depends on the link type.");
                    return;
                }
            }
        }
        setStatus("Clipboard is empty.");
    }

    private void handleIncoming(Intent intent) {
        if (intent == null || urlInput == null) return;
        String incoming = null;
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            incoming = intent.getStringExtra(Intent.EXTRA_TEXT);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            incoming = intent.getData().toString();
        }
        if (incoming != null) {
            String link = findHttpLink(incoming);
            if (link != null) {
                urlInput.setText(link);
                setStatus("Shared link received. If Facebook only shares a page link, direct downloading may not be available.");
            } else {
                setStatus("Shared text mein web link nahi mila.");
            }
        }
    }

    private String findHttpLink(String text) {
        String value = text.trim();
        int start = value.indexOf("https://");
        if (start < 0) start = value.indexOf("http://");
        if (start < 0) return null;
        String link = value.substring(start).split("\\s+")[0];
        return link.replaceAll("[),.]+$", "");
    }

    private void startDownload() {
        String raw = urlInput.getText().toString().trim();
        if (raw.isEmpty()) {
            setStatus("Pehle video link paste ya share karein.");
            return;
        }
        Uri uri;
        try { uri = Uri.parse(raw); } catch (Exception e) {
            setStatus("Video link valid nahi hai.");
            return;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            setStatus("Safety ke liye HTTPS link hi allowed hai.");
            return;
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (host.equals("facebook.com") || host.endsWith(".facebook.com")
                || host.equals("fb.watch") || host.endsWith(".fb.watch")) {
            setStatus("Yeh Facebook page/Reel share link hai, direct video file nahi. Is version mein Facebook link ko video file mein convert nahi kiya ja sakta.");
            return;
        }
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        String extension;
        if (path.endsWith(".mp4")) extension = ".mp4";
        else if (path.endsWith(".m4v")) extension = ".m4v";
        else if (path.endsWith(".webm")) extension = ".webm";
        else {
            setStatus("Is version mein direct MP4, M4V ya WebM file link hi supported hai.");
            return;
        }
        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("Video Downloader");
            request.setDescription("Saving a permitted video");
            request.setMimeType(extension.equals(".webm") ? "video/webm" : "video/mp4");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "MyVideo_" + System.currentTimeMillis() + extension);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) {
                setStatus("Android download service unavailable hai.");
                return;
            }
            manager.enqueue(request);
            setStatus("Download started. Downloads folder/notification check karein.");
        } catch (Exception e) {
            setStatus("Download start nahi hua. Link check karke try karein.");
        }
    }

    private void setStatus(String message) {
        if (status != null) status.setText(message);
    }

    private void add(LinearLayout parent, View child, int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(top);
        parent.addView(child, params);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
