package org.apache.commons.io.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.ByteOrderMark;
/* loaded from: classes.dex */
public class XmlStreamReader extends Reader {
    private static final int BUFFER_SIZE = 4096;
    private static final String HTTP_EX_1 = "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be NULL";
    private static final String HTTP_EX_2 = "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch";
    private static final String HTTP_EX_3 = "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Invalid MIME";
    private static final String RAW_EX_1 = "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch";
    private static final String RAW_EX_2 = "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM";
    private static final String US_ASCII = "US-ASCII";
    private static final String UTF_16 = "UTF-16";
    private static final String UTF_32 = "UTF-32";
    private final String defaultEncoding;
    private final String encoding;
    private final Reader reader;
    private static final ByteOrderMark[] BOMS = {ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE};
    private static final String UTF_8 = "UTF-8";
    private static final String UTF_16BE = "UTF-16BE";
    private static final String UTF_16LE = "UTF-16LE";
    private static final String UTF_32BE = "UTF-32BE";
    private static final String UTF_32LE = "UTF-32LE";
    private static final String EBCDIC = "CP1047";
    private static final ByteOrderMark[] XML_GUESS_BYTES = {new ByteOrderMark(UTF_8, 60, 63, 120, 109), new ByteOrderMark(UTF_16BE, 0, 60, 0, 63), new ByteOrderMark(UTF_16LE, 60, 0, 63, 0), new ByteOrderMark(UTF_32BE, 0, 0, 0, 60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109), new ByteOrderMark(UTF_32LE, 60, 0, 0, 0, 63, 0, 0, 0, 120, 0, 0, 0, 109, 0, 0, 0), new ByteOrderMark(EBCDIC, 76, 111, 167, 148)};
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=[\"']?([.[^; \"']]*)[\"']?");
    public static final Pattern ENCODING_PATTERN = Pattern.compile("<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))", 8);

    public String getDefaultEncoding() {
        return this.defaultEncoding;
    }

    public XmlStreamReader(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public XmlStreamReader(InputStream is) throws IOException {
        this(is, true);
    }

    public XmlStreamReader(InputStream is, boolean lenient) throws IOException {
        this(is, lenient, (String) null);
    }

    public XmlStreamReader(InputStream is, boolean lenient, String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        BOMInputStream bom = new BOMInputStream(new BufferedInputStream(is, 4096), false, BOMS);
        BOMInputStream pis = new BOMInputStream(bom, true, XML_GUESS_BYTES);
        this.encoding = doRawStream(bom, pis, lenient);
        this.reader = new InputStreamReader(pis, this.encoding);
    }

    public XmlStreamReader(URL url) throws IOException {
        this(url.openConnection(), (String) null);
    }

    public XmlStreamReader(URLConnection conn, String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        String contentType = conn.getContentType();
        InputStream is = conn.getInputStream();
        BOMInputStream bom = new BOMInputStream(new BufferedInputStream(is, 4096), false, BOMS);
        BOMInputStream pis = new BOMInputStream(bom, true, XML_GUESS_BYTES);
        if ((conn instanceof HttpURLConnection) || contentType != null) {
            this.encoding = doHttpStream(bom, pis, contentType, true);
        } else {
            this.encoding = doRawStream(bom, pis, true);
        }
        this.reader = new InputStreamReader(pis, this.encoding);
    }

    public XmlStreamReader(InputStream is, String httpContentType) throws IOException {
        this(is, httpContentType, true);
    }

    public XmlStreamReader(InputStream is, String httpContentType, boolean lenient, String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        BOMInputStream bom = new BOMInputStream(new BufferedInputStream(is, 4096), false, BOMS);
        BOMInputStream pis = new BOMInputStream(bom, true, XML_GUESS_BYTES);
        this.encoding = doHttpStream(bom, pis, httpContentType, lenient);
        this.reader = new InputStreamReader(pis, this.encoding);
    }

    public XmlStreamReader(InputStream is, String httpContentType, boolean lenient) throws IOException {
        this(is, httpContentType, lenient, null);
    }

    public String getEncoding() {
        return this.encoding;
    }

    @Override // java.io.Reader
    public int read(char[] buf, int offset, int len) throws IOException {
        return this.reader.read(buf, offset, len);
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.reader.close();
    }

    private String doRawStream(BOMInputStream bom, BOMInputStream pis, boolean lenient) throws IOException {
        String bomEnc = bom.getBOMCharsetName();
        String xmlGuessEnc = pis.getBOMCharsetName();
        String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        try {
            return calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
        } catch (XmlStreamReaderException ex) {
            if (lenient) {
                return doLenientDetection(null, ex);
            }
            throw ex;
        }
    }

    private String doHttpStream(BOMInputStream bom, BOMInputStream pis, String httpContentType, boolean lenient) throws IOException {
        String bomEnc = bom.getBOMCharsetName();
        String xmlGuessEnc = pis.getBOMCharsetName();
        String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        try {
            return calculateHttpEncoding(httpContentType, bomEnc, xmlGuessEnc, xmlEnc, lenient);
        } catch (XmlStreamReaderException ex) {
            if (lenient) {
                return doLenientDetection(httpContentType, ex);
            }
            throw ex;
        }
    }

    private String doLenientDetection(String httpContentType, XmlStreamReaderException ex) throws IOException {
        if (httpContentType != null && httpContentType.startsWith("text/html")) {
            String httpContentType2 = httpContentType.substring("text/html".length());
            try {
                return calculateHttpEncoding("text/xml" + httpContentType2, ex.getBomEncoding(), ex.getXmlGuessEncoding(), ex.getXmlEncoding(), true);
            } catch (XmlStreamReaderException ex2) {
                ex = ex2;
            }
        }
        String encoding = ex.getXmlEncoding();
        if (encoding == null) {
            encoding = ex.getContentTypeEncoding();
        }
        if (encoding == null) {
            String encoding2 = this.defaultEncoding;
            if (encoding2 == null) {
                encoding2 = UTF_8;
            }
            return encoding2;
        }
        return encoding;
    }

    String calculateRawEncoding(String bomEnc, String xmlGuessEnc, String xmlEnc) throws IOException {
        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                String str = this.defaultEncoding;
                return str == null ? UTF_8 : str;
            } else if (xmlEnc.equals(UTF_16) && (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc.equals(UTF_16LE))) {
                return xmlGuessEnc;
            } else {
                return xmlEnc;
            }
        } else if (bomEnc.equals(UTF_8)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            } else if (xmlEnc != null && !xmlEnc.equals(UTF_8)) {
                String msg2 = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg2, bomEnc, xmlGuessEnc, xmlEnc);
            } else {
                return bomEnc;
            }
        } else if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                String msg3 = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg3, bomEnc, xmlGuessEnc, xmlEnc);
            } else if (xmlEnc != null && !xmlEnc.equals(UTF_16) && !xmlEnc.equals(bomEnc)) {
                String msg4 = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg4, bomEnc, xmlGuessEnc, xmlEnc);
            } else {
                return bomEnc;
            }
        } else if (bomEnc.equals(UTF_32BE) || bomEnc.equals(UTF_32LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                String msg5 = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg5, bomEnc, xmlGuessEnc, xmlEnc);
            } else if (xmlEnc != null && !xmlEnc.equals(UTF_32) && !xmlEnc.equals(bomEnc)) {
                String msg6 = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg6, bomEnc, xmlGuessEnc, xmlEnc);
            } else {
                return bomEnc;
            }
        } else {
            String msg7 = MessageFormat.format(RAW_EX_2, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg7, bomEnc, xmlGuessEnc, xmlEnc);
        }
    }

    String calculateHttpEncoding(String httpContentType, String bomEnc, String xmlGuessEnc, String xmlEnc, boolean lenient) throws IOException {
        if (!lenient || xmlEnc == null) {
            String cTMime = getContentTypeMime(httpContentType);
            String cTEnc = getContentTypeEncoding(httpContentType);
            boolean appXml = isAppXml(cTMime);
            boolean textXml = isTextXml(cTMime);
            if (!appXml && !textXml) {
                String msg = MessageFormat.format(HTTP_EX_3, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            } else if (cTEnc == null) {
                if (appXml) {
                    return calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
                }
                String str = this.defaultEncoding;
                return str == null ? US_ASCII : str;
            } else if (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE)) {
                if (bomEnc != null) {
                    String msg2 = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                    throw new XmlStreamReaderException(msg2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                }
                return cTEnc;
            } else if (cTEnc.equals(UTF_16)) {
                if (bomEnc == null || !bomEnc.startsWith(UTF_16)) {
                    String msg3 = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                    throw new XmlStreamReaderException(msg3, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                }
                return bomEnc;
            } else if (cTEnc.equals(UTF_32BE) || cTEnc.equals(UTF_32LE)) {
                if (bomEnc != null) {
                    String msg4 = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                    throw new XmlStreamReaderException(msg4, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                }
                return cTEnc;
            } else if (cTEnc.equals(UTF_32)) {
                if (bomEnc == null || !bomEnc.startsWith(UTF_32)) {
                    String msg5 = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                    throw new XmlStreamReaderException(msg5, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                }
                return bomEnc;
            } else {
                return cTEnc;
            }
        }
        return xmlEnc;
    }

    static String getContentTypeMime(String httpContentType) {
        String mime;
        if (httpContentType == null) {
            return null;
        }
        int i = httpContentType.indexOf(";");
        if (i >= 0) {
            mime = httpContentType.substring(0, i);
        } else {
            mime = httpContentType;
        }
        return mime.trim();
    }

    static String getContentTypeEncoding(String httpContentType) {
        int i;
        if (httpContentType == null || (i = httpContentType.indexOf(";")) <= -1) {
            return null;
        }
        String postMime = httpContentType.substring(i + 1);
        Matcher m = CHARSET_PATTERN.matcher(postMime);
        String encoding = m.find() ? m.group(1) : null;
        return encoding != null ? encoding.toUpperCase(Locale.US) : null;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x0031, code lost:
        if (r5 != (-1)) goto L15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x003a, code lost:
        throw new java.io.IOException("Unexpected end of XML stream");
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0056, code lost:
        throw new java.io.IOException("XML prolog or ROOT element not found on first " + r3 + " bytes");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private static java.lang.String getXmlProlog(java.io.InputStream r14, java.lang.String r15) throws java.io.IOException {
        /*
            r0 = 0
            if (r15 == 0) goto L9f
            r1 = 4096(0x1000, float:5.74E-42)
            byte[] r2 = new byte[r1]
            r14.mark(r1)
            r3 = 0
            r4 = 4096(0x1000, float:5.74E-42)
            int r5 = r14.read(r2, r3, r4)
            r6 = -1
            java.lang.String r7 = ""
        L14:
            r8 = 0
            r9 = -1
            if (r5 == r9) goto L2f
            if (r6 != r9) goto L2f
            if (r3 >= r1) goto L2f
            int r3 = r3 + r5
            int r4 = r4 - r5
            int r5 = r14.read(r2, r3, r4)
            java.lang.String r9 = new java.lang.String
            r9.<init>(r2, r8, r3, r15)
            r7 = r9
            r8 = 62
            int r6 = r7.indexOf(r8)
            goto L14
        L2f:
            if (r6 != r9) goto L57
            if (r5 != r9) goto L3b
            java.io.IOException r1 = new java.io.IOException
            java.lang.String r8 = "Unexpected end of XML stream"
            r1.<init>(r8)
            throw r1
        L3b:
            java.io.IOException r1 = new java.io.IOException
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "XML prolog or ROOT element not found on first "
            r8.append(r9)
            r8.append(r3)
            java.lang.String r9 = " bytes"
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            r1.<init>(r8)
            throw r1
        L57:
            r1 = r3
            if (r1 <= 0) goto L9f
            r14.reset()
            java.io.BufferedReader r9 = new java.io.BufferedReader
            java.io.StringReader r10 = new java.io.StringReader
            int r11 = r6 + 1
            java.lang.String r8 = r7.substring(r8, r11)
            r10.<init>(r8)
            r9.<init>(r10)
            r8 = r9
            java.lang.StringBuffer r9 = new java.lang.StringBuffer
            r9.<init>()
            java.lang.String r10 = r8.readLine()
        L77:
            if (r10 == 0) goto L81
            r9.append(r10)
            java.lang.String r10 = r8.readLine()
            goto L77
        L81:
            java.util.regex.Pattern r11 = org.apache.commons.io.input.XmlStreamReader.ENCODING_PATTERN
            java.util.regex.Matcher r11 = r11.matcher(r9)
            boolean r12 = r11.find()
            if (r12 == 0) goto L9f
            r12 = 1
            java.lang.String r13 = r11.group(r12)
            java.lang.String r0 = r13.toUpperCase()
            int r13 = r0.length()
            int r13 = r13 - r12
            java.lang.String r0 = r0.substring(r12, r13)
        L9f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.commons.io.input.XmlStreamReader.getXmlProlog(java.io.InputStream, java.lang.String):java.lang.String");
    }

    static boolean isAppXml(String mime) {
        return mime != null && (mime.equals("application/xml") || mime.equals("application/xml-dtd") || mime.equals("application/xml-external-parsed-entity") || (mime.startsWith("application/") && mime.endsWith("+xml")));
    }

    static boolean isTextXml(String mime) {
        return mime != null && (mime.equals("text/xml") || mime.equals("text/xml-external-parsed-entity") || (mime.startsWith("text/") && mime.endsWith("+xml")));
    }
}
