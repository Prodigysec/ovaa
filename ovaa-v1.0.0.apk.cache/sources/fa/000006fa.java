package org.apache.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.io.output.StringBuilderWriter;

/* loaded from: classes.dex */
public class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final char DIR_SEPARATOR = File.separatorChar;
    public static final char DIR_SEPARATOR_UNIX = '/';
    public static final char DIR_SEPARATOR_WINDOWS = '\\';
    public static final int EOF = -1;
    public static final String LINE_SEPARATOR;
    public static final String LINE_SEPARATOR_UNIX = "\n";
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
    private static final int SKIP_BUFFER_SIZE = 2048;
    private static byte[] SKIP_BYTE_BUFFER;
    private static char[] SKIP_CHAR_BUFFER;

    static {
        StringBuilderWriter buf = new StringBuilderWriter(4);
        try {
            PrintWriter out = new PrintWriter(buf);
            out.println();
            LINE_SEPARATOR = buf.toString();
            out.close();
            buf.close();
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    buf.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    public static void close(URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

    @Deprecated
    public static void closeQuietly(Reader input) {
        closeQuietly((Closeable) input);
    }

    @Deprecated
    public static void closeQuietly(Writer output) {
        closeQuietly((Closeable) output);
    }

    @Deprecated
    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable) input);
    }

    @Deprecated
    public static void closeQuietly(OutputStream output) {
        closeQuietly((Closeable) output);
    }

    @Deprecated
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    @Deprecated
    public static void closeQuietly(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            closeQuietly(closeable);
        }
    }

    @Deprecated
    public static void closeQuietly(Socket sock) {
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
            }
        }
    }

    @Deprecated
    public static void closeQuietly(Selector selector) {
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
            }
        }
    }

    @Deprecated
    public static void closeQuietly(ServerSocket sock) {
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
            }
        }
    }

    public static InputStream toBufferedInputStream(InputStream input) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input);
    }

    public static InputStream toBufferedInputStream(InputStream input, int size) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input, size);
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static BufferedReader toBufferedReader(Reader reader, int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

    public static BufferedReader buffer(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static BufferedReader buffer(Reader reader, int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

    public static BufferedWriter buffer(Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    public static BufferedWriter buffer(Writer writer, int size) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, size);
    }

    public static BufferedOutputStream buffer(OutputStream outputStream) {
        if (outputStream != null) {
            return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
        }
        throw null;
    }

    public static BufferedOutputStream buffer(OutputStream outputStream, int size) {
        if (outputStream != null) {
            return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream, size);
        }
        throw null;
    }

    public static BufferedInputStream buffer(InputStream inputStream) {
        if (inputStream != null) {
            return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
        }
        throw null;
    }

    public static BufferedInputStream buffer(InputStream inputStream, int size) {
        if (inputStream != null) {
            return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream : new BufferedInputStream(inputStream, size);
        }
        throw null;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            copy(input, output);
            byte[] byteArray = output.toByteArray();
            output.close();
            return byteArray;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    output.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    public static byte[] toByteArray(InputStream input, long size) throws IOException {
        if (size > 2147483647L) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        }
        return toByteArray(input, (int) size);
    }

    public static byte[] toByteArray(InputStream input, int size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        } else if (size == 0) {
            return new byte[0];
        } else {
            byte[] data = new byte[size];
            int offset = 0;
            while (offset < size) {
                int read = input.read(data, offset, size - offset);
                if (read == -1) {
                    break;
                }
                offset += read;
            }
            if (offset != size) {
                throw new IOException("Unexpected read size. current: " + offset + ", expected: " + size);
            }
            return data;
        }
    }

    @Deprecated
    public static byte[] toByteArray(Reader input) throws IOException {
        return toByteArray(input, Charset.defaultCharset());
    }

    public static byte[] toByteArray(Reader input, Charset encoding) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            copy(input, output, encoding);
            byte[] byteArray = output.toByteArray();
            output.close();
            return byteArray;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    output.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    public static byte[] toByteArray(Reader input, String encoding) throws IOException {
        return toByteArray(input, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static byte[] toByteArray(String input) throws IOException {
        return input.getBytes(Charset.defaultCharset());
    }

    public static byte[] toByteArray(URI uri) throws IOException {
        return toByteArray(uri.toURL());
    }

    public static byte[] toByteArray(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        try {
            return toByteArray(conn);
        } finally {
            close(conn);
        }
    }

    public static byte[] toByteArray(URLConnection urlConn) throws IOException {
        InputStream inputStream = urlConn.getInputStream();
        try {
            byte[] byteArray = toByteArray(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return byteArray;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    @Deprecated
    public static char[] toCharArray(InputStream is) throws IOException {
        return toCharArray(is, Charset.defaultCharset());
    }

    public static char[] toCharArray(InputStream is, Charset encoding) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        copy(is, output, encoding);
        return output.toCharArray();
    }

    public static char[] toCharArray(InputStream is, String encoding) throws IOException {
        return toCharArray(is, Charsets.toCharset(encoding));
    }

    public static char[] toCharArray(Reader input) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(input, sw);
        return sw.toCharArray();
    }

    @Deprecated
    public static String toString(InputStream input) throws IOException {
        return toString(input, Charset.defaultCharset());
    }

    public static String toString(InputStream input, Charset encoding) throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        try {
            copy(input, sw, encoding);
            String stringBuilderWriter = sw.toString();
            sw.close();
            return stringBuilderWriter;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    sw.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    public static String toString(InputStream input, String encoding) throws IOException {
        return toString(input, Charsets.toCharset(encoding));
    }

    public static String toString(Reader input) throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        try {
            copy(input, sw);
            String stringBuilderWriter = sw.toString();
            sw.close();
            return stringBuilderWriter;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    sw.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    @Deprecated
    public static String toString(URI uri) throws IOException {
        return toString(uri, Charset.defaultCharset());
    }

    public static String toString(URI uri, Charset encoding) throws IOException {
        return toString(uri.toURL(), Charsets.toCharset(encoding));
    }

    public static String toString(URI uri, String encoding) throws IOException {
        return toString(uri, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static String toString(URL url) throws IOException {
        return toString(url, Charset.defaultCharset());
    }

    public static String toString(URL url, Charset encoding) throws IOException {
        InputStream inputStream = url.openStream();
        try {
            String iOUtils = toString(inputStream, encoding);
            if (inputStream != null) {
                inputStream.close();
            }
            return iOUtils;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static String toString(URL url, String encoding) throws IOException {
        return toString(url, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static String toString(byte[] input) throws IOException {
        return new String(input, Charset.defaultCharset());
    }

    public static String toString(byte[] input, String encoding) throws IOException {
        return new String(input, Charsets.toCharset(encoding));
    }

    public static String resourceToString(String name, Charset encoding) throws IOException {
        return resourceToString(name, encoding, null);
    }

    public static String resourceToString(String name, Charset encoding, ClassLoader classLoader) throws IOException {
        return toString(resourceToURL(name, classLoader), encoding);
    }

    public static byte[] resourceToByteArray(String name) throws IOException {
        return resourceToByteArray(name, null);
    }

    public static byte[] resourceToByteArray(String name, ClassLoader classLoader) throws IOException {
        return toByteArray(resourceToURL(name, classLoader));
    }

    public static URL resourceToURL(String name) throws IOException {
        return resourceToURL(name, null);
    }

    public static URL resourceToURL(String name, ClassLoader classLoader) throws IOException {
        URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);
        if (resource == null) {
            throw new IOException("Resource not found: " + name);
        }
        return resource;
    }

    @Deprecated
    public static List<String> readLines(InputStream input) throws IOException {
        return readLines(input, Charset.defaultCharset());
    }

    public static List<String> readLines(InputStream input, Charset encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(encoding));
        return readLines(reader);
    }

    public static List<String> readLines(InputStream input, String encoding) throws IOException {
        return readLines(input, Charsets.toCharset(encoding));
    }

    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            list.add(line);
        }
        return list;
    }

    public static LineIterator lineIterator(Reader reader) {
        return new LineIterator(reader);
    }

    public static LineIterator lineIterator(InputStream input, Charset encoding) throws IOException {
        return new LineIterator(new InputStreamReader(input, Charsets.toCharset(encoding)));
    }

    public static LineIterator lineIterator(InputStream input, String encoding) throws IOException {
        return lineIterator(input, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static InputStream toInputStream(CharSequence input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(CharSequence input, Charset encoding) {
        return toInputStream(input.toString(), encoding);
    }

    public static InputStream toInputStream(CharSequence input, String encoding) throws IOException {
        return toInputStream(input, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static InputStream toInputStream(String input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(String input, Charset encoding) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(encoding)));
    }

    public static InputStream toInputStream(String input, String encoding) throws IOException {
        byte[] bytes = input.getBytes(Charsets.toCharset(encoding));
        return new ByteArrayInputStream(bytes);
    }

    public static void write(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    public static void writeChunked(byte[] data, OutputStream output) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                int chunk = Math.min(bytes, 4096);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    @Deprecated
    public static void write(byte[] data, Writer output) throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    public static void write(byte[] data, Writer output, Charset encoding) throws IOException {
        if (data != null) {
            output.write(new String(data, Charsets.toCharset(encoding)));
        }
    }

    public static void write(byte[] data, Writer output, String encoding) throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    public static void write(char[] data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    public static void writeChunked(char[] data, Writer output) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                int chunk = Math.min(bytes, 4096);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    @Deprecated
    public static void write(char[] data, OutputStream output) throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    public static void write(char[] data, OutputStream output, Charset encoding) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(Charsets.toCharset(encoding)));
        }
    }

    public static void write(char[] data, OutputStream output, String encoding) throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    public static void write(CharSequence data, Writer output) throws IOException {
        if (data != null) {
            write(data.toString(), output);
        }
    }

    @Deprecated
    public static void write(CharSequence data, OutputStream output) throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    public static void write(CharSequence data, OutputStream output, Charset encoding) throws IOException {
        if (data != null) {
            write(data.toString(), output, encoding);
        }
    }

    public static void write(CharSequence data, OutputStream output, String encoding) throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    public static void write(String data, Writer output) throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

    @Deprecated
    public static void write(String data, OutputStream output) throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    public static void write(String data, OutputStream output, Charset encoding) throws IOException {
        if (data != null) {
            output.write(data.getBytes(Charsets.toCharset(encoding)));
        }
    }

    public static void write(String data, OutputStream output, String encoding) throws IOException {
        write(data, output, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static void write(StringBuffer data, Writer output) throws IOException {
        if (data != null) {
            output.write(data.toString());
        }
    }

    @Deprecated
    public static void write(StringBuffer data, OutputStream output) throws IOException {
        write(data, output, (String) null);
    }

    @Deprecated
    public static void write(StringBuffer data, OutputStream output, String encoding) throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(Charsets.toCharset(encoding)));
        }
    }

    @Deprecated
    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output) throws IOException {
        writeLines(lines, lineEnding, output, Charset.defaultCharset());
    }

    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, Charset encoding) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = LINE_SEPARATOR;
        }
        Charset cs = Charsets.toCharset(encoding);
        for (Object line : lines) {
            if (line != null) {
                output.write(line.toString().getBytes(cs));
            }
            output.write(lineEnding.getBytes(cs));
        }
    }

    public static void writeLines(Collection<?> lines, String lineEnding, OutputStream output, String encoding) throws IOException {
        writeLines(lines, lineEnding, output, Charsets.toCharset(encoding));
    }

    public static void writeLines(Collection<?> lines, String lineEnding, Writer writer) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = LINE_SEPARATOR;
        }
        for (Object line : lines) {
            if (line != null) {
                writer.write(line.toString());
            }
            writer.write(lineEnding);
        }
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 4096);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 != n) {
                output.write(buffer, 0, n);
                count += n;
            } else {
                return count;
            }
        }
    }

    public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length) throws IOException {
        return copyLarge(input, output, inputOffset, length, new byte[4096]);
    }

    public static long copyLarge(InputStream input, OutputStream output, long inputOffset, long length, byte[] buffer) throws IOException {
        long j = 0;
        if (inputOffset > 0) {
            skipFully(input, inputOffset);
        }
        if (length == 0) {
            return 0L;
        }
        int bufferLength = buffer.length;
        int bytesToRead = bufferLength;
        if (length > 0 && length < bufferLength) {
            bytesToRead = (int) length;
        }
        long totalRead = 0;
        while (bytesToRead > 0) {
            int read = input.read(buffer, 0, bytesToRead);
            if (-1 == read) {
                break;
            }
            output.write(buffer, 0, read);
            totalRead += read;
            if (length <= j) {
                j = 0;
            } else {
                bytesToRead = (int) Math.min(length - totalRead, bufferLength);
                j = 0;
            }
        }
        return totalRead;
    }

    @Deprecated
    public static void copy(InputStream input, Writer output) throws IOException {
        copy(input, output, Charset.defaultCharset());
    }

    public static void copy(InputStream input, Writer output, Charset inputEncoding) throws IOException {
        InputStreamReader in = new InputStreamReader(input, Charsets.toCharset(inputEncoding));
        copy(in, output);
    }

    public static void copy(InputStream input, Writer output, String inputEncoding) throws IOException {
        copy(input, output, Charsets.toCharset(inputEncoding));
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        return copyLarge(input, output, new char[4096]);
    }

    public static long copyLarge(Reader input, Writer output, char[] buffer) throws IOException {
        long count = 0;
        while (true) {
            int n = input.read(buffer);
            if (-1 != n) {
                output.write(buffer, 0, n);
                count += n;
            } else {
                return count;
            }
        }
    }

    public static long copyLarge(Reader input, Writer output, long inputOffset, long length) throws IOException {
        return copyLarge(input, output, inputOffset, length, new char[4096]);
    }

    public static long copyLarge(Reader input, Writer output, long inputOffset, long length, char[] buffer) throws IOException {
        long j = 0;
        if (inputOffset > 0) {
            skipFully(input, inputOffset);
        }
        if (length == 0) {
            return 0L;
        }
        int bytesToRead = buffer.length;
        if (length > 0 && length < buffer.length) {
            bytesToRead = (int) length;
        }
        long totalRead = 0;
        while (bytesToRead > 0) {
            int read = input.read(buffer, 0, bytesToRead);
            if (-1 == read) {
                break;
            }
            output.write(buffer, 0, read);
            totalRead += read;
            if (length <= j) {
                j = 0;
            } else {
                bytesToRead = (int) Math.min(length - totalRead, buffer.length);
                j = 0;
            }
        }
        return totalRead;
    }

    @Deprecated
    public static void copy(Reader input, OutputStream output) throws IOException {
        copy(input, output, Charset.defaultCharset());
    }

    public static void copy(Reader input, OutputStream output, Charset outputEncoding) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(output, Charsets.toCharset(outputEncoding));
        copy(input, out);
        out.flush();
    }

    public static void copy(Reader input, OutputStream output, String outputEncoding) throws IOException {
        copy(input, output, Charsets.toCharset(outputEncoding));
    }

    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }
        for (int ch = input1.read(); -1 != ch; ch = input1.read()) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
        }
        int ch22 = input2.read();
        return ch22 == -1;
    }

    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        Reader input12 = toBufferedReader(input1);
        Reader input22 = toBufferedReader(input2);
        for (int ch = input12.read(); -1 != ch; ch = input12.read()) {
            int ch2 = input22.read();
            if (ch != ch2) {
                return false;
            }
        }
        int ch22 = input22.read();
        return ch22 == -1;
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0029, code lost:
        if (r4 != null) goto L14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x002c, code lost:
        return false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:21:?, code lost:
        return true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static boolean contentEqualsIgnoreEOL(java.io.Reader r6, java.io.Reader r7) throws java.io.IOException {
        /*
            r0 = 1
            if (r6 != r7) goto L4
            return r0
        L4:
            java.io.BufferedReader r1 = toBufferedReader(r6)
            java.io.BufferedReader r2 = toBufferedReader(r7)
            java.lang.String r3 = r1.readLine()
            java.lang.String r4 = r2.readLine()
        L14:
            if (r3 == 0) goto L27
            if (r4 == 0) goto L27
            boolean r5 = r3.equals(r4)
            if (r5 == 0) goto L27
            java.lang.String r3 = r1.readLine()
            java.lang.String r4 = r2.readLine()
            goto L14
        L27:
            if (r3 != 0) goto L2e
            if (r4 != 0) goto L2c
            goto L32
        L2c:
            r0 = 0
            goto L32
        L2e:
            boolean r0 = r3.equals(r4)
        L32:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.io.IOUtils.contentEqualsIgnoreEOL(java.io.Reader, java.io.Reader):boolean");
    }

    public static long skip(InputStream input, long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[2048];
        }
        long remain = toSkip;
        while (remain > 0) {
            long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, 2048L));
            if (n < 0) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    public static long skip(ReadableByteChannel input, long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        ByteBuffer skipByteBuffer = ByteBuffer.allocate((int) Math.min(toSkip, 2048L));
        long remain = toSkip;
        while (remain > 0) {
            skipByteBuffer.position(0);
            skipByteBuffer.limit((int) Math.min(remain, 2048L));
            int n = input.read(skipByteBuffer);
            if (n == -1) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    public static long skip(Reader input, long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        if (SKIP_CHAR_BUFFER == null) {
            SKIP_CHAR_BUFFER = new char[2048];
        }
        long remain = toSkip;
        while (remain > 0) {
            long n = input.read(SKIP_CHAR_BUFFER, 0, (int) Math.min(remain, 2048L));
            if (n < 0) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    public static void skipFully(InputStream input, long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static void skipFully(ReadableByteChannel input, long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static void skipFully(Reader input, long toSkip) throws IOException {
        long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static int read(Reader input, char[] buffer, int offset, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            int location = length - remaining;
            int count = input.read(buffer, offset + location, remaining);
            if (-1 == count) {
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    public static int read(Reader input, char[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    public static int read(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            int location = length - remaining;
            int count = input.read(buffer, offset + location, remaining);
            if (-1 == count) {
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

    public static int read(InputStream input, byte[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    public static int read(ReadableByteChannel input, ByteBuffer buffer) throws IOException {
        int length = buffer.remaining();
        while (buffer.remaining() > 0) {
            int count = input.read(buffer);
            if (-1 == count) {
                break;
            }
        }
        return length - buffer.remaining();
    }

    public static void readFully(Reader input, char[] buffer, int offset, int length) throws IOException {
        int actual = read(input, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    public static void readFully(Reader input, char[] buffer) throws IOException {
        readFully(input, buffer, 0, buffer.length);
    }

    public static void readFully(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        int actual = read(input, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

    public static void readFully(InputStream input, byte[] buffer) throws IOException {
        readFully(input, buffer, 0, buffer.length);
    }

    public static byte[] readFully(InputStream input, int length) throws IOException {
        byte[] buffer = new byte[length];
        readFully(input, buffer, 0, buffer.length);
        return buffer;
    }

    public static void readFully(ReadableByteChannel input, ByteBuffer buffer) throws IOException {
        int expected = buffer.remaining();
        int actual = read(input, buffer);
        if (actual != expected) {
            throw new EOFException("Length to read: " + expected + " actual: " + actual);
        }
    }
}