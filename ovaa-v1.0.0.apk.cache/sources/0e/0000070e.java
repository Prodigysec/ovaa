package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.Serializable;

/* loaded from: classes.dex */
public class DelegateFileFilter extends AbstractFileFilter implements Serializable {
    private static final long serialVersionUID = -8723373124984771318L;
    private final FileFilter fileFilter;
    private final FilenameFilter filenameFilter;

    public DelegateFileFilter(FilenameFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("The FilenameFilter must not be null");
        }
        this.filenameFilter = filter;
        this.fileFilter = null;
    }

    public DelegateFileFilter(FileFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("The FileFilter must not be null");
        }
        this.fileFilter = filter;
        this.filenameFilter = null;
    }

    @Override // org.apache.commons.io.filefilter.AbstractFileFilter, org.apache.commons.io.filefilter.IOFileFilter, java.io.FileFilter
    public boolean accept(File file) {
        FileFilter fileFilter = this.fileFilter;
        if (fileFilter != null) {
            return fileFilter.accept(file);
        }
        return super.accept(file);
    }

    @Override // org.apache.commons.io.filefilter.AbstractFileFilter, org.apache.commons.io.filefilter.IOFileFilter, java.io.FilenameFilter
    public boolean accept(File dir, String name) {
        FilenameFilter filenameFilter = this.filenameFilter;
        if (filenameFilter != null) {
            return filenameFilter.accept(dir, name);
        }
        return super.accept(dir, name);
    }

    @Override // org.apache.commons.io.filefilter.AbstractFileFilter
    public String toString() {
        Object obj = this.fileFilter;
        if (obj == null) {
            obj = this.filenameFilter;
        }
        String delegate = obj.toString();
        return super.toString() + "(" + delegate + ")";
    }
}