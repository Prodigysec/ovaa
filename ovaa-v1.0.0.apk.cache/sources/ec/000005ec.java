package com.google.gson.stream;

import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;

/* loaded from: classes.dex */
public class JsonReader implements Closeable {
    private static final long MIN_INCOMPLETE_INTEGER = -922337203685477580L;
    private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();
    private static final int NUMBER_CHAR_DECIMAL = 3;
    private static final int NUMBER_CHAR_DIGIT = 2;
    private static final int NUMBER_CHAR_EXP_DIGIT = 7;
    private static final int NUMBER_CHAR_EXP_E = 5;
    private static final int NUMBER_CHAR_EXP_SIGN = 6;
    private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
    private static final int NUMBER_CHAR_NONE = 0;
    private static final int NUMBER_CHAR_SIGN = 1;
    private static final int PEEKED_BEGIN_ARRAY = 3;
    private static final int PEEKED_BEGIN_OBJECT = 1;
    private static final int PEEKED_BUFFERED = 11;
    private static final int PEEKED_DOUBLE_QUOTED = 9;
    private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
    private static final int PEEKED_END_ARRAY = 4;
    private static final int PEEKED_END_OBJECT = 2;
    private static final int PEEKED_EOF = 17;
    private static final int PEEKED_FALSE = 6;
    private static final int PEEKED_LONG = 15;
    private static final int PEEKED_NONE = 0;
    private static final int PEEKED_NULL = 7;
    private static final int PEEKED_NUMBER = 16;
    private static final int PEEKED_SINGLE_QUOTED = 8;
    private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
    private static final int PEEKED_TRUE = 5;
    private static final int PEEKED_UNQUOTED = 10;
    private static final int PEEKED_UNQUOTED_NAME = 14;
    private final Reader in;
    private int[] pathIndices;
    private String[] pathNames;
    private long peekedLong;
    private int peekedNumberLength;
    private String peekedString;
    private int[] stack;
    private int stackSize;
    private boolean lenient = false;
    private final char[] buffer = new char[1024];
    private int pos = 0;
    private int limit = 0;
    private int lineNumber = 0;
    private int lineStart = 0;
    int peeked = 0;

    static {
        JsonReaderInternalAccess.INSTANCE = new JsonReaderInternalAccess() { // from class: com.google.gson.stream.JsonReader.1
            @Override // com.google.gson.internal.JsonReaderInternalAccess
            public void promoteNameToValue(JsonReader reader) throws IOException {
                if (reader instanceof JsonTreeReader) {
                    ((JsonTreeReader) reader).promoteNameToValue();
                    return;
                }
                int p = reader.peeked;
                if (p == 0) {
                    p = reader.doPeek();
                }
                if (p == 13) {
                    reader.peeked = 9;
                } else if (p == 12) {
                    reader.peeked = 8;
                } else if (p == 14) {
                    reader.peeked = 10;
                } else {
                    throw new IllegalStateException("Expected a name but was " + reader.peek() + reader.locationString());
                }
            }
        };
    }

    public JsonReader(Reader in) {
        int[] iArr = new int[32];
        this.stack = iArr;
        this.stackSize = 0;
        this.stackSize = 0 + 1;
        iArr[0] = 6;
        this.pathNames = new String[32];
        this.pathIndices = new int[32];
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        this.in = in;
    }

    public final void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public final boolean isLenient() {
        return this.lenient;
    }

    public void beginArray() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 3) {
            push(1);
            this.pathIndices[this.stackSize - 1] = 0;
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected BEGIN_ARRAY but was " + peek() + locationString());
    }

    public void endArray() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 4) {
            int i = this.stackSize - 1;
            this.stackSize = i;
            int[] iArr = this.pathIndices;
            int i2 = i - 1;
            iArr[i2] = iArr[i2] + 1;
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected END_ARRAY but was " + peek() + locationString());
    }

    public void beginObject() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 1) {
            push(3);
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected BEGIN_OBJECT but was " + peek() + locationString());
    }

    public void endObject() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 2) {
            int i = this.stackSize - 1;
            this.stackSize = i;
            this.pathNames[i] = null;
            int[] iArr = this.pathIndices;
            int i2 = i - 1;
            iArr[i2] = iArr[i2] + 1;
            this.peeked = 0;
            return;
        }
        throw new IllegalStateException("Expected END_OBJECT but was " + peek() + locationString());
    }

    public boolean hasNext() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        return (p == 2 || p == 4) ? false : true;
    }

    public JsonToken peek() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        switch (p) {
            case 1:
                return JsonToken.BEGIN_OBJECT;
            case 2:
                return JsonToken.END_OBJECT;
            case 3:
                return JsonToken.BEGIN_ARRAY;
            case 4:
                return JsonToken.END_ARRAY;
            case 5:
            case 6:
                return JsonToken.BOOLEAN;
            case 7:
                return JsonToken.NULL;
            case 8:
            case 9:
            case 10:
            case 11:
                return JsonToken.STRING;
            case 12:
            case 13:
            case 14:
                return JsonToken.NAME;
            case 15:
            case 16:
                return JsonToken.NUMBER;
            case 17:
                return JsonToken.END_DOCUMENT;
            default:
                throw new AssertionError();
        }
    }

    int doPeek() throws IOException {
        int c;
        int[] iArr = this.stack;
        int i = this.stackSize;
        int peekStack = iArr[i - 1];
        if (peekStack == 1) {
            iArr[i - 1] = 2;
        } else if (peekStack == 2) {
            int c2 = nextNonWhitespace(true);
            if (c2 != 44) {
                if (c2 != 59) {
                    if (c2 != 93) {
                        throw syntaxError("Unterminated array");
                    }
                    this.peeked = 4;
                    return 4;
                }
                checkLenient();
            }
        } else if (peekStack == 3 || peekStack == 5) {
            this.stack[this.stackSize - 1] = 4;
            if (peekStack == 5 && (c = nextNonWhitespace(true)) != 44) {
                if (c != 59) {
                    if (c == 125) {
                        this.peeked = 2;
                        return 2;
                    }
                    throw syntaxError("Unterminated object");
                }
                checkLenient();
            }
            int c3 = nextNonWhitespace(true);
            if (c3 == 34) {
                this.peeked = 13;
                return 13;
            } else if (c3 != 39) {
                if (c3 == 125) {
                    if (peekStack != 5) {
                        this.peeked = 2;
                        return 2;
                    }
                    throw syntaxError("Expected name");
                }
                checkLenient();
                this.pos--;
                if (isLiteral((char) c3)) {
                    this.peeked = 14;
                    return 14;
                }
                throw syntaxError("Expected name");
            } else {
                checkLenient();
                this.peeked = 12;
                return 12;
            }
        } else if (peekStack == 4) {
            iArr[i - 1] = 5;
            int c4 = nextNonWhitespace(true);
            if (c4 != 58) {
                if (c4 != 61) {
                    throw syntaxError("Expected ':'");
                }
                checkLenient();
                if (this.pos < this.limit || fillBuffer(1)) {
                    char[] cArr = this.buffer;
                    int i2 = this.pos;
                    if (cArr[i2] == '>') {
                        this.pos = i2 + 1;
                    }
                }
            }
        } else if (peekStack == 6) {
            if (this.lenient) {
                consumeNonExecutePrefix();
            }
            this.stack[this.stackSize - 1] = 7;
        } else if (peekStack == 7) {
            if (nextNonWhitespace(false) == -1) {
                this.peeked = 17;
                return 17;
            }
            checkLenient();
            this.pos--;
        } else if (peekStack == 8) {
            throw new IllegalStateException("JsonReader is closed");
        }
        int c5 = nextNonWhitespace(true);
        if (c5 == 34) {
            this.peeked = 9;
            return 9;
        } else if (c5 != 39) {
            if (c5 != 44 && c5 != 59) {
                if (c5 == 91) {
                    this.peeked = 3;
                    return 3;
                } else if (c5 != 93) {
                    if (c5 != 123) {
                        this.pos--;
                        int result = peekKeyword();
                        if (result != 0) {
                            return result;
                        }
                        int result2 = peekNumber();
                        if (result2 == 0) {
                            if (!isLiteral(this.buffer[this.pos])) {
                                throw syntaxError("Expected value");
                            }
                            checkLenient();
                            this.peeked = 10;
                            return 10;
                        }
                        return result2;
                    }
                    this.peeked = 1;
                    return 1;
                } else if (peekStack == 1) {
                    this.peeked = 4;
                    return 4;
                }
            }
            if (peekStack != 1 && peekStack != 2) {
                throw syntaxError("Unexpected value");
            }
            checkLenient();
            this.pos--;
            this.peeked = 7;
            return 7;
        } else {
            checkLenient();
            this.peeked = 8;
            return 8;
        }
    }

    private int peekKeyword() throws IOException {
        String keyword;
        String keywordUpper;
        int peeking;
        char c = this.buffer[this.pos];
        if (c == 't' || c == 'T') {
            keyword = "true";
            keywordUpper = "TRUE";
            peeking = 5;
        } else if (c == 'f' || c == 'F') {
            keyword = "false";
            keywordUpper = "FALSE";
            peeking = 6;
        } else if (c != 'n' && c != 'N') {
            return 0;
        } else {
            keyword = "null";
            keywordUpper = "NULL";
            peeking = 7;
        }
        int length = keyword.length();
        for (int i = 1; i < length; i++) {
            if (this.pos + i >= this.limit && !fillBuffer(i + 1)) {
                return 0;
            }
            char c2 = this.buffer[this.pos + i];
            if (c2 != keyword.charAt(i) && c2 != keywordUpper.charAt(i)) {
                return 0;
            }
        }
        int i2 = this.pos;
        if ((i2 + length < this.limit || fillBuffer(length + 1)) && isLiteral(this.buffer[this.pos + length])) {
            return 0;
        }
        this.pos += length;
        this.peeked = peeking;
        return peeking;
    }

    /* JADX WARN: Code restructure failed: missing block: B:55:0x0092, code lost:
        if (isLiteral(r10) != false) goto L82;
     */
    /* JADX WARN: Code restructure failed: missing block: B:56:0x0095, code lost:
        if (r8 != 2) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x0097, code lost:
        if (r7 == false) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x009d, code lost:
        if (r4 != Long.MIN_VALUE) goto L15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x009f, code lost:
        if (r6 == false) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x00a3, code lost:
        if (r4 != 0) goto L18;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x00a5, code lost:
        if (r6 != false) goto L24;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x00a7, code lost:
        if (r6 == false) goto L23;
     */
    /* JADX WARN: Code restructure failed: missing block: B:65:0x00a9, code lost:
        r10 = r4;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x00ab, code lost:
        r10 = -r4;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x00ac, code lost:
        r19.peekedLong = r10;
        r19.pos += r9;
        r19.peeked = 15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:68:0x00b7, code lost:
        return 15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:69:0x00b8, code lost:
        if (r8 == 2) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x00bb, code lost:
        if (r8 == 4) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:73:0x00be, code lost:
        if (r8 != 7) goto L29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:75:0x00c1, code lost:
        return 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:77:0x00c3, code lost:
        r19.peekedNumberLength = r9;
        r19.peeked = 16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:78:0x00c9, code lost:
        return 16;
     */
    /* JADX WARN: Code restructure failed: missing block: B:79:0x00ca, code lost:
        return 0;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private int peekNumber() throws java.io.IOException {
        /*
            Method dump skipped, instructions count: 241
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.gson.stream.JsonReader.peekNumber():int");
    }

    private boolean isLiteral(char c) throws IOException {
        if (c != '\t' && c != '\n' && c != '\f' && c != '\r' && c != ' ') {
            if (c != '#') {
                if (c == ',') {
                    return false;
                }
                if (c != '/' && c != '=') {
                    if (c == '{' || c == '}' || c == ':') {
                        return false;
                    }
                    if (c != ';') {
                        switch (c) {
                            case '[':
                            case ']':
                                return false;
                            case '\\':
                                break;
                            default:
                                return true;
                        }
                    }
                }
            }
            checkLenient();
            return false;
        }
        return false;
    }

    public String nextName() throws IOException {
        String result;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 14) {
            result = nextUnquotedValue();
        } else if (p == 12) {
            result = nextQuotedValue('\'');
        } else if (p == 13) {
            result = nextQuotedValue('\"');
        } else {
            throw new IllegalStateException("Expected a name but was " + peek() + locationString());
        }
        this.peeked = 0;
        this.pathNames[this.stackSize - 1] = result;
        return result;
    }

    public String nextString() throws IOException {
        String result;
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 10) {
            result = nextUnquotedValue();
        } else if (p == 8) {
            result = nextQuotedValue('\'');
        } else if (p == 9) {
            result = nextQuotedValue('\"');
        } else if (p == 11) {
            result = this.peekedString;
            this.peekedString = null;
        } else if (p == 15) {
            result = Long.toString(this.peekedLong);
        } else if (p == 16) {
            result = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else {
            throw new IllegalStateException("Expected a string but was " + peek() + locationString());
        }
        this.peeked = 0;
        int[] iArr = this.pathIndices;
        int i = this.stackSize - 1;
        iArr[i] = iArr[i] + 1;
        return result;
    }

    public boolean nextBoolean() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 5) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return true;
        } else if (p == 6) {
            this.peeked = 0;
            int[] iArr2 = this.pathIndices;
            int i2 = this.stackSize - 1;
            iArr2[i2] = iArr2[i2] + 1;
            return false;
        } else {
            throw new IllegalStateException("Expected a boolean but was " + peek() + locationString());
        }
    }

    public void nextNull() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 7) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return;
        }
        throw new IllegalStateException("Expected null but was " + peek() + locationString());
    }

    public double nextDouble() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 15) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return this.peekedLong;
        }
        if (p == 16) {
            this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else if (p == 8 || p == 9) {
            this.peekedString = nextQuotedValue(p == 8 ? '\'' : '\"');
        } else if (p == 10) {
            this.peekedString = nextUnquotedValue();
        } else if (p != 11) {
            throw new IllegalStateException("Expected a double but was " + peek() + locationString());
        }
        this.peeked = 11;
        double result = Double.parseDouble(this.peekedString);
        if (!this.lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
            throw new MalformedJsonException("JSON forbids NaN and infinities: " + result + locationString());
        }
        this.peekedString = null;
        this.peeked = 0;
        int[] iArr2 = this.pathIndices;
        int i2 = this.stackSize - 1;
        iArr2[i2] = iArr2[i2] + 1;
        return result;
    }

    public long nextLong() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 15) {
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return this.peekedLong;
        }
        if (p == 16) {
            this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else if (p == 8 || p == 9 || p == 10) {
            if (p == 10) {
                this.peekedString = nextUnquotedValue();
            } else {
                this.peekedString = nextQuotedValue(p == 8 ? '\'' : '\"');
            }
            try {
                long result = Long.parseLong(this.peekedString);
                this.peeked = 0;
                int[] iArr2 = this.pathIndices;
                int i2 = this.stackSize - 1;
                iArr2[i2] = iArr2[i2] + 1;
                return result;
            } catch (NumberFormatException e) {
            }
        } else {
            throw new IllegalStateException("Expected a long but was " + peek() + locationString());
        }
        this.peeked = 11;
        double asDouble = Double.parseDouble(this.peekedString);
        long result2 = (long) asDouble;
        if (result2 != asDouble) {
            throw new NumberFormatException("Expected a long but was " + this.peekedString + locationString());
        }
        this.peekedString = null;
        this.peeked = 0;
        int[] iArr3 = this.pathIndices;
        int i3 = this.stackSize - 1;
        iArr3[i3] = iArr3[i3] + 1;
        return result2;
    }

    private String nextQuotedValue(char quote) throws IOException {
        char[] buffer = this.buffer;
        StringBuilder builder = null;
        do {
            int c = this.pos;
            int l = this.limit;
            int start = c;
            while (c < l) {
                int p = c + 1;
                char c2 = buffer[c];
                if (c2 == quote) {
                    this.pos = p;
                    int len = (p - start) - 1;
                    if (builder == null) {
                        return new String(buffer, start, len);
                    }
                    builder.append(buffer, start, len);
                    return builder.toString();
                } else if (c2 == '\\') {
                    this.pos = p;
                    int len2 = (p - start) - 1;
                    if (builder == null) {
                        int estimatedLength = (len2 + 1) * 2;
                        builder = new StringBuilder(Math.max(estimatedLength, 16));
                    }
                    builder.append(buffer, start, len2);
                    builder.append(readEscapeCharacter());
                    int p2 = this.pos;
                    l = this.limit;
                    start = p2;
                    c = p2;
                } else {
                    if (c2 == '\n') {
                        this.lineNumber++;
                        this.lineStart = p;
                    }
                    c = p;
                }
            }
            if (builder == null) {
                int estimatedLength2 = (c - start) * 2;
                builder = new StringBuilder(Math.max(estimatedLength2, 16));
            }
            builder.append(buffer, start, c - start);
            this.pos = c;
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x0049, code lost:
        checkLenient();
     */
    /* JADX WARN: Code restructure failed: missing block: B:45:0x0080, code lost:
        r2 = new java.lang.String(r5.buffer, r5.pos, r1);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private java.lang.String nextUnquotedValue() throws java.io.IOException {
        /*
            r5 = this;
            r0 = 0
            r1 = 0
        L2:
            int r2 = r5.pos
            int r3 = r2 + r1
            int r4 = r5.limit
            if (r3 >= r4) goto L4d
            char[] r3 = r5.buffer
            int r2 = r2 + r1
            char r2 = r3[r2]
            r3 = 9
            if (r2 == r3) goto L4c
            r3 = 10
            if (r2 == r3) goto L4c
            r3 = 12
            if (r2 == r3) goto L4c
            r3 = 13
            if (r2 == r3) goto L4c
            r3 = 32
            if (r2 == r3) goto L4c
            r3 = 35
            if (r2 == r3) goto L49
            r3 = 44
            if (r2 == r3) goto L4c
            r3 = 47
            if (r2 == r3) goto L49
            r3 = 61
            if (r2 == r3) goto L49
            r3 = 123(0x7b, float:1.72E-43)
            if (r2 == r3) goto L4c
            r3 = 125(0x7d, float:1.75E-43)
            if (r2 == r3) goto L4c
            r3 = 58
            if (r2 == r3) goto L4c
            r3 = 59
            if (r2 == r3) goto L49
            switch(r2) {
                case 91: goto L4c;
                case 92: goto L49;
                case 93: goto L4c;
                default: goto L46;
            }
        L46:
            int r1 = r1 + 1
            goto L2
        L49:
            r5.checkLenient()
        L4c:
            goto L7e
        L4d:
            char[] r2 = r5.buffer
            int r2 = r2.length
            if (r1 >= r2) goto L5b
            int r2 = r1 + 1
            boolean r2 = r5.fillBuffer(r2)
            if (r2 == 0) goto L7e
            goto L2
        L5b:
            if (r0 != 0) goto L69
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r3 = 16
            int r3 = java.lang.Math.max(r1, r3)
            r2.<init>(r3)
            r0 = r2
        L69:
            char[] r2 = r5.buffer
            int r3 = r5.pos
            r0.append(r2, r3, r1)
            int r2 = r5.pos
            int r2 = r2 + r1
            r5.pos = r2
            r1 = 0
            r2 = 1
            boolean r2 = r5.fillBuffer(r2)
            if (r2 != 0) goto L9b
        L7e:
            if (r0 != 0) goto L8a
            java.lang.String r2 = new java.lang.String
            char[] r3 = r5.buffer
            int r4 = r5.pos
            r2.<init>(r3, r4, r1)
            goto L95
        L8a:
            char[] r2 = r5.buffer
            int r3 = r5.pos
            r0.append(r2, r3, r1)
            java.lang.String r2 = r0.toString()
        L95:
            int r3 = r5.pos
            int r3 = r3 + r1
            r5.pos = r3
            return r2
        L9b:
            goto L2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.gson.stream.JsonReader.nextUnquotedValue():java.lang.String");
    }

    private void skipQuotedValue(char quote) throws IOException {
        char[] buffer = this.buffer;
        do {
            int c = this.pos;
            int l = this.limit;
            while (c < l) {
                int p = c + 1;
                char c2 = buffer[c];
                if (c2 == quote) {
                    this.pos = p;
                    return;
                } else if (c2 == '\\') {
                    this.pos = p;
                    readEscapeCharacter();
                    int p2 = this.pos;
                    l = this.limit;
                    c = p2;
                } else {
                    if (c2 == '\n') {
                        this.lineNumber++;
                        this.lineStart = p;
                    }
                    c = p;
                }
            }
            this.pos = c;
        } while (fillBuffer(1));
        throw syntaxError("Unterminated string");
    }

    /* JADX WARN: Code restructure failed: missing block: B:33:0x0048, code lost:
        checkLenient();
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void skipUnquotedValue() throws java.io.IOException {
        /*
            r4 = this;
        L0:
            r0 = 0
        L1:
            int r1 = r4.pos
            int r2 = r1 + r0
            int r3 = r4.limit
            if (r2 >= r3) goto L51
            char[] r2 = r4.buffer
            int r1 = r1 + r0
            char r1 = r2[r1]
            r2 = 9
            if (r1 == r2) goto L4b
            r2 = 10
            if (r1 == r2) goto L4b
            r2 = 12
            if (r1 == r2) goto L4b
            r2 = 13
            if (r1 == r2) goto L4b
            r2 = 32
            if (r1 == r2) goto L4b
            r2 = 35
            if (r1 == r2) goto L48
            r2 = 44
            if (r1 == r2) goto L4b
            r2 = 47
            if (r1 == r2) goto L48
            r2 = 61
            if (r1 == r2) goto L48
            r2 = 123(0x7b, float:1.72E-43)
            if (r1 == r2) goto L4b
            r2 = 125(0x7d, float:1.75E-43)
            if (r1 == r2) goto L4b
            r2 = 58
            if (r1 == r2) goto L4b
            r2 = 59
            if (r1 == r2) goto L48
            switch(r1) {
                case 91: goto L4b;
                case 92: goto L48;
                case 93: goto L4b;
                default: goto L45;
            }
        L45:
            int r0 = r0 + 1
            goto L1
        L48:
            r4.checkLenient()
        L4b:
            int r1 = r4.pos
            int r1 = r1 + r0
            r4.pos = r1
            return
        L51:
            int r1 = r1 + r0
            r4.pos = r1
            r0 = 1
            boolean r0 = r4.fillBuffer(r0)
            if (r0 != 0) goto L5c
            return
        L5c:
            goto L0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.gson.stream.JsonReader.skipUnquotedValue():void");
    }

    public int nextInt() throws IOException {
        int p = this.peeked;
        if (p == 0) {
            p = doPeek();
        }
        if (p == 15) {
            long j = this.peekedLong;
            int result = (int) j;
            if (j != result) {
                throw new NumberFormatException("Expected an int but was " + this.peekedLong + locationString());
            }
            this.peeked = 0;
            int[] iArr = this.pathIndices;
            int i = this.stackSize - 1;
            iArr[i] = iArr[i] + 1;
            return result;
        }
        if (p == 16) {
            this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
            this.pos += this.peekedNumberLength;
        } else if (p == 8 || p == 9 || p == 10) {
            if (p == 10) {
                this.peekedString = nextUnquotedValue();
            } else {
                this.peekedString = nextQuotedValue(p == 8 ? '\'' : '\"');
            }
            try {
                int result2 = Integer.parseInt(this.peekedString);
                this.peeked = 0;
                int[] iArr2 = this.pathIndices;
                int i2 = this.stackSize - 1;
                iArr2[i2] = iArr2[i2] + 1;
                return result2;
            } catch (NumberFormatException e) {
            }
        } else {
            throw new IllegalStateException("Expected an int but was " + peek() + locationString());
        }
        this.peeked = 11;
        double asDouble = Double.parseDouble(this.peekedString);
        int result3 = (int) asDouble;
        if (result3 != asDouble) {
            throw new NumberFormatException("Expected an int but was " + this.peekedString + locationString());
        }
        this.peekedString = null;
        this.peeked = 0;
        int[] iArr3 = this.pathIndices;
        int i3 = this.stackSize - 1;
        iArr3[i3] = iArr3[i3] + 1;
        return result3;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.peeked = 0;
        this.stack[0] = 8;
        this.stackSize = 1;
        this.in.close();
    }

    public void skipValue() throws IOException {
        int count = 0;
        do {
            int p = this.peeked;
            if (p == 0) {
                p = doPeek();
            }
            if (p == 3) {
                push(1);
                count++;
            } else if (p == 1) {
                push(3);
                count++;
            } else if (p == 4) {
                this.stackSize--;
                count--;
            } else if (p == 2) {
                this.stackSize--;
                count--;
            } else if (p == 14 || p == 10) {
                skipUnquotedValue();
            } else if (p == 8 || p == 12) {
                skipQuotedValue('\'');
            } else if (p == 9 || p == 13) {
                skipQuotedValue('\"');
            } else if (p == 16) {
                this.pos += this.peekedNumberLength;
            }
            this.peeked = 0;
        } while (count != 0);
        int[] iArr = this.pathIndices;
        int i = this.stackSize;
        int i2 = i - 1;
        iArr[i2] = iArr[i2] + 1;
        this.pathNames[i - 1] = "null";
    }

    private void push(int newTop) {
        int i = this.stackSize;
        int[] iArr = this.stack;
        if (i == iArr.length) {
            int newLength = i * 2;
            this.stack = Arrays.copyOf(iArr, newLength);
            this.pathIndices = Arrays.copyOf(this.pathIndices, newLength);
            this.pathNames = (String[]) Arrays.copyOf(this.pathNames, newLength);
        }
        int[] iArr2 = this.stack;
        int i2 = this.stackSize;
        this.stackSize = i2 + 1;
        iArr2[i2] = newTop;
    }

    private boolean fillBuffer(int minimum) throws IOException {
        int i;
        char[] buffer = this.buffer;
        int i2 = this.lineStart;
        int i3 = this.pos;
        this.lineStart = i2 - i3;
        int i4 = this.limit;
        if (i4 != i3) {
            int i5 = i4 - i3;
            this.limit = i5;
            System.arraycopy(buffer, i3, buffer, 0, i5);
        } else {
            this.limit = 0;
        }
        this.pos = 0;
        do {
            Reader reader = this.in;
            int i6 = this.limit;
            int total = reader.read(buffer, i6, buffer.length - i6);
            if (total == -1) {
                return false;
            }
            int i7 = this.limit + total;
            this.limit = i7;
            if (this.lineNumber == 0 && (i = this.lineStart) == 0 && i7 > 0 && buffer[0] == 65279) {
                this.pos++;
                this.lineStart = i + 1;
                minimum++;
            }
        } while (this.limit < minimum);
        return true;
    }

    private int nextNonWhitespace(boolean throwOnEof) throws IOException {
        char[] buffer = this.buffer;
        int p = this.pos;
        int l = this.limit;
        while (true) {
            if (p == l) {
                this.pos = p;
                if (fillBuffer(1)) {
                    p = this.pos;
                    l = this.limit;
                } else if (throwOnEof) {
                    throw new EOFException("End of input" + locationString());
                } else {
                    return -1;
                }
            }
            int p2 = p + 1;
            char c = buffer[p];
            if (c == '\n') {
                this.lineNumber++;
                this.lineStart = p2;
            } else if (c != ' ' && c != '\r' && c != '\t') {
                if (c == '/') {
                    this.pos = p2;
                    if (p2 == l) {
                        this.pos = p2 - 1;
                        boolean charsLoaded = fillBuffer(2);
                        this.pos++;
                        if (!charsLoaded) {
                            return c;
                        }
                    }
                    checkLenient();
                    int p3 = this.pos;
                    char peek = buffer[p3];
                    if (peek != '*') {
                        if (peek == '/') {
                            this.pos = p3 + 1;
                            skipToEndOfLine();
                            int p4 = this.pos;
                            l = this.limit;
                            p = p4;
                        } else {
                            return c;
                        }
                    } else {
                        this.pos = p3 + 1;
                        if (skipTo("*/")) {
                            int p5 = this.pos + 2;
                            l = this.limit;
                            p = p5;
                        } else {
                            throw syntaxError("Unterminated comment");
                        }
                    }
                } else if (c == '#') {
                    this.pos = p2;
                    checkLenient();
                    skipToEndOfLine();
                    int p6 = this.pos;
                    l = this.limit;
                    p = p6;
                } else {
                    this.pos = p2;
                    return c;
                }
            }
            p = p2;
        }
    }

    private void checkLenient() throws IOException {
        if (!this.lenient) {
            throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
        }
    }

    private void skipToEndOfLine() throws IOException {
        char c;
        do {
            if (this.pos < this.limit || fillBuffer(1)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                int i2 = i + 1;
                this.pos = i2;
                c = cArr[i];
                if (c == '\n') {
                    this.lineNumber++;
                    this.lineStart = i2;
                    return;
                }
            } else {
                return;
            }
        } while (c != '\r');
    }

    private boolean skipTo(String toFind) throws IOException {
        int length = toFind.length();
        while (true) {
            if (this.pos + length <= this.limit || fillBuffer(length)) {
                char[] cArr = this.buffer;
                int i = this.pos;
                if (cArr[i] == '\n') {
                    this.lineNumber++;
                    this.lineStart = i + 1;
                } else {
                    for (int c = 0; c < length; c++) {
                        if (this.buffer[this.pos + c] != toFind.charAt(c)) {
                            break;
                        }
                    }
                    return true;
                }
                int c2 = this.pos;
                this.pos = c2 + 1;
            } else {
                return false;
            }
        }
    }

    public String toString() {
        return getClass().getSimpleName() + locationString();
    }

    String locationString() {
        int line = this.lineNumber + 1;
        int column = (this.pos - this.lineStart) + 1;
        return " at line " + line + " column " + column + " path " + getPath();
    }

    public String getPath() {
        StringBuilder result = new StringBuilder().append('$');
        int size = this.stackSize;
        for (int i = 0; i < size; i++) {
            int i2 = this.stack[i];
            if (i2 == 1 || i2 == 2) {
                result.append('[');
                result.append(this.pathIndices[i]);
                result.append(']');
            } else if (i2 == 3 || i2 == 4 || i2 == 5) {
                result.append(FilenameUtils.EXTENSION_SEPARATOR);
                String[] strArr = this.pathNames;
                if (strArr[i] != null) {
                    result.append(strArr[i]);
                }
            }
        }
        return result.toString();
    }

    private char readEscapeCharacter() throws IOException {
        int i;
        if (this.pos == this.limit && !fillBuffer(1)) {
            throw syntaxError("Unterminated escape sequence");
        }
        char[] cArr = this.buffer;
        int i2 = this.pos;
        int i3 = i2 + 1;
        this.pos = i3;
        char escaped = cArr[i2];
        if (escaped == '\n') {
            this.lineNumber++;
            this.lineStart = i3;
        } else if (escaped != '\"' && escaped != '\'' && escaped != '/' && escaped != '\\') {
            if (escaped != 'b') {
                if (escaped != 'f') {
                    if (escaped != 'n') {
                        if (escaped != 'r') {
                            if (escaped != 't') {
                                if (escaped == 'u') {
                                    if (i3 + 4 > this.limit && !fillBuffer(4)) {
                                        throw syntaxError("Unterminated escape sequence");
                                    }
                                    char result = 0;
                                    int i4 = this.pos;
                                    int end = i4 + 4;
                                    while (i4 < end) {
                                        char c = this.buffer[i4];
                                        char result2 = (char) (result << 4);
                                        if (c >= '0' && c <= '9') {
                                            i = c - '0';
                                        } else if (c >= 'a' && c <= 'f') {
                                            i = (c - 'a') + 10;
                                        } else if (c >= 'A' && c <= 'F') {
                                            i = (c - 'A') + 10;
                                        } else {
                                            throw new NumberFormatException("\\u" + new String(this.buffer, this.pos, 4));
                                        }
                                        result = (char) (i + result2);
                                        i4++;
                                    }
                                    this.pos += 4;
                                    return result;
                                }
                                throw syntaxError("Invalid escape sequence");
                            }
                            return '\t';
                        }
                        return '\r';
                    }
                    return '\n';
                }
                return '\f';
            }
            return '\b';
        }
        return escaped;
    }

    private IOException syntaxError(String message) throws IOException {
        throw new MalformedJsonException(message + locationString());
    }

    private void consumeNonExecutePrefix() throws IOException {
        nextNonWhitespace(true);
        int i = this.pos - 1;
        this.pos = i;
        char[] cArr = NON_EXECUTE_PREFIX;
        if (i + cArr.length > this.limit && !fillBuffer(cArr.length)) {
            return;
        }
        int i2 = 0;
        while (true) {
            char[] cArr2 = NON_EXECUTE_PREFIX;
            if (i2 < cArr2.length) {
                if (this.buffer[this.pos + i2] == cArr2[i2]) {
                    i2++;
                } else {
                    return;
                }
            } else {
                int i3 = this.pos;
                this.pos = i3 + cArr2.length;
                return;
            }
        }
    }
}