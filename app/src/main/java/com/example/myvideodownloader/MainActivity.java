package com.example.myvideodownloader;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private EditText urlInput;
    private TextView status, accountInfo;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = CallbackManager.Factory.create();
        buildScreen();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override public void onSuccess(LoginResult result) { loadFacebookProfile(); }
            @Override public void onCancel() { status.setText("Facebook login cancelled."); }
            @Override public void onError(FacebookException error) {
                status.setText("Facebook login error: " + error.getMessage());
            }
        });

        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token != null && !token.isExpired()) loadFacebookProfile();
        handleIncoming(getIntent());
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncoming(intent);
    }

    private void buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(22));
        page.setBackgroundColor(Color.rgb(247, 249, 252));

        TextView title = new TextView(this);
        title.setText("My Video Downloader");
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.rgb(20, 45, 80));
        page.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Facebook Videos & Reels  •  No Ads");
        sub.setTextSize(14);
        sub.setTextColor(Color.DKGRAY);
        add(page, sub, 8);

        Button login = new Button(this);
        login.setText("Continue with Facebook");
        login.setOnClickListener(v -> LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile")));
        add(page, login, 18);

        accountInfo = new TextView(this);
        accountInfo.setText("Facebook account: Not connected");
        accountInfo.setTextSize(14);
        accountInfo.setTextColor(Color.rgb(35, 75, 120));
        add(page, accountInfo, 12);

        TextView explanation = new TextView(this);
        explanation.setText("Facebook Login does not grant access to every profile's Reels. Meta only provides approved data through supported APIs. This app does not scrape Facebook or bypass private-video, login, DRM, or download restrictions.");
        explanation.setTextSize(13);
        explanation.setTextColor(Color.rgb(75, 80, 90));
        add(page, explanation, 16);

        TextView heading = new TextView(this);
        heading.setText("Download a permitted direct video link");
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setTextSize(16);
        heading.setTextColor(Color.rgb(20, 45, 80));
        add(page, heading, 18);

        urlInput = new EditText(this);
        urlInput.setMinLines(2);
        urlInput.setHint("Share a creator-provided direct MP4 / WebM URL");
        add(page, urlInput, 8);

        Button download = new Button(this);
        download.setText("⬇  Download");
        download.setOnClickListener(v -> startDownload());
        add(page, download, 8);

        status = new TextView(this);
        status.setText("Ready. Sign in or share a permitted direct video link to this app.");
        status.setTextSize(13);
        status.setTextColor(Color.rgb(70, 80, 95));
        add(page, status, 12);

        Button logout = new Button(this);
        logout.setText("Log out of Facebook");
        logout.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();
            accountInfo.setText("Facebook account: Not connected");
            status.setText("Logged out.");
        });
        add(page, logout, 8);

        setContentView(page);
    }

    private void loadFacebookProfile() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null) return;
        GraphRequest request = GraphRequest.newMeRequest(token, (JSONObject object, GraphResponse response) -> {
            if (response.getError() != null) {
                status.setText("Connected, but profile details could not be loaded.");
                return;
            }
            accountInfo.setText("Facebook account: " + object.optString("name", "Connected") + " (connected)");
            status.setText("Facebook login successful. Reels access depends on Meta API permissions.");
        });
        Bundle params = new Bundle();
        params.putString("fields", "id,name");
        request.setParameters(params);
        request.executeAsync();
    }

    private void handleIncoming(Intent intent) {
        if (intent == null || urlInput == null) return;
        String incoming = null;
        if (Intent.ACTION_SEND.equals(intent.getAction())) incoming = intent.getStringExtra(Intent.EXTRA_TEXT);
        else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null)
            incoming = intent.getData().toString();
        if (incoming != null) {
            int start = incoming.indexOf("http");
            String url = start >= 0 ? incoming.substring(start).split("\\s")[0] : incoming.trim();
            urlInput.setText(url.replaceAll("[),.]+$", ""));
            status.setText("Link received. Only direct, permitted video files can be downloaded.");
        }
    }

    private void startDownload() {
        String raw = urlInput.getText().toString().trim();
        if (raw.isEmpty()) { status.setText("Share a permitted direct video URL first."); return; }
        Uri uri = Uri.parse(raw);
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
            status.setText("Please enter a valid HTTP or HTTPS link."); return;
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        if (host.contains("facebook.com") || host.contains("fb.watch")) {
            status.setText("This is a Facebook page/share link, not a direct video file. Facebook does not expose every Reel as a downloadable file.");
            return;
        }
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase();
        if (!(path.endsWith(".mp4") || path.endsWith(".m4v") || path.endsWith(".webm"))) {
            status.setText("This build accepts direct MP4, M4V, or WebM URLs only."); return;
        }
        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("My Video Downloader");
            request.setDescription("Saving permitted video");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String ext = path.endsWith(".webm") ? ".webm" : path.endsWith(".m4v") ? ".m4v" : ".mp4";
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                "MyVideo_" + System.currentTimeMillis() + ext);
            ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
            status.setText("Download started. Check your Downloads folder.");
        } catch (Exception e) {
            status.setText("Could not start download. Check the video link.");
        }
    }

    private void add(LinearLayout parent, View child, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(top);
        parent.addView(child, p);
    }
    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
