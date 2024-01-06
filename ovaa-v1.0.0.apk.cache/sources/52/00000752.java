package org.apache.commons.io.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/* loaded from: classes.dex */
public class DeferredFileOutputStream extends ThresholdingOutputStream {
    private boolean closed;
    private OutputStream currentOutputStream;
    private final File directory;
    private ByteArrayOutputStream memoryOutputStream;
    private File outputFile;
    private final String prefix;
    private final String suffix;

    public DeferredFileOutputStream(int threshold, File outputFile) {
        this(threshold, outputFile, null, null, null, 1024);
    }

    public DeferredFileOutputStream(int threshold, int initialBufferSize, File outputFile) {
        this(threshold, outputFile, null, null, null, initialBufferSize);
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be atleast 0.");
        }
    }

    public DeferredFileOutputStream(int threshold, String prefix, String suffix, File directory) {
        this(threshold, null, prefix, suffix, directory, 1024);
        if (prefix == null) {
            throw new IllegalArgumentException("Temporary file prefix is missing");
        }
    }

    public DeferredFileOutputStream(int threshold, int initialBufferSize, String prefix, String suffix, File directory) {
        this(threshold, null, prefix, suffix, directory, initialBufferSize);
        if (prefix == null) {
            throw new IllegalArgumentException("Temporary file prefix is missing");
        }
        if (initialBufferSize < 0) {
            throw new IllegalArgumentException("Initial buffer size must be atleast 0.");
        }
    }

    private DeferredFileOutputStream(int threshold, File outputFile, String prefix, String suffix, File directory, int initialBufferSize) {
        super(threshold);
        this.closed = false;
        this.outputFile = outputFile;
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(initialBufferSize);
        this.memoryOutputStream = byteArrayOutputStream;
        this.currentOutputStream = byteArrayOutputStream;
    }

    @Override // org.apache.commons.io.output.ThresholdingOutputStream
    protected OutputStream getStream() throws IOException {
        return this.currentOutputStream;
    }

    @Override // org.apache.commons.io.output.ThresholdingOutputStream
    protected void thresholdReached() throws IOException {
        String str = this.prefix;
        if (str != null) {
            this.outputFile = File.createTempFile(str, this.suffix, this.directory);
        }
        FileUtils.forceMkdirParent(this.outputFile);
        FileOutputStream fos = new FileOutputStream(this.outputFile);
        try {
            this.memoryOutputStream.writeTo(fos);
            this.currentOutputStream = fos;
            this.memoryOutputStream = null;
        } catch (IOException e) {
            fos.close();
            throw e;
        }
    }

    public boolean isInMemory() {
        return !isThresholdExceeded();
    }

    public byte[] getData() {
        ByteArrayOutputStream byteArrayOutputStream = this.memoryOutputStream;
        if (byteArrayOutputStream != null) {
            return byteArrayOutputStream.toByteArray();
        }
        return null;
    }

    public File getFile() {
        return this.outputFile;
    }

    @Override // org.apache.commons.io.output.ThresholdingOutputStream, java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        super.close();
        this.closed = true;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (!this.closed) {
            throw new IOException("Stream not closed");
        }
        if (isInMemory()) {
            this.memoryOutputStream.writeTo(out);
            return;
        }
        FileInputStream fis = new FileInputStream(this.outputFile);
        try {
            IOUtils.copy(fis, out);
            fis.close();
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    fis.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }
}