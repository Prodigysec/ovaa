package oversecured.ovaa.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import oversecured.ovaa.R;
import oversecured.ovaa.objects.LoginData;
/* loaded from: classes.dex */
public class LoginUtils {
    private static final String EMAIL_KEY = "email";
    private static final String LOGIN_URL_KEY = "login_url";
    private static final String PASSWORD_KEY = "password";
    private static LoginUtils utils;
    private Context context;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    private LoginUtils(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("login_data", 0);
        this.preferences = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    public static LoginUtils getInstance(Context context) {
        if (utils == null) {
            utils = new LoginUtils(context);
        }
        return utils;
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(this.preferences.getString("email", null));
    }

    public void saveCredentials(LoginData loginData) {
        this.editor.putString("email", loginData.email).putString(PASSWORD_KEY, loginData.password).commit();
    }

    public LoginData getLoginData() {
        return new LoginData(this.preferences.getString("email", null), this.preferences.getString(PASSWORD_KEY, null));
    }

    public void setLoginUrl(String url) {
        this.editor.putString(LOGIN_URL_KEY, url).commit();
    }

    public String getLoginUrl() {
        String url = this.preferences.getString(LOGIN_URL_KEY, null);
        if (TextUtils.isEmpty(url)) {
            String url2 = this.context.getString(R.string.login_url);
            this.editor.putString(LOGIN_URL_KEY, url2).commit();
            return url2;
        }
        return url;
    }

    public void logout() {
        this.editor.clear().commit();
    }
}
