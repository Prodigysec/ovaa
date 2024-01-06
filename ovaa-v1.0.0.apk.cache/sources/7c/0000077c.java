package oversecured.ovaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import oversecured.ovaa.R;
import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.FileUtils;
import oversecured.ovaa.utils.IntentUtils;
import oversecured.ovaa.utils.LoginUtils;
import oversecured.ovaa.utils.WeakCrypto;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_CODE = 1002;
    private static final int PICK_CODE = 1001;
    private LoginUtils loginUtils;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.loginUtils = LoginUtils.getInstance(this);
        findViewById(R.id.fileTheftButton).setOnClickListener(new View.OnClickListener() { // from class: oversecured.ovaa.activities.MainActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.checkPermissions();
                Intent pickerIntent = new Intent("android.intent.action.PICK");
                pickerIntent.setType("image/*");
                MainActivity.this.startActivityForResult(pickerIntent, 1001);
            }
        });
        findViewById(R.id.broadcastButton).setOnClickListener(new View.OnClickListener() { // from class: oversecured.ovaa.activities.MainActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent i = new Intent("oversecured.ovaa.action.UNPROTECTED_CREDENTIALS_DATA");
                i.putExtra("payload", MainActivity.this.loginUtils.getLoginData());
                MainActivity.this.sendBroadcast(i);
            }
        });
        findViewById(R.id.activityButton).setOnClickListener(new View.OnClickListener() { // from class: oversecured.ovaa.activities.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                LoginData loginData = MainActivity.this.loginUtils.getLoginData();
                String token = WeakCrypto.encrypt(loginData.toString());
                Intent i = new Intent("oversecured.ovaa.action.WEBVIEW");
                i.putExtra("url", "http://example.com./?token=" + token);
                IntentUtils.protectActivityIntent(MainActivity.this, i);
                MainActivity.this.startActivity(i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && data != null && requestCode == 1001) {
            FileUtils.copyToCache(this, data.getData());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkPermissions() {
        String[] permissions = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != 0) {
                ActivityCompat.requestPermissions(this, permissions, 1002);
            }
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1002) {
            for (int grantResult : grantResults) {
                if (grantResult != 0) {
                    checkPermissions();
                }
            }
        }
    }
}