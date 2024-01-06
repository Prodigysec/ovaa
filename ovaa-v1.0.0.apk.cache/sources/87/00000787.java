package oversecured.ovaa.objects;

import java.io.Serializable;

/* loaded from: classes.dex */
public class MemoryCorruptionSerializable implements Serializable {
    private static final long serialVersionUID = 0;
    private long ptr;

    private native void freePtr(long j);

    static {
        System.loadLibrary("ovaa");
    }

    protected void finalize() throws Throwable {
        long j = this.ptr;
        if (j != 0) {
            freePtr(j);
            this.ptr = 0L;
        }
    }
}