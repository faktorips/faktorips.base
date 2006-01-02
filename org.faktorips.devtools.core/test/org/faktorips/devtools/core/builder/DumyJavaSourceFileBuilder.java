package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.LocalizedStringsSet;

class DumyJavaSourceFileBuilder extends JavaSourceFileBuilder {

    public boolean generateCalled = false;
    public boolean isBuilderFor = false;

    public DumyJavaSourceFileBuilder(IJavaPackageStructure packageStructure, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(packageStructure, kindId, localizedStringsSet);
    }

    protected String generate() throws CoreException {
        generateCalled = true;
        return null;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return isBuilderFor;
    }

    void reset() {
        generateCalled = false;
        isBuilderFor = false;
    }
}