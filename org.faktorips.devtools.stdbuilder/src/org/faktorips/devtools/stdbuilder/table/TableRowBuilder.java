package org.faktorips.devtools.stdbuilder.table;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class TableRowBuilder extends JavaSourceFileBuilder {

    public TableRowBuilder(IJavaPackageStructure packageStructure, String kindId) {
        super(packageStructure, kindId, new LocalizedStringsSet(TableRowBuilder.class));
    }

    protected String generate() throws CoreException {
        TableRowGenerator generator = new TableRowGenerator();
        generator.setJavaSourceFileBuilder(this);
        return generator.generate(getIpsSrcFile());
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType());
    }

    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "Row";
    }
}
