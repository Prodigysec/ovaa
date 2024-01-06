package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.Appendable;
/* loaded from: classes.dex */
public class AppendableOutputStream<T extends Appendable> extends OutputStream {
    private final T appendable;

    public AppendableOutputStream(T appendable) {
        this.appendable = appendable;
    }

    @Override // java.io.OutputStream
    public void write(int b) throws IOException {
        this.appendable.append((char) b);
    }

    public T getAppendable() {
        return this.appendable;
    }
}
