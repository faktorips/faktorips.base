package org.faktorips.devtools.stdbuilder.table;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.AbstractTocFileUpdateBuilder;
import org.faktorips.runtime.TocEntry;

public class TableTocFileUpdateBuilder extends AbstractTocFileUpdateBuilder {

    private TableImplBuilder tableImplBuilder;

    /**
     * See super class constructor.
     */
    public TableTocFileUpdateBuilder(IJavaPackageStructure structure, String kind) {
        super(structure, kind);
    }

    public void setTableImplBuilder(TableImplBuilder tableImplBuilder) {
        this.tableImplBuilder = tableImplBuilder;
    }

    
    protected void checkIfDependOnBuildersSet() throws IllegalStateException {
        String builderName = null;

        if (tableImplBuilder == null) {
            builderName = TableImplBuilder.class.getName();
        }

        if (builderName != null) {
            throw new IllegalStateException(
                    "One of the builders this builder depends on is not set: " + builderName);
        }
    }

    protected TocEntry createTocEntry(IIpsObject object) throws CoreException {
        ITableContents tableContents = (ITableContents)object;
        ITableStructure tableStructure = tableContents.findTableStructure();
        if (tableStructure == null) {
            return null;
        }
        String packageInternal = getPackageStructure().getPackage(kind,
            tableContents.getIpsSrcFile());
        String tableStructureName = tableImplBuilder.getQualifiedClassName(tableStructure
                .getIpsSrcFile());
        String xmlResourceName = packageInternal.replace('.', '/') + '/' + tableContents.getName()
                + ".xml";
        TocEntry entry = TocEntry.createTableTocEntry(tableStructure.getQualifiedName(),
            xmlResourceName, tableStructureName, null);
        return entry;
    }

    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType());
    }
}
