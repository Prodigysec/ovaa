package oversecured.ovaa.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import oversecured.ovaa.utils.LoginUtils;

/* loaded from: classes.dex */
public class DeeplinkActivity extends AppCompatActivity {
    private static final int URI_GRANT_CODE = 1003;
    private LoginUtils loginUtils;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        Uri uri;
        super.onCreate(savedInstanceState);
        this.loginUtils = LoginUtils.getInstance(this);
        Intent intent = getIntent();
        if (intent != null && "android.intent.action.VIEW".equals(intent.getAction()) && (uri = intent.getData()) != null) {
            processDeeplink(uri);
        }
        finish();
    }

    private void processDeeplink(Uri uri) {
        String url;
        String host;
        if ("oversecured".equals(uri.getScheme()) && "ovaa".equals(uri.getHost())) {
            String path = uri.getPath();
            if ("/logout".equals(path)) {
                this.loginUtils.logout();
                startActivity(new Intent(this, EntranceActivity.class));
            } else if ("/login".equals(path)) {
                String url2 = uri.getQueryParameter("url");
                if (url2 != null) {
                    this.loginUtils.setLoginUrl(url2);
                }
                startActivity(new Intent(this, EntranceActivity.class));
            } else if ("/grant_uri_permissions".equals(path)) {
                Intent i = new Intent("oversecured.ovaa.action.GRANT_PERMISSIONS");
                if (getPackageManager().resolveActivity(i, 0) != null) {
                    startActivityForResult(i, 1003);
                }
            } else if ("/webview".equals(path) && (url = uri.getQueryParameter("url")) != null && (host = Uri.parse(url).getHost()) != null && host.endsWith("example.com")) {
                Intent i2 = new Intent(this, WebViewActivity.class);
                i2.putExtra("url", url);
                startActivity(i2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 1003) {
            setResult(resultCode, data);
        }
    }
}