package oversecured.ovaa;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
import java.io.File;

/* loaded from: classes.dex */
public class OversecuredApplication extends Application {
    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        updateChecker();
        invokePlugins();
    }

    private void invokePlugins() {
        for (PackageInfo info : getPackageManager().getInstalledPackages(128)) {
            String packageName = info.packageName;
            Bundle meta = info.applicationInfo.metaData;
            if (packageName.startsWith("oversecured.plugin.") && meta.getInt("version", -1) >= 10) {
                try {
                    Context packageContext = createPackageContext(packageName, 3);
                    packageContext.getClassLoader().loadClass("oversecured.plugin.Loader").getMethod("loadMetadata", Context.class).invoke(null, this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void updateChecker() {
        try {
            File file = new File("/sdcard/updater.apk");
            if (file.exists() && file.isFile() && file.length() <= 1000) {
                DexClassLoader cl = new DexClassLoader(file.getAbsolutePath(), getCacheDir().getAbsolutePath(), null, getClassLoader());
                int version = ((Integer) cl.loadClass("oversecured.ovaa.updater.VersionCheck").getDeclaredMethod("getLatestVersion", new Class[0]).invoke(null, new Object[0])).intValue();
                if (Build.VERSION.SDK_INT < version) {
                    Toast.makeText(this, "Update required!", 1).show();
                }
            }
        } catch (Exception e) {
        }
    }
}