package oversecured.ovaa.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import oversecured.ovaa.utils.LoginUtils;
/* loaded from: classes.dex */
public class EntranceActivity extends AppCompatActivity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LoginUtils.getInstance(this).isLoggedIn()) {
            startActivity(new Intent("oversecured.ovaa.action.ACTIVITY_MAIN"));
        } else {
            startActivity(new Intent("oversecured.ovaa.action.LOGIN"));
        }
        finish();
    }
}
