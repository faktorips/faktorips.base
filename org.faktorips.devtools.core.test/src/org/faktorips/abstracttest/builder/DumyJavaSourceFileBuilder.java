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

package org.faktorips.abstracttest.builder;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

public class DumyJavaSourceFileBuilder extends JavaSourceFileBuilder {

    public boolean generateCalled = false;
    public boolean isBuilderFor = false;

    public DumyJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, kindId, localizedStringsSet);
    }

    @Override
    protected String generate() throws CoreException {
        generateCalled = true;
        return "";
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return isBuilderFor;
    }

    public void reset() {
        generateCalled = false;
        isBuilderFor = false;
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements, IIpsElement ipsElement) {

    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }
}
