package oversecured.ovaa.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import oversecured.ovaa.objects.LoginData;
import oversecured.ovaa.utils.LoginUtils;

/* loaded from: classes.dex */
public class CredentialsProvider extends ContentProvider {
    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LoginData loginData = LoginUtils.getInstance(getContext()).getLoginData();
        MatrixCursor cursor = new MatrixCursor(new String[]{NotificationCompat.CATEGORY_EMAIL, "password"});
        cursor.addRow(new String[]{loginData.email, loginData.password});
        return cursor;
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}