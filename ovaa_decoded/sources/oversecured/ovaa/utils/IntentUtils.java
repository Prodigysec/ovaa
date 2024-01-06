package oversecured.ovaa.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import java.util.Iterator;
/* loaded from: classes.dex */
public class IntentUtils {
    private IntentUtils() {
    }

    public static void protectActivityIntent(Context context, Intent intent) {
        Iterator<ResolveInfo> it = context.getPackageManager().queryIntentActivities(intent, 0).iterator();
        if (it.hasNext()) {
            ResolveInfo info = it.next();
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        }
    }
}
