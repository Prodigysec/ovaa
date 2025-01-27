package okhttp3.internal.publicsuffix;

import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.internal.Util;
import okhttp3.internal.platform.Platform;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import org.apache.commons.io.FilenameUtils;

/* loaded from: classes.dex */
public final class PublicSuffixDatabase {
    private static final byte EXCEPTION_MARKER = 33;
    public static final String PUBLIC_SUFFIX_RESOURCE = "publicsuffixes.gz";
    private byte[] publicSuffixExceptionListBytes;
    private byte[] publicSuffixListBytes;
    private static final byte[] WILDCARD_LABEL = {42};
    private static final String[] EMPTY_RULE = new String[0];
    private static final String[] PREVAILING_RULE = {"*"};
    private static final PublicSuffixDatabase instance = new PublicSuffixDatabase();
    private final AtomicBoolean listRead = new AtomicBoolean(false);
    private final CountDownLatch readCompleteLatch = new CountDownLatch(1);

    public static PublicSuffixDatabase get() {
        return instance;
    }

    public String getEffectiveTldPlusOne(String domain) {
        int firstLabelOffset;
        if (domain == null) {
            throw new NullPointerException("domain == null");
        }
        String unicodeDomain = IDN.toUnicode(domain);
        String[] domainLabels = unicodeDomain.split("\\.");
        String[] rule = findMatchingRule(domainLabels);
        if (domainLabels.length == rule.length && rule[0].charAt(0) != '!') {
            return null;
        }
        if (rule[0].charAt(0) == '!') {
            firstLabelOffset = domainLabels.length - rule.length;
        } else {
            int firstLabelOffset2 = domainLabels.length;
            firstLabelOffset = firstLabelOffset2 - (rule.length + 1);
        }
        StringBuilder effectiveTldPlusOne = new StringBuilder();
        String[] punycodeLabels = domain.split("\\.");
        for (int i = firstLabelOffset; i < punycodeLabels.length; i++) {
            effectiveTldPlusOne.append(punycodeLabels[i]);
            effectiveTldPlusOne.append(FilenameUtils.EXTENSION_SEPARATOR);
        }
        int i2 = effectiveTldPlusOne.length();
        effectiveTldPlusOne.deleteCharAt(i2 - 1);
        return effectiveTldPlusOne.toString();
    }

    private String[] findMatchingRule(String[] domainLabels) {
        String[] exactRuleLabels;
        String[] wildcardRuleLabels;
        if (!this.listRead.get() && this.listRead.compareAndSet(false, true)) {
            readTheList();
        } else {
            try {
                this.readCompleteLatch.await();
            } catch (InterruptedException e) {
            }
        }
        synchronized (this) {
            if (this.publicSuffixListBytes == null) {
                throw new IllegalStateException("Unable to load publicsuffixes.gz resource from the classpath.");
            }
        }
        byte[][] domainLabelsUtf8Bytes = new byte[domainLabels.length];
        for (int i = 0; i < domainLabels.length; i++) {
            domainLabelsUtf8Bytes[i] = domainLabels[i].getBytes(Util.UTF_8);
        }
        String exactMatch = null;
        int i2 = 0;
        while (true) {
            if (i2 >= domainLabelsUtf8Bytes.length) {
                break;
            }
            String rule = binarySearchBytes(this.publicSuffixListBytes, domainLabelsUtf8Bytes, i2);
            if (rule == null) {
                i2++;
            } else {
                exactMatch = rule;
                break;
            }
        }
        String wildcardMatch = null;
        if (domainLabelsUtf8Bytes.length > 1) {
            byte[][] labelsWithWildcard = (byte[][]) domainLabelsUtf8Bytes.clone();
            int labelIndex = 0;
            while (true) {
                if (labelIndex >= labelsWithWildcard.length - 1) {
                    break;
                }
                labelsWithWildcard[labelIndex] = WILDCARD_LABEL;
                String rule2 = binarySearchBytes(this.publicSuffixListBytes, labelsWithWildcard, labelIndex);
                if (rule2 == null) {
                    labelIndex++;
                } else {
                    wildcardMatch = rule2;
                    break;
                }
            }
        }
        String exception = null;
        if (wildcardMatch != null) {
            int labelIndex2 = 0;
            while (true) {
                if (labelIndex2 >= domainLabelsUtf8Bytes.length - 1) {
                    break;
                }
                String rule3 = binarySearchBytes(this.publicSuffixExceptionListBytes, domainLabelsUtf8Bytes, labelIndex2);
                if (rule3 == null) {
                    labelIndex2++;
                } else {
                    exception = rule3;
                    break;
                }
            }
        }
        if (exception != null) {
            return ("!" + exception).split("\\.");
        } else if (exactMatch == null && wildcardMatch == null) {
            return PREVAILING_RULE;
        } else {
            if (exactMatch != null) {
                exactRuleLabels = exactMatch.split("\\.");
            } else {
                exactRuleLabels = EMPTY_RULE;
            }
            if (wildcardMatch != null) {
                wildcardRuleLabels = wildcardMatch.split("\\.");
            } else {
                wildcardRuleLabels = EMPTY_RULE;
            }
            if (exactRuleLabels.length > wildcardRuleLabels.length) {
                return exactRuleLabels;
            }
            return wildcardRuleLabels;
        }
    }

    private static String binarySearchBytes(byte[] bytesToSearch, byte[][] labels, int labelIndex) {
        int byte0;
        int compareResult;
        int low;
        int low2;
        int low3 = 0;
        int high = bytesToSearch.length;
        while (low3 < high) {
            int mid = (low3 + high) / 2;
            while (mid > -1 && bytesToSearch[mid] != 10) {
                mid--;
            }
            int mid2 = mid + 1;
            int end = 1;
            while (bytesToSearch[mid2 + end] != 10) {
                end++;
            }
            int publicSuffixLength = (mid2 + end) - mid2;
            int currentLabelIndex = labelIndex;
            int currentLabelByteIndex = 0;
            int publicSuffixByteIndex = 0;
            boolean expectDot = false;
            while (true) {
                if (expectDot) {
                    byte0 = 46;
                    expectDot = false;
                } else {
                    byte0 = labels[currentLabelIndex][currentLabelByteIndex] & 255;
                }
                int byte1 = bytesToSearch[mid2 + publicSuffixByteIndex] & 255;
                compareResult = byte0 - byte1;
                if (compareResult == 0) {
                    publicSuffixByteIndex++;
                    currentLabelByteIndex++;
                    if (publicSuffixByteIndex == publicSuffixLength) {
                        break;
                    }
                    if (labels[currentLabelIndex].length != currentLabelByteIndex) {
                        low2 = low3;
                    } else if (currentLabelIndex == labels.length - 1) {
                        break;
                    } else {
                        low2 = low3;
                        currentLabelIndex++;
                        expectDot = true;
                        currentLabelByteIndex = -1;
                    }
                    low3 = low2;
                } else {
                    break;
                }
            }
            if (compareResult < 0) {
                high = mid2 - 1;
            } else if (compareResult > 0) {
                low3 = mid2 + end + 1;
            } else {
                int low4 = publicSuffixLength - publicSuffixByteIndex;
                int labelBytesLeft = labels[currentLabelIndex].length - currentLabelByteIndex;
                int i = currentLabelIndex + 1;
                while (true) {
                    low = low3;
                    int low5 = labels.length;
                    if (i >= low5) {
                        break;
                    }
                    labelBytesLeft += labels[i].length;
                    i++;
                    low3 = low;
                }
                if (labelBytesLeft < low4) {
                    high = mid2 - 1;
                    low3 = low;
                } else if (labelBytesLeft <= low4) {
                    String match = new String(bytesToSearch, mid2, publicSuffixLength, Util.UTF_8);
                    return match;
                } else {
                    low3 = mid2 + end + 1;
                }
            }
        }
        return null;
    }

    private void readTheList() {
        byte[] publicSuffixListBytes = null;
        byte[] publicSuffixExceptionListBytes = null;
        InputStream is = PublicSuffixDatabase.class.getClassLoader().getResourceAsStream(PUBLIC_SUFFIX_RESOURCE);
        if (is != null) {
            BufferedSource bufferedSource = Okio.buffer(new GzipSource(Okio.source(is)));
            try {
                try {
                    int totalBytes = bufferedSource.readInt();
                    publicSuffixListBytes = new byte[totalBytes];
                    bufferedSource.readFully(publicSuffixListBytes);
                    int totalExceptionBytes = bufferedSource.readInt();
                    publicSuffixExceptionListBytes = new byte[totalExceptionBytes];
                    bufferedSource.readFully(publicSuffixExceptionListBytes);
                } catch (IOException e) {
                    Platform.get().log(5, "Failed to read public suffix list", e);
                    publicSuffixListBytes = null;
                    publicSuffixExceptionListBytes = null;
                }
            } finally {
                Util.closeQuietly(bufferedSource);
            }
        }
        synchronized (this) {
            this.publicSuffixListBytes = publicSuffixListBytes;
            this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        }
        this.readCompleteLatch.countDown();
    }

    void setListBytes(byte[] publicSuffixListBytes, byte[] publicSuffixExceptionListBytes) {
        this.publicSuffixListBytes = publicSuffixListBytes;
        this.publicSuffixExceptionListBytes = publicSuffixExceptionListBytes;
        this.listRead.set(true);
        this.readCompleteLatch.countDown();
    }
}