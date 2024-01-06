package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ObservableInputStream extends ProxyInputStream {
    private final List<Observer> observers;

    /* loaded from: classes.dex */
    public static abstract class Observer {
        void data(int pByte) throws IOException {
        }

        void data(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        }

        void finished() throws IOException {
        }

        void closed() throws IOException {
        }

        void error(IOException pException) throws IOException {
            throw pException;
        }
    }

    public ObservableInputStream(InputStream pProxy) {
        super(pProxy);
        this.observers = new ArrayList();
    }

    public void add(Observer pObserver) {
        this.observers.add(pObserver);
    }

    public void remove(Observer pObserver) {
        this.observers.remove(pObserver);
    }

    public void removeAllObservers() {
        this.observers.clear();
    }

    @Override // org.apache.commons.io.input.ProxyInputStream, java.io.FilterInputStream, java.io.InputStream
    public int read() throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read();
        } catch (IOException pException) {
            ioe = pException;
        }
        if (ioe != null) {
            noteError(ioe);
        } else if (result == -1) {
            noteFinished();
        } else {
            noteDataByte(result);
        }
        return result;
    }

    @Override // org.apache.commons.io.input.ProxyInputStream, java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] pBuffer) throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read(pBuffer);
        } catch (IOException pException) {
            ioe = pException;
        }
        if (ioe != null) {
            noteError(ioe);
        } else if (result == -1) {
            noteFinished();
        } else if (result > 0) {
            noteDataBytes(pBuffer, 0, result);
        }
        return result;
    }

    @Override // org.apache.commons.io.input.ProxyInputStream, java.io.FilterInputStream, java.io.InputStream
    public int read(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        int result = 0;
        IOException ioe = null;
        try {
            result = super.read(pBuffer, pOffset, pLength);
        } catch (IOException pException) {
            ioe = pException;
        }
        if (ioe != null) {
            noteError(ioe);
        } else if (result == -1) {
            noteFinished();
        } else if (result > 0) {
            noteDataBytes(pBuffer, pOffset, result);
        }
        return result;
    }

    protected void noteDataBytes(byte[] pBuffer, int pOffset, int pLength) throws IOException {
        for (Observer observer : getObservers()) {
            observer.data(pBuffer, pOffset, pLength);
        }
    }

    protected void noteFinished() throws IOException {
        for (Observer observer : getObservers()) {
            observer.finished();
        }
    }

    protected void noteDataByte(int pDataByte) throws IOException {
        for (Observer observer : getObservers()) {
            observer.data(pDataByte);
        }
    }

    protected void noteError(IOException pException) throws IOException {
        for (Observer observer : getObservers()) {
            observer.error(pException);
        }
    }

    protected void noteClosed() throws IOException {
        for (Observer observer : getObservers()) {
            observer.closed();
        }
    }

    protected List<Observer> getObservers() {
        return this.observers;
    }

    @Override // org.apache.commons.io.input.ProxyInputStream, java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        IOException ioe = null;
        try {
            super.close();
        } catch (IOException e) {
            ioe = e;
        }
        if (ioe == null) {
            noteClosed();
        } else {
            noteError(ioe);
        }
    }

    public void consume() throws IOException {
        int res;
        byte[] buffer = new byte[8192];
        do {
            res = read(buffer);
        } while (res != -1);
    }
}