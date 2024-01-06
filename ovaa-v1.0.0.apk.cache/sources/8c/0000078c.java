package oversecured.ovaa.utils;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;

/* loaded from: classes.dex */
public class FileUtils {
    private FileUtils() {
    }

    public static void deleteRecursive(File file) {
        File[] listFiles;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }

    public static File copyToCache(Context context, Uri uri) {
        try {
            File externalCacheDir = context.getExternalCacheDir();
            File out = new File(externalCacheDir, "" + System.currentTimeMillis());
            InputStream i = context.getContentResolver().openInputStream(uri);
            OutputStream o = new FileOutputStream(out);
            IOUtils.copy(i, o);
            i.close();
            o.close();
            return out;
        } catch (Exception e) {
            return null;
        }
    }
}