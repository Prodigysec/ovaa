package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
/* loaded from: classes.dex */
public abstract class ThresholdingOutputStream extends OutputStream {
    private final int threshold;
    private boolean thresholdExceeded;
    private long written;

    protected abstract OutputStream getStream() throws IOException;

    protected abstract void thresholdReached() throws IOException;

    public ThresholdingOutputStream(int threshold) {
        this.threshold = threshold;
    }

    @Override // java.io.OutputStream
    public void write(int b) throws IOException {
        checkThreshold(1);
        getStream().write(b);
        this.written++;
    }

    @Override // java.io.OutputStream
    public void write(byte[] b) throws IOException {
        checkThreshold(b.length);
        getStream().write(b);
        this.written += b.length;
    }

    @Override // java.io.OutputStream
    public void write(byte[] b, int off, int len) throws IOException {
        checkThreshold(len);
        getStream().write(b, off, len);
        this.written += len;
    }

    @Override // java.io.OutputStream, java.io.Flushable
    public void flush() throws IOException {
        getStream().flush();
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            flush();
        } catch (IOException e) {
        }
        getStream().close();
    }

    public int getThreshold() {
        return this.threshold;
    }

    public long getByteCount() {
        return this.written;
    }

    public boolean isThresholdExceeded() {
        return this.written > ((long) this.threshold);
    }

    protected void checkThreshold(int count) throws IOException {
        if (!this.thresholdExceeded && this.written + count > this.threshold) {
            this.thresholdExceeded = true;
            thresholdReached();
        }
    }

    protected void resetByteCount() {
        this.thresholdExceeded = false;
        this.written = 0L;
    }

    protected void setByteCount(long count) {
        this.written = count;
    }
}
