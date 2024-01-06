package org.apache.commons.io.serialization;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
/* loaded from: classes.dex */
final class FullClassNameMatcher implements ClassNameMatcher {
    private final Set<String> classesSet;

    public FullClassNameMatcher(String... classes) {
        this.classesSet = Collections.unmodifiableSet(new HashSet(Arrays.asList(classes)));
    }

    @Override // org.apache.commons.io.serialization.ClassNameMatcher
    public boolean matches(String className) {
        return this.classesSet.contains(className);
    }
}
