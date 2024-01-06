package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;

/* loaded from: classes.dex */
public class MagicNumberFileFilter extends AbstractFileFilter implements Serializable {
    private static final long serialVersionUID = -547733176983104172L;
    private final long byteOffset;
    private final byte[] magicNumbers;

    public MagicNumberFileFilter(byte[] magicNumber) {
        this(magicNumber, 0L);
    }

    public MagicNumberFileFilter(String magicNumber) {
        this(magicNumber, 0L);
    }

    public MagicNumberFileFilter(String magicNumber, long offset) {
        if (magicNumber == null) {
            throw new IllegalArgumentException("The magic number cannot be null");
        }
        if (magicNumber.isEmpty()) {
            throw new IllegalArgumentException("The magic number must contain at least one byte");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("The offset cannot be negative");
        }
        this.magicNumbers = magicNumber.getBytes(Charset.defaultCharset());
        this.byteOffset = offset;
    }

    public MagicNumberFileFilter(byte[] magicNumber, long offset) {
        if (magicNumber == null) {
            throw new IllegalArgumentException("The magic number cannot be null");
        }
        if (magicNumber.length == 0) {
            throw new IllegalArgumentException("The magic number must contain at least one byte");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("The offset cannot be negative");
        }
        byte[] bArr = new byte[magicNumber.length];
        this.magicNumbers = bArr;
        System.arraycopy(magicNumber, 0, bArr, 0, magicNumber.length);
        this.byteOffset = offset;
    }

    @Override // org.apache.commons.io.filefilter.AbstractFileFilter, org.apache.commons.io.filefilter.IOFileFilter, java.io.FileFilter
    public boolean accept(File file) {
        if (file != null && file.isFile() && file.canRead()) {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                byte[] fileBytes = new byte[this.magicNumbers.length];
                randomAccessFile.seek(this.byteOffset);
                int read = randomAccessFile.read(fileBytes);
                if (read == this.magicNumbers.length) {
                    boolean equals = Arrays.equals(this.magicNumbers, fileBytes);
                    randomAccessFile.close();
                    return equals;
                }
                randomAccessFile.close();
                return false;
            } catch (IOException e) {
            }
        }
        return false;
    }

    @Override // org.apache.commons.io.filefilter.AbstractFileFilter
    public String toString() {
        return super.toString() + "(" + new String(this.magicNumbers, Charset.defaultCharset()) + "," + this.byteOffset + ")";
    }
}