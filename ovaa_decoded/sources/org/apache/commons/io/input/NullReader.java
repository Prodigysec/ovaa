package org.apache.commons.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
/* loaded from: classes.dex */
public class NullReader extends Reader {
    private boolean eof;
    private long mark;
    private final boolean markSupported;
    private long position;
    private long readlimit;
    private final long size;
    private final boolean throwEofException;

    public NullReader(long size) {
        this(size, true, false);
    }

    public NullReader(long size, boolean markSupported, boolean throwEofException) {
        this.mark = -1L;
        this.size = size;
        this.markSupported = markSupported;
        this.throwEofException = throwEofException;
    }

    public long getPosition() {
        return this.position;
    }

    public long getSize() {
        return this.size;
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.eof = false;
        this.position = 0L;
        this.mark = -1L;
    }

    @Override // java.io.Reader
    public synchronized void mark(int readlimit) {
        if (!this.markSupported) {
            throw new UnsupportedOperationException("Mark not supported");
        }
        this.mark = this.position;
        this.readlimit = readlimit;
    }

    @Override // java.io.Reader
    public boolean markSupported() {
        return this.markSupported;
    }

    @Override // java.io.Reader
    public int read() throws IOException {
        if (this.eof) {
            throw new IOException("Read after end of file");
        }
        long j = this.position;
        if (j == this.size) {
            return doEndOfFile();
        }
        this.position = j + 1;
        return processChar();
    }

    @Override // java.io.Reader
    public int read(char[] chars) throws IOException {
        return read(chars, 0, chars.length);
    }

    @Override // java.io.Reader
    public int read(char[] chars, int offset, int length) throws IOException {
        if (this.eof) {
            throw new IOException("Read after end of file");
        }
        long j = this.position;
        long j2 = this.size;
        if (j == j2) {
            return doEndOfFile();
        }
        long j3 = j + length;
        this.position = j3;
        int returnLength = length;
        if (j3 > j2) {
            returnLength = length - ((int) (j3 - j2));
            this.position = j2;
        }
        processChars(chars, offset, returnLength);
        return returnLength;
    }

    @Override // java.io.Reader
    public synchronized void reset() throws IOException {
        if (!this.markSupported) {
            throw new UnsupportedOperationException("Mark not supported");
        }
        if (this.mark < 0) {
            throw new IOException("No position has been marked");
        }
        if (this.position > this.mark + this.readlimit) {
            throw new IOException("Marked position [" + this.mark + "] is no longer valid - passed the read limit [" + this.readlimit + "]");
        }
        this.position = this.mark;
        this.eof = false;
    }

    @Override // java.io.Reader
    public long skip(long numberOfChars) throws IOException {
        if (this.eof) {
            throw new IOException("Skip after end of file");
        }
        long j = this.position;
        long j2 = this.size;
        if (j == j2) {
            return doEndOfFile();
        }
        long j3 = j + numberOfChars;
        this.position = j3;
        if (j3 <= j2) {
            return numberOfChars;
        }
        long returnLength = numberOfChars - (j3 - j2);
        this.position = j2;
        return returnLength;
    }

    protected int processChar() {
        return 0;
    }

    protected void processChars(char[] chars, int offset, int length) {
    }

    private int doEndOfFile() throws EOFException {
        this.eof = true;
        if (this.throwEofException) {
            throw new EOFException();
        }
        return -1;
    }
}
