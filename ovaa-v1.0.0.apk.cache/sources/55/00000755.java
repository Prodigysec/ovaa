package org.apache.commons.io.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

/* loaded from: classes.dex */
public class LockableFileWriter extends Writer {
    private static final String LCK = ".lck";
    private final File lockFile;
    private final Writer out;

    public LockableFileWriter(String fileName) throws IOException {
        this(fileName, false, (String) null);
    }

    public LockableFileWriter(String fileName, boolean append) throws IOException {
        this(fileName, append, (String) null);
    }

    public LockableFileWriter(String fileName, boolean append, String lockDir) throws IOException {
        this(new File(fileName), append, lockDir);
    }

    public LockableFileWriter(File file) throws IOException {
        this(file, false, (String) null);
    }

    public LockableFileWriter(File file, boolean append) throws IOException {
        this(file, append, (String) null);
    }

    @Deprecated
    public LockableFileWriter(File file, boolean append, String lockDir) throws IOException {
        this(file, Charset.defaultCharset(), append, lockDir);
    }

    public LockableFileWriter(File file, Charset encoding) throws IOException {
        this(file, encoding, false, (String) null);
    }

    public LockableFileWriter(File file, String encoding) throws IOException {
        this(file, encoding, false, (String) null);
    }

    public LockableFileWriter(File file, Charset encoding, boolean append, String lockDir) throws IOException {
        File file2 = file.getAbsoluteFile();
        if (file2.getParentFile() != null) {
            FileUtils.forceMkdir(file2.getParentFile());
        }
        if (file2.isDirectory()) {
            throw new IOException("File specified is a directory");
        }
        File lockDirFile = new File(lockDir == null ? System.getProperty("java.io.tmpdir") : lockDir);
        FileUtils.forceMkdir(lockDirFile);
        testLockDir(lockDirFile);
        this.lockFile = new File(lockDirFile, file2.getName() + LCK);
        createLock();
        this.out = initWriter(file2, encoding, append);
    }

    public LockableFileWriter(File file, String encoding, boolean append, String lockDir) throws IOException {
        this(file, Charsets.toCharset(encoding), append, lockDir);
    }

    private void testLockDir(File lockDir) throws IOException {
        if (!lockDir.exists()) {
            throw new IOException("Could not find lockDir: " + lockDir.getAbsolutePath());
        } else if (!lockDir.canWrite()) {
            throw new IOException("Could not write to lockDir: " + lockDir.getAbsolutePath());
        }
    }

    private void createLock() throws IOException {
        synchronized (LockableFileWriter.class) {
            if (!this.lockFile.createNewFile()) {
                throw new IOException("Can't write file, lock " + this.lockFile.getAbsolutePath() + " exists");
            }
            this.lockFile.deleteOnExit();
        }
    }

    private Writer initWriter(File file, Charset encoding, boolean append) throws IOException {
        boolean fileExistedAlready = file.exists();
        try {
            return new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(), append), Charsets.toCharset(encoding));
        } catch (IOException | RuntimeException ex) {
            FileUtils.deleteQuietly(this.lockFile);
            if (!fileExistedAlready) {
                FileUtils.deleteQuietly(file);
            }
            throw ex;
        }
    }

    @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        try {
            this.out.close();
        } finally {
            this.lockFile.delete();
        }
    }

    @Override // java.io.Writer
    public void write(int idx) throws IOException {
        this.out.write(idx);
    }

    @Override // java.io.Writer
    public void write(char[] chr) throws IOException {
        this.out.write(chr);
    }

    @Override // java.io.Writer
    public void write(char[] chr, int st, int end) throws IOException {
        this.out.write(chr, st, end);
    }

    @Override // java.io.Writer
    public void write(String str) throws IOException {
        this.out.write(str);
    }

    @Override // java.io.Writer
    public void write(String str, int st, int end) throws IOException {
        this.out.write(str, st, end);
    }

    @Override // java.io.Writer, java.io.Flushable
    public void flush() throws IOException {
        this.out.flush();
    }
}