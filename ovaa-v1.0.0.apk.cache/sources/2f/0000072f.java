package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.io.input.ObservableInputStream;

/* loaded from: classes.dex */
public class MessageDigestCalculatingInputStream extends ObservableInputStream {
    private final MessageDigest messageDigest;

    /* loaded from: classes.dex */
    public static class MessageDigestMaintainingObserver extends ObservableInputStream.Observer {
        private final MessageDigest md;

        public MessageDigestMaintainingObserver(MessageDigest pMd) {
            this.md = pMd;
        }

        @Override // org.apache.commons.io.input.ObservableInputStream.Observer
        void data(int pByte) throws IOException {
            this.md.update((byte) pByte);
        }

        @Override // org.apache.commons.io.input.ObservableInputStream.Observer
        void data(byte[] pBuffer, int pOffset, int pLength) throws IOException {
            this.md.update(pBuffer, pOffset, pLength);
        }
    }

    public MessageDigestCalculatingInputStream(InputStream pStream, MessageDigest pDigest) {
        super(pStream);
        this.messageDigest = pDigest;
        add(new MessageDigestMaintainingObserver(pDigest));
    }

    public MessageDigestCalculatingInputStream(InputStream pStream, String pAlgorithm) throws NoSuchAlgorithmException {
        this(pStream, MessageDigest.getInstance(pAlgorithm));
    }

    public MessageDigestCalculatingInputStream(InputStream pStream) throws NoSuchAlgorithmException {
        this(pStream, MessageDigest.getInstance("MD5"));
    }

    public MessageDigest getMessageDigest() {
        return this.messageDigest;
    }
}