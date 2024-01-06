package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;
/* loaded from: classes.dex */
public class BoundedReader extends Reader {
    private static final int INVALID = -1;
    private int charsRead = 0;
    private int markedAt = -1;
    private final int maxCharsFromTargetReader;
    private int readAheadLimit;
    private final Reader target;

    public BoundedReader(Reader target, int maxCharsFromTargetReader) throws IOException {
        this.target = target;
        this.maxCharsFromTargetReader = maxCharsFromTargetReader;
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.target.close();
    }

    @Override // java.io.Reader
    public void reset() throws IOException {
        this.charsRead = this.markedAt;
        this.target.reset();
    }

    @Override // java.io.Reader
    public void mark(int readAheadLimit) throws IOException {
        int i = this.charsRead;
        this.readAheadLimit = readAheadLimit - i;
        this.markedAt = i;
        this.target.mark(readAheadLimit);
    }

    @Override // java.io.Reader
    public int read() throws IOException {
        int i = this.charsRead;
        if (i >= this.maxCharsFromTargetReader) {
            return -1;
        }
        int i2 = this.markedAt;
        if (i2 < 0 || i - i2 < this.readAheadLimit) {
            this.charsRead++;
            return this.target.read();
        }
        return -1;
    }

    @Override // java.io.Reader
    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int c = read();
            if (c == -1) {
                if (i == 0) {
                    return -1;
                }
                return i;
            }
            cbuf[off + i] = (char) c;
        }
        return len;
    }
}
