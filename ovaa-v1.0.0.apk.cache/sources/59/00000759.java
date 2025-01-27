package org.apache.commons.io.output;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/* loaded from: classes.dex */
public class ProxyWriter extends FilterWriter {
    public ProxyWriter(Writer proxy) {
        super(proxy);
    }

    @Override // java.io.Writer, java.lang.Appendable
    public Writer append(char c) throws IOException {
        try {
            beforeWrite(1);
            this.out.append(c);
            afterWrite(1);
        } catch (IOException e) {
            handleIOException(e);
        }
        return this;
    }

    @Override // java.io.Writer, java.lang.Appendable
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        try {
            beforeWrite(end - start);
            this.out.append(csq, start, end);
            afterWrite(end - start);
        } catch (IOException e) {
            handleIOException(e);
        }
        return this;
    }

    @Override // java.io.Writer, java.lang.Appendable
    public Writer append(CharSequence csq) throws IOException {
        int len = 0;
        if (csq != null) {
            try {
                len = csq.length();
            } catch (IOException e) {
                handleIOException(e);
            }
        }
        beforeWrite(len);
        this.out.append(csq);
        afterWrite(len);
        return this;
    }

    @Override // java.io.FilterWriter, java.io.Writer
    public void write(int idx) throws IOException {
        try {
            beforeWrite(1);
            this.out.write(idx);
            afterWrite(1);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    @Override // java.io.Writer
    public void write(char[] chr) throws IOException {
        int len = 0;
        if (chr != null) {
            try {
                len = chr.length;
            } catch (IOException e) {
                handleIOException(e);
                return;
            }
        }
        beforeWrite(len);
        this.out.write(chr);
        afterWrite(len);
    }

    @Override // java.io.FilterWriter, java.io.Writer
    public void write(char[] chr, int st, int len) throws IOException {
        try {
            beforeWrite(len);
            this.out.write(chr, st, len);
            afterWrite(len);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    @Override // java.io.Writer
    public void write(String str) throws IOException {
        int len = 0;
        if (str != null) {
            try {
                len = str.length();
            } catch (IOException e) {
                handleIOException(e);
                return;
            }
        }
        beforeWrite(len);
        this.out.write(str);
        afterWrite(len);
    }

    @Override // java.io.FilterWriter, java.io.Writer
    public void write(String str, int st, int len) throws IOException {
        try {
            beforeWrite(len);
            this.out.write(str, st, len);
            afterWrite(len);
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    @Override // java.io.FilterWriter, java.io.Writer, java.io.Flushable
    public void flush() throws IOException {
        try {
            this.out.flush();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    @Override // java.io.FilterWriter, java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            this.out.close();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    protected void beforeWrite(int n) throws IOException {
    }

    protected void afterWrite(int n) throws IOException {
    }

    protected void handleIOException(IOException e) throws IOException {
        throw e;
    }
}