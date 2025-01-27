package org.apache.commons.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.NullOutputStream;
/* loaded from: classes.dex */
public class FileUtils {
    public static final File[] EMPTY_FILE_ARRAY;
    private static final long FILE_COPY_BUFFER_SIZE = 31457280;
    public static final long ONE_EB = 1152921504606846976L;
    public static final BigInteger ONE_EB_BI;
    public static final long ONE_GB = 1073741824;
    public static final BigInteger ONE_GB_BI;
    public static final long ONE_KB = 1024;
    public static final BigInteger ONE_KB_BI;
    public static final long ONE_MB = 1048576;
    public static final BigInteger ONE_MB_BI;
    public static final long ONE_PB = 1125899906842624L;
    public static final BigInteger ONE_PB_BI;
    public static final long ONE_TB = 1099511627776L;
    public static final BigInteger ONE_TB_BI;
    public static final BigInteger ONE_YB;
    public static final BigInteger ONE_ZB;

    static {
        BigInteger valueOf = BigInteger.valueOf(ONE_KB);
        ONE_KB_BI = valueOf;
        BigInteger multiply = valueOf.multiply(valueOf);
        ONE_MB_BI = multiply;
        BigInteger multiply2 = ONE_KB_BI.multiply(multiply);
        ONE_GB_BI = multiply2;
        BigInteger multiply3 = ONE_KB_BI.multiply(multiply2);
        ONE_TB_BI = multiply3;
        BigInteger multiply4 = ONE_KB_BI.multiply(multiply3);
        ONE_PB_BI = multiply4;
        ONE_EB_BI = ONE_KB_BI.multiply(multiply4);
        BigInteger multiply5 = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));
        ONE_ZB = multiply5;
        ONE_YB = ONE_KB_BI.multiply(multiply5);
        EMPTY_FILE_ARRAY = new File[0];
    }

    public static File getFile(File directory, String... names) {
        if (directory == null) {
            throw new NullPointerException("directory must not be null");
        }
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = directory;
        for (String name : names) {
            file = new File(file, name);
        }
        return file;
    }

    public static File getFile(String... names) {
        File file;
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file2 = null;
        for (String name : names) {
            if (file2 == null) {
                file = new File(name);
            } else {
                file = new File(file2, name);
            }
            file2 = file;
        }
        return file2;
    }

    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            } else if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            } else {
                return new FileInputStream(file);
            }
        }
        throw new FileNotFoundException("File '" + file + "' does not exist");
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            } else if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }
        return new FileOutputStream(file, append);
    }

    public static String byteCountToDisplaySize(BigInteger size) {
        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            String displaySize = String.valueOf(size.divide(ONE_EB_BI)) + " EB";
            return displaySize;
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            String displaySize2 = String.valueOf(size.divide(ONE_PB_BI)) + " PB";
            return displaySize2;
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            String displaySize3 = String.valueOf(size.divide(ONE_TB_BI)) + " TB";
            return displaySize3;
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            String displaySize4 = String.valueOf(size.divide(ONE_GB_BI)) + " GB";
            return displaySize4;
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            String displaySize5 = String.valueOf(size.divide(ONE_MB_BI)) + " MB";
            return displaySize5;
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            String displaySize6 = String.valueOf(size.divide(ONE_KB_BI)) + " KB";
            return displaySize6;
        } else {
            String displaySize7 = String.valueOf(size) + " bytes";
            return displaySize7;
        }
    }

    public static String byteCountToDisplaySize(long size) {
        return byteCountToDisplaySize(BigInteger.valueOf(size));
    }

    public static void touch(File file) throws IOException {
        if (!file.exists()) {
            openOutputStream(file).close();
        }
        boolean success = file.setLastModified(System.currentTimeMillis());
        if (!success) {
            throw new IOException("Unable to set the last modification time for " + file);
        }
    }

    public static File[] convertFileCollectionToFileArray(Collection<File> files) {
        return (File[]) files.toArray(new File[files.size()]);
    }

    private static void innerListFiles(Collection<File> files, File directory, IOFileFilter filter, boolean includeSubDirectories) {
        File[] found = directory.listFiles((FileFilter) filter);
        if (found != null) {
            for (File file : found) {
                if (file.isDirectory()) {
                    if (includeSubDirectories) {
                        files.add(file);
                    }
                    innerListFiles(files, file, filter, includeSubDirectories);
                } else {
                    files.add(file);
                }
            }
        }
    }

    public static Collection<File> listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        validateListFilesParameters(directory, fileFilter);
        IOFileFilter effFileFilter = setUpEffectiveFileFilter(fileFilter);
        IOFileFilter effDirFilter = setUpEffectiveDirFilter(dirFilter);
        Collection<File> files = new LinkedList<>();
        innerListFiles(files, directory, FileFilterUtils.or(effFileFilter, effDirFilter), false);
        return files;
    }

    private static void validateListFilesParameters(File directory, IOFileFilter fileFilter) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter 'directory' is not a directory: " + directory);
        } else if (fileFilter == null) {
            throw new NullPointerException("Parameter 'fileFilter' is null");
        }
    }

    private static IOFileFilter setUpEffectiveFileFilter(IOFileFilter fileFilter) {
        return FileFilterUtils.and(fileFilter, FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
    }

    private static IOFileFilter setUpEffectiveDirFilter(IOFileFilter dirFilter) {
        return dirFilter == null ? FalseFileFilter.INSTANCE : FileFilterUtils.and(dirFilter, DirectoryFileFilter.INSTANCE);
    }

    public static Collection<File> listFilesAndDirs(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        validateListFilesParameters(directory, fileFilter);
        IOFileFilter effFileFilter = setUpEffectiveFileFilter(fileFilter);
        IOFileFilter effDirFilter = setUpEffectiveDirFilter(dirFilter);
        Collection<File> files = new LinkedList<>();
        if (directory.isDirectory()) {
            files.add(directory);
        }
        innerListFiles(files, directory, FileFilterUtils.or(effFileFilter, effDirFilter), true);
        return files;
    }

    public static Iterator<File> iterateFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        return listFiles(directory, fileFilter, dirFilter).iterator();
    }

    public static Iterator<File> iterateFilesAndDirs(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        return listFilesAndDirs(directory, fileFilter, dirFilter).iterator();
    }

    private static String[] toSuffixes(String[] extensions) {
        String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    public static Collection<File> listFiles(File directory, String[] extensions, boolean recursive) {
        IOFileFilter filter;
        if (extensions == null) {
            filter = TrueFileFilter.INSTANCE;
        } else {
            String[] suffixes = toSuffixes(extensions);
            filter = new SuffixFileFilter(suffixes);
        }
        return listFiles(directory, filter, recursive ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE);
    }

    public static Iterator<File> iterateFiles(File directory, String[] extensions, boolean recursive) {
        return listFiles(directory, extensions, recursive).iterator();
    }

    public static boolean contentEquals(File file1, File file2) throws IOException {
        boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }
        if (!file1Exists) {
            return true;
        }
        if (file1.isDirectory() || file2.isDirectory()) {
            throw new IOException("Can't compare directories, only files");
        }
        if (file1.length() != file2.length()) {
            return false;
        }
        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            return true;
        }
        InputStream input1 = new FileInputStream(file1);
        try {
            InputStream input2 = new FileInputStream(file2);
            boolean contentEquals = IOUtils.contentEquals(input1, input2);
            input2.close();
            input1.close();
            return contentEquals;
        } finally {
        }
    }

    public static boolean contentEqualsIgnoreEOL(File file1, File file2, String charsetName) throws IOException {
        boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }
        if (!file1Exists) {
            return true;
        }
        if (file1.isDirectory() || file2.isDirectory()) {
            throw new IOException("Can't compare directories, only files");
        }
        if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            return true;
        }
        Reader input1 = charsetName == null ? new InputStreamReader(new FileInputStream(file1), Charset.defaultCharset()) : new InputStreamReader(new FileInputStream(file1), charsetName);
        try {
            Reader input2 = charsetName == null ? new InputStreamReader(new FileInputStream(file2), Charset.defaultCharset()) : new InputStreamReader(new FileInputStream(file2), charsetName);
            boolean contentEqualsIgnoreEOL = IOUtils.contentEqualsIgnoreEOL(input1, input2);
            input2.close();
            input1.close();
            return contentEqualsIgnoreEOL;
        } finally {
        }
    }

    public static File toFile(URL url) {
        if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        String filename = url.getFile().replace(IOUtils.DIR_SEPARATOR_UNIX, File.separatorChar);
        return new File(decodeUrl(filename));
    }

    static String decodeUrl(String url) {
        if (url == null || url.indexOf(37) < 0) {
            return url;
        }
        int n = url.length();
        StringBuilder buffer = new StringBuilder();
        ByteBuffer bytes = ByteBuffer.allocate(n);
        int i = 0;
        while (i < n) {
            if (url.charAt(i) == '%') {
                do {
                    try {
                        byte octet = (byte) Integer.parseInt(url.substring(i + 1, i + 3), 16);
                        bytes.put(octet);
                        i += 3;
                        if (i >= n) {
                            break;
                        }
                    } catch (RuntimeException e) {
                        if (bytes.position() > 0) {
                            bytes.flip();
                            buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                            bytes.clear();
                        }
                    } catch (Throwable th) {
                        if (bytes.position() > 0) {
                            bytes.flip();
                            buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                            bytes.clear();
                        }
                        throw th;
                    }
                } while (url.charAt(i) == '%');
                if (bytes.position() > 0) {
                    bytes.flip();
                    buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                    bytes.clear();
                }
            }
            buffer.append(url.charAt(i));
            i++;
        }
        String decoded = buffer.toString();
        return decoded;
    }

    public static File[] toFiles(URL[] urls) {
        if (urls == null || urls.length == 0) {
            return EMPTY_FILE_ARRAY;
        }
        File[] files = new File[urls.length];
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if (url != null) {
                if (!url.getProtocol().equals("file")) {
                    throw new IllegalArgumentException("URL could not be converted to a File: " + url);
                }
                files[i] = toFile(url);
            }
        }
        return files;
    }

    public static URL[] toURLs(File[] files) throws IOException {
        URL[] urls = new URL[files.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }

    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
        copyFileToDirectory(srcFile, destDir, true);
    }

    public static void copyFileToDirectory(File srcFile, File destDir, boolean preserveFileDate) throws IOException {
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && !destDir.isDirectory()) {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile, preserveFileDate);
    }

    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcFile, destFile);
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        } else if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        } else {
            File parentFile = destFile.getParentFile();
            if (parentFile != null && !parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile + "' directory cannot be created");
            } else if (destFile.exists() && !destFile.canWrite()) {
                throw new IOException("Destination '" + destFile + "' exists but is read-only");
            } else {
                doCopyFile(srcFile, destFile, preserveFileDate);
            }
        }
    }

    public static long copyFile(File input, OutputStream output) throws IOException {
        FileInputStream fis = new FileInputStream(input);
        try {
            long copyLarge = IOUtils.copyLarge(fis, output);
            fis.close();
            return copyLarge;
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

    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }
        FileInputStream fis = new FileInputStream(srcFile);
        try {
            FileChannel input = fis.getChannel();
            FileOutputStream fos = new FileOutputStream(destFile);
            FileChannel output = fos.getChannel();
            try {
                long size = input.size();
                long pos = 0;
                while (pos < size) {
                    long remain = size - pos;
                    long count = remain > FILE_COPY_BUFFER_SIZE ? 31457280L : remain;
                    long bytesCopied = output.transferFrom(input, pos, count);
                    if (bytesCopied == 0) {
                        break;
                    }
                    pos += bytesCopied;
                }
                if (output != null) {
                    output.close();
                }
                fos.close();
                if (input != null) {
                    input.close();
                }
                fis.close();
                long srcLen = srcFile.length();
                long dstLen = destFile.length();
                if (srcLen == dstLen) {
                    if (preserveFileDate) {
                        destFile.setLastModified(srcFile.lastModified());
                        return;
                    }
                    return;
                }
                throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    }
                    throw th2;
                }
            }
        } catch (Throwable th4) {
            try {
                throw th4;
            } catch (Throwable th5) {
                try {
                    fis.close();
                } catch (Throwable th6) {
                    th4.addSuppressed(th6);
                }
                throw th5;
            }
        }
    }

    public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (srcDir.exists() && !srcDir.isDirectory()) {
            throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
        } else if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        } else {
            if (destDir.exists() && !destDir.isDirectory()) {
                throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
            }
            copyDirectory(srcDir, new File(destDir, srcDir.getName()), true);
        }
    }

    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    public static void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }

    public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcDir, destDir);
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        } else if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        } else {
            List<String> exclusionList = null;
            if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
                File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
                if (srcFiles != null && srcFiles.length > 0) {
                    exclusionList = new ArrayList<>(srcFiles.length);
                    for (File srcFile : srcFiles) {
                        File copiedFile = new File(destDir, srcFile.getName());
                        exclusionList.add(copiedFile.getCanonicalPath());
                    }
                }
            }
            doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
        }
    }

    private static void checkFileRequirements(File src, File dest) throws FileNotFoundException {
        if (src == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (dest == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!src.exists()) {
            throw new FileNotFoundException("Source '" + src + "' does not exist");
        }
    }

    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate, List<String> exclusionList) throws IOException {
        File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (srcFiles == null) {
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else if (!destDir.mkdirs() && !destDir.isDirectory()) {
            throw new IOException("Destination '" + destDir + "' directory cannot be created");
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (File srcFile : srcFiles) {
            File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(srcFile, dstFile, preserveFileDate);
                }
            }
        }
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

    public static void copyURLToFile(URL source, File destination) throws IOException {
        copyInputStreamToFile(source.openStream(), destination);
    }

    public static void copyURLToFile(URL source, File destination, int connectionTimeout, int readTimeout) throws IOException {
        URLConnection connection = source.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        copyInputStreamToFile(connection.getInputStream(), destination);
    }

    public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
        try {
            copyToFile(source, destination);
            if (source != null) {
                source.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (source != null) {
                    try {
                        source.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static void copyToFile(InputStream source, File destination) throws IOException {
        try {
            OutputStream out = openOutputStream(destination);
            IOUtils.copy(source, out);
            if (out != null) {
                out.close();
            }
            if (source != null) {
                source.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (source != null) {
                    try {
                        source.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static void copyToDirectory(File src, File destDir) throws IOException {
        if (src == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (src.isFile()) {
            copyFileToDirectory(src, destDir);
        } else if (src.isDirectory()) {
            copyDirectoryToDirectory(src, destDir);
        } else {
            throw new IOException("The source " + src + " does not exist");
        }
    }

    public static void copyToDirectory(Iterable<File> srcs, File destDir) throws IOException {
        if (srcs == null) {
            throw new NullPointerException("Sources must not be null");
        }
        for (File src : srcs) {
            copyFileToDirectory(src, destDir);
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }
        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (Exception e) {
        }
        try {
            return file.delete();
        } catch (Exception e2) {
            return false;
        }
    }

    public static boolean directoryContains(File directory, File child) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Directory must not be null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        } else if (child == null || !directory.exists() || !child.exists()) {
            return false;
        } else {
            String canonicalParent = directory.getCanonicalPath();
            String canonicalChild = child.getCanonicalPath();
            return FilenameUtils.directoryContains(canonicalParent, canonicalChild);
        }
    }

    public static void cleanDirectory(File directory) throws IOException {
        File[] files = verifiedListFiles(directory);
        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    private static File[] verifiedListFiles(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        } else if (!directory.isDirectory()) {
            String message2 = directory + " is not a directory";
            throw new IllegalArgumentException(message2);
        } else {
            File[] files = directory.listFiles();
            if (files == null) {
                throw new IOException("Failed to list contents of " + directory);
            }
            return files;
        }
    }

    public static boolean waitFor(File file, int seconds) {
        long finishAt = System.currentTimeMillis() + (seconds * 1000);
        boolean wasInterrupted = false;
        while (!file.exists()) {
            try {
                long remaining = finishAt - System.currentTimeMillis();
                if (remaining >= 0) {
                    try {
                        Thread.sleep(Math.min(100L, remaining));
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                    } catch (Exception e2) {
                    }
                } else {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                    return false;
                }
            } finally {
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static String readFileToString(File file, Charset encoding) throws IOException {
        InputStream in = openInputStream(file);
        try {
            String iOUtils = IOUtils.toString(in, Charsets.toCharset(encoding));
            if (in != null) {
                in.close();
            }
            return iOUtils;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static String readFileToString(File file, String encoding) throws IOException {
        return readFileToString(file, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static String readFileToString(File file) throws IOException {
        return readFileToString(file, Charset.defaultCharset());
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = openInputStream(file);
        try {
            long fileLength = file.length();
            byte[] byteArray = fileLength > 0 ? IOUtils.toByteArray(in, fileLength) : IOUtils.toByteArray(in);
            if (in != null) {
                in.close();
            }
            return byteArray;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static List<String> readLines(File file, Charset encoding) throws IOException {
        InputStream in = openInputStream(file);
        try {
            List<String> readLines = IOUtils.readLines(in, Charsets.toCharset(encoding));
            if (in != null) {
                in.close();
            }
            return readLines;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static List<String> readLines(File file, String encoding) throws IOException {
        return readLines(file, Charsets.toCharset(encoding));
    }

    @Deprecated
    public static List<String> readLines(File file) throws IOException {
        return readLines(file, Charset.defaultCharset());
    }

    public static LineIterator lineIterator(File file, String encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return IOUtils.lineIterator(in, encoding);
        } catch (IOException | RuntimeException ex) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ex.addSuppressed(e);
                }
            }
            throw ex;
        }
    }

    public static LineIterator lineIterator(File file) throws IOException {
        return lineIterator(file, null);
    }

    public static void writeStringToFile(File file, String data, Charset encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    public static void writeStringToFile(File file, String data, String encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    public static void writeStringToFile(File file, String data, Charset encoding, boolean append) throws IOException {
        OutputStream out = openOutputStream(file, append);
        try {
            IOUtils.write(data, out, encoding);
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static void writeStringToFile(File file, String data, String encoding, boolean append) throws IOException {
        writeStringToFile(file, data, Charsets.toCharset(encoding), append);
    }

    @Deprecated
    public static void writeStringToFile(File file, String data) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), false);
    }

    @Deprecated
    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), append);
    }

    @Deprecated
    public static void write(File file, CharSequence data) throws IOException {
        write(file, data, Charset.defaultCharset(), false);
    }

    @Deprecated
    public static void write(File file, CharSequence data, boolean append) throws IOException {
        write(file, data, Charset.defaultCharset(), append);
    }

    public static void write(File file, CharSequence data, Charset encoding) throws IOException {
        write(file, data, encoding, false);
    }

    public static void write(File file, CharSequence data, String encoding) throws IOException {
        write(file, data, encoding, false);
    }

    public static void write(File file, CharSequence data, Charset encoding, boolean append) throws IOException {
        String str = data == null ? null : data.toString();
        writeStringToFile(file, str, encoding, append);
    }

    public static void write(File file, CharSequence data, String encoding, boolean append) throws IOException {
        write(file, data, Charsets.toCharset(encoding), append);
    }

    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        writeByteArrayToFile(file, data, 0, data.length, append);
    }

    public static void writeByteArrayToFile(File file, byte[] data, int off, int len) throws IOException {
        writeByteArrayToFile(file, data, off, len, false);
    }

    public static void writeByteArrayToFile(File file, byte[] data, int off, int len, boolean append) throws IOException {
        OutputStream out = openOutputStream(file, append);
        try {
            out.write(data, off, len);
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                }
                throw th2;
            }
        }
    }

    public static void writeLines(File file, String encoding, Collection<?> lines) throws IOException {
        writeLines(file, encoding, lines, null, false);
    }

    public static void writeLines(File file, String encoding, Collection<?> lines, boolean append) throws IOException {
        writeLines(file, encoding, lines, null, append);
    }

    public static void writeLines(File file, Collection<?> lines) throws IOException {
        writeLines(file, null, lines, null, false);
    }

    public static void writeLines(File file, Collection<?> lines, boolean append) throws IOException {
        writeLines(file, null, lines, null, append);
    }

    public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding) throws IOException {
        writeLines(file, encoding, lines, lineEnding, false);
    }

    public static void writeLines(File file, String encoding, Collection<?> lines, String lineEnding, boolean append) throws IOException {
        OutputStream out = new BufferedOutputStream(openOutputStream(file, append));
        try {
            IOUtils.writeLines(lines, lineEnding, out, encoding);
            out.close();
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    out.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    public static void writeLines(File file, Collection<?> lines, String lineEnding) throws IOException {
        writeLines(file, null, lines, lineEnding, false);
    }

    public static void writeLines(File file, Collection<?> lines, String lineEnding, boolean append) throws IOException {
        writeLines(file, null, lines, lineEnding, append);
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
            return;
        }
        boolean filePresent = file.exists();
        if (!file.delete()) {
            if (!filePresent) {
                throw new FileNotFoundException("File does not exist: " + file);
            }
            String message = "Unable to delete file: " + file;
            throw new IOException(message);
        }
    }

    public static void forceDeleteOnExit(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectoryOnExit(file);
        } else {
            file.deleteOnExit();
        }
    }

    private static void deleteDirectoryOnExit(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }
        directory.deleteOnExit();
        if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
        }
    }

    private static void cleanDirectoryOnExit(File directory) throws IOException {
        File[] files = verifiedListFiles(directory);
        IOException exception = null;
        for (File file : files) {
            try {
                forceDeleteOnExit(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message = "File " + directory + " exists and is not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else if (!directory.mkdirs() && !directory.isDirectory()) {
            String message2 = "Unable to create directory " + directory;
            throw new IOException(message2);
        }
    }

    public static void forceMkdirParent(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent == null) {
            return;
        }
        forceMkdir(parent);
    }

    public static long sizeOf(File file) {
        if (!file.exists()) {
            String message = file + " does not exist";
            throw new IllegalArgumentException(message);
        } else if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        } else {
            return file.length();
        }
    }

    public static BigInteger sizeOfAsBigInteger(File file) {
        if (!file.exists()) {
            String message = file + " does not exist";
            throw new IllegalArgumentException(message);
        } else if (file.isDirectory()) {
            return sizeOfDirectoryBig0(file);
        } else {
            return BigInteger.valueOf(file.length());
        }
    }

    public static long sizeOfDirectory(File directory) {
        checkDirectory(directory);
        return sizeOfDirectory0(directory);
    }

    private static long sizeOfDirectory0(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return 0L;
        }
        long size = 0;
        for (File file : files) {
            try {
                if (!isSymlink(file)) {
                    size += sizeOf0(file);
                    if (size < 0) {
                        break;
                    }
                } else {
                    continue;
                }
            } catch (IOException e) {
            }
        }
        return size;
    }

    private static long sizeOf0(File file) {
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        return file.length();
    }

    public static BigInteger sizeOfDirectoryAsBigInteger(File directory) {
        checkDirectory(directory);
        return sizeOfDirectoryBig0(directory);
    }

    private static BigInteger sizeOfDirectoryBig0(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return BigInteger.ZERO;
        }
        BigInteger size = BigInteger.ZERO;
        for (File file : files) {
            try {
                if (!isSymlink(file)) {
                    size = size.add(sizeOfBig0(file));
                }
            } catch (IOException e) {
            }
        }
        return size;
    }

    private static BigInteger sizeOfBig0(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            return sizeOfDirectoryBig0(fileOrDir);
        }
        return BigInteger.valueOf(fileOrDir.length());
    }

    private static void checkDirectory(File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
    }

    public static boolean isFileNewer(File file, File reference) {
        if (reference == null) {
            throw new IllegalArgumentException("No specified reference file");
        }
        if (!reference.exists()) {
            throw new IllegalArgumentException("The reference file '" + reference + "' doesn't exist");
        }
        return isFileNewer(file, reference.lastModified());
    }

    public static boolean isFileNewer(File file, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("No specified date");
        }
        return isFileNewer(file, date.getTime());
    }

    public static boolean isFileNewer(File file, long timeMillis) {
        if (file != null) {
            return file.exists() && file.lastModified() > timeMillis;
        }
        throw new IllegalArgumentException("No specified file");
    }

    public static boolean isFileOlder(File file, File reference) {
        if (reference == null) {
            throw new IllegalArgumentException("No specified reference file");
        }
        if (!reference.exists()) {
            throw new IllegalArgumentException("The reference file '" + reference + "' doesn't exist");
        }
        return isFileOlder(file, reference.lastModified());
    }

    public static boolean isFileOlder(File file, Date date) {
        if (date == null) {
            throw new IllegalArgumentException("No specified date");
        }
        return isFileOlder(file, date.getTime());
    }

    public static boolean isFileOlder(File file, long timeMillis) {
        if (file != null) {
            return file.exists() && file.lastModified() < timeMillis;
        }
        throw new IllegalArgumentException("No specified file");
    }

    public static long checksumCRC32(File file) throws IOException {
        CRC32 crc = new CRC32();
        checksum(file, crc);
        return crc.getValue();
    }

    public static Checksum checksum(File file, Checksum checksum) throws IOException {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Checksums can't be computed on directories");
        }
        InputStream in = new CheckedInputStream(new FileInputStream(file), checksum);
        try {
            IOUtils.copy(in, new NullOutputStream());
            in.close();
            return checksum;
        } catch (Throwable th) {
            try {
                throw th;
            } catch (Throwable th2) {
                try {
                    in.close();
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
                throw th2;
            }
        }
    }

    public static void moveDirectory(File srcDir, File destDir) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        } else if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' is not a directory");
        } else if (destDir.exists()) {
            throw new FileExistsException("Destination '" + destDir + "' already exists");
        } else {
            boolean rename = srcDir.renameTo(destDir);
            if (!rename) {
                String canonicalPath = destDir.getCanonicalPath();
                if (canonicalPath.startsWith(srcDir.getCanonicalPath() + File.separator)) {
                    throw new IOException("Cannot move directory: " + srcDir + " to a subdirectory of itself: " + destDir);
                }
                copyDirectory(srcDir, destDir);
                deleteDirectory(srcDir);
                if (srcDir.exists()) {
                    throw new IOException("Failed to delete original directory '" + srcDir + "' after copy to '" + destDir + "'");
                }
            }
        }
    }

    public static void moveDirectoryToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
        if (src == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination directory must not be null");
        }
        if (!destDir.exists() && createDestDir) {
            destDir.mkdirs();
        }
        if (!destDir.exists()) {
            throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist [createDestDir=" + createDestDir + "]");
        } else if (!destDir.isDirectory()) {
            throw new IOException("Destination '" + destDir + "' is not a directory");
        } else {
            moveDirectory(src, new File(destDir, src.getName()));
        }
    }

    public static void moveFile(File srcFile, File destFile) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        } else if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' is a directory");
        } else if (destFile.exists()) {
            throw new FileExistsException("Destination '" + destFile + "' already exists");
        } else if (destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' is a directory");
        } else {
            boolean rename = srcFile.renameTo(destFile);
            if (!rename) {
                copyFile(srcFile, destFile);
                if (!srcFile.delete()) {
                    deleteQuietly(destFile);
                    throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
                }
            }
        }
    }

    public static void moveFileToDirectory(File srcFile, File destDir, boolean createDestDir) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination directory must not be null");
        }
        if (!destDir.exists() && createDestDir) {
            destDir.mkdirs();
        }
        if (!destDir.exists()) {
            throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist [createDestDir=" + createDestDir + "]");
        } else if (!destDir.isDirectory()) {
            throw new IOException("Destination '" + destDir + "' is not a directory");
        } else {
            moveFile(srcFile, new File(destDir, srcFile.getName()));
        }
    }

    public static void moveToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
        if (src == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!src.exists()) {
            throw new FileNotFoundException("Source '" + src + "' does not exist");
        } else if (src.isDirectory()) {
            moveDirectoryToDirectory(src, destDir, createDestDir);
        } else {
            moveFileToDirectory(src, destDir, createDestDir);
        }
    }

    public static boolean isSymlink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        return Files.isSymbolicLink(file.toPath());
    }
}
