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
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        page.setBackgroundColor(Color.rgb(246, 248, 252));

        TextView title = new TextView(this);
        title.setText("My Video Downloader");
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.rgb(20, 45, 80));
        add(page, title, 0);

        TextView subtitle = new TextView(this);
        subtitle.setText("Simple video saving • No account • No ads");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.rgb(80, 90, 105));
        add(page, subtitle, 8);

        TextView info = new TextView(this);
        info.setText("Paste a direct video-file link or use Share from a website. You can save videos that you own or have permission to download.");
        info.setTextSize(14);
        info.setTextColor(Color.rgb(55, 65, 80));
        add(page, info, 22);

        urlInput = new EditText(this);
        urlInput.setMinLines(2);
        urlInput.setTextSize(15);
        urlInput.setHint("Paste direct MP4 / M4V / WebM link");
        urlInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        add(page, urlInput, 16);

        Button paste = new Button(this);
        paste.setText("Paste link");
        paste.setOnClickListener(v -> pasteFromClipboard());
        add(page, paste, 10);

        Button download = new Button(this);
        download.setText("Download video");
        download.setOnClickListener(v -> startDownload());
        add(page, download, 8);

        Button clear = new Button(this);
        clear.setText("Clear");
        clear.setOnClickListener(v -> {
            urlInput.setText("");
            setStatus("Link cleared.");
        });
        add(page, clear, 4);

        status = new TextView(this);
        status.setText("Ready. Downloads are saved in your phone's Downloads folder.");
        status.setTextSize(14);
        status.setTextColor(Color.rgb(45, 75, 110));
        add(page, status, 18);

        TextView note = new TextView(this);
        note.setText("Facebook note: a Facebook Reel/share link is not a direct video file. This app does not sign in to Facebook, scrape pages, or bypass private-video or platform restrictions. Facebook may not provide a downloadable file for every Reel.");
        note.setTextSize(12);
        note.setTextColor(Color.rgb(105, 105, 115));
        add(page, note, 24);

        setContentView(page);
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence value = clip.getItemAt(0).coerceToText(this);
                if (value != null) {
                    urlInput.setText(value.toString().trim());
                    setStatus("Link pasted. Tap Download video to continue.");
                    return;
                }
            }
        }
        setStatus("Clipboard is empty. Copy a permitted direct video link first.");
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
                setStatus("Shared link received. Check whether it is a direct video-file URL.");
            } else {
                setStatus("No web link found in the shared text.");
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
            setStatus("Paste or share a direct video-file link first.");
            return;
        }

        Uri uri;
        try {
            uri = Uri.parse(raw);
        } catch (Exception e) {
            setStatus("That link is not valid.");
            return;
        }

        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme)) {
            setStatus("For safety, this app accepts HTTPS links only.");
            return;
        }

        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (host.equals("facebook.com") || host.endsWith(".facebook.com")
                || host.equals("fb.watch") || host.endsWith(".fb.watch")) {
            setStatus("This is a Facebook page/Reel link, not a direct video file. The app cannot turn every Facebook share link into a downloadable file.");
            return;
        }

        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
        String extension;
        if (path.endsWith(".mp4")) extension = ".mp4";
        else if (path.endsWith(".m4v")) extension = ".m4v";
        else if (path.endsWith(".webm")) extension = ".webm";
        else {
            setStatus("Only direct MP4, M4V, or WebM file links are supported in this version.");
            return;
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("My Video Downloader");
            request.setDescription("Saving a permitted video");
            request.setMimeType(extension.equals(".webm") ? "video/webm" : "video/mp4");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "MyVideo_" + System.currentTimeMillis() + extension
            );
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) {
                setStatus("Android download service is unavailable on this phone.");
                return;
            }
            manager.enqueue(request);
            setStatus("Download started. Check the Downloads folder and notification.");
        } catch (Exception e) {
            setStatus("Download could not start. Check the link and try again.");
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