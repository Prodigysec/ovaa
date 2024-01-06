package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/* loaded from: classes.dex */
public class CompositeFileComparator extends AbstractFileComparator implements Serializable {
    private static final Comparator<?>[] NO_COMPARATORS = new Comparator[0];
    private static final long serialVersionUID = -2224170307287243428L;
    private final Comparator<File>[] delegates;

    @Override // org.apache.commons.io.comparator.AbstractFileComparator
    public /* bridge */ /* synthetic */ List sort(List list) {
        return super.sort(list);
    }

    @Override // org.apache.commons.io.comparator.AbstractFileComparator
    public /* bridge */ /* synthetic */ File[] sort(File[] fileArr) {
        return super.sort(fileArr);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public CompositeFileComparator(Comparator<File>... delegates) {
        if (delegates == null) {
            this.delegates = NO_COMPARATORS;
            return;
        }
        Comparator<File>[] comparatorArr = new Comparator[delegates.length];
        this.delegates = comparatorArr;
        System.arraycopy(delegates, 0, comparatorArr, 0, delegates.length);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public CompositeFileComparator(Iterable<Comparator<File>> delegates) {
        if (delegates == null) {
            this.delegates = NO_COMPARATORS;
            return;
        }
        List<Comparator<File>> list = new ArrayList<>();
        for (Comparator<File> comparator : delegates) {
            list.add(comparator);
        }
        this.delegates = (Comparator[]) list.toArray(new Comparator[list.size()]);
    }

    @Override // java.util.Comparator
    public int compare(File file1, File file2) {
        Comparator<File>[] comparatorArr;
        int result = 0;
        for (Comparator<File> delegate : this.delegates) {
            result = delegate.compare(file1, file2);
            if (result != 0) {
                break;
            }
        }
        return result;
    }

    @Override // org.apache.commons.io.comparator.AbstractFileComparator
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append('{');
        for (int i = 0; i < this.delegates.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(this.delegates[i]);
        }
        builder.append('}');
        return builder.toString();
    }
}