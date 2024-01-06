package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes.dex */
public class WindowsLineEndingInputStream extends InputStream {
    private final boolean ensureLineFeedAtEndOfFile;
    private final InputStream target;
    private boolean slashRSeen = false;
    private boolean slashNSeen = false;
    private boolean injectSlashN = false;
    private boolean eofSeen = false;

    public WindowsLineEndingInputStream(InputStream in, boolean ensureLineFeedAtEndOfFile) {
        this.target = in;
        this.ensureLineFeedAtEndOfFile = ensureLineFeedAtEndOfFile;
    }

    private int readWithUpdate() throws IOException {
        int target = this.target.read();
        boolean z = target == -1;
        this.eofSeen = z;
        if (z) {
            return target;
        }
        this.slashRSeen = target == 13;
        this.slashNSeen = target == 10;
        return target;
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        if (this.eofSeen) {
            return eofGame();
        }
        if (this.injectSlashN) {
            this.injectSlashN = false;
            return 10;
        }
        boolean prevWasSlashR = this.slashRSeen;
        int target = readWithUpdate();
        if (this.eofSeen) {
            return eofGame();
        }
        if (target == 10 && !prevWasSlashR) {
            this.injectSlashN = true;
            return 13;
        }
        return target;
    }

    private int eofGame() {
        if (this.ensureLineFeedAtEndOfFile) {
            if (!this.slashNSeen && !this.slashRSeen) {
                this.slashRSeen = true;
                return 13;
            } else if (this.slashNSeen) {
                return -1;
            } else {
                this.slashRSeen = false;
                this.slashNSeen = true;
                return 10;
            }
        }
        return -1;
    }

    @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        super.close();
        this.target.close();
    }

    @Override // java.io.InputStream
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException("Mark not supported");
    }
}