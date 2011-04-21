/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class TableRowBuilder extends JavaSourceFileBuilder {

    public TableRowBuilder(DefaultBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(TableRowBuilder.class));
    }

    @Override
    protected String generate() throws CoreException {
        TableRowGenerator generator = new TableRowGenerator();
        generator.setImportContainer(getImportContainer());
        generator.setJavaSourceFileBuilder(this);
        return generator.generate(getIpsSrcFile());
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())) {
            return true;
        }
        return false;
    }

    @Override
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "Row";
    }

    public String getFieldNameForNullRow() {
        return "NULL_ROW";
    }

    private IImportContainer getImportContainer() throws CoreException {
        IFile file = getJavaFile(getIpsSrcFile());
        ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
        if (cu == null || !cu.exists()) {
            return null;
        }
        return cu.getImportContainer();
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

}
