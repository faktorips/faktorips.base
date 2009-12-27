/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides basic functionality for the refactoring participant tests of the standard builder.
 * 
 * @author Alexander Weickmann
 */
public abstract class RefactoringParticipantTest extends AbstractIpsRefactoringTest {

    protected IFolder modelFolder;

    protected IFolder internalFolder;

    protected IType policyClass;

    protected IType policyInterface;

    protected IType productClass;

    protected IType productInterface;

    protected IType productGenClass;

    protected IType productGenInterface;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Initialize folders and Java elements.
        modelFolder = ipsProject.getProject().getFolder(Path.fromOSString("src/org/faktorips/sample/model"));
        internalFolder = modelFolder.getFolder("internal");
        policyInterface = getJavaType(PACKAGE, POLICY_NAME, false);
        policyClass = getJavaType(PACKAGE, POLICY_NAME, true);
        productInterface = getJavaType(PACKAGE, PRODUCT_NAME, false);
        productClass = getJavaType(PACKAGE, PRODUCT_NAME, true);
        productGenInterface = getJavaType(PACKAGE, PRODUCT_NAME + "Gen", false);
        productGenClass = getJavaType(PACKAGE, PRODUCT_NAME + "Gen", true);
    }

    /**
     * Returns the Java <tt>IType</tt> corresponding to the indicated package name, type name and
     * internal flag.
     * 
     * @param packageName The package where the <tt>IType</tt> is located.
     * @param typeName The name of the <tt>IType</tt>.
     * @param implementation Flag indicating whether an implementation type or a published interface
     *            type is searched.
     * 
     * @throws NullPointerException If <tt>packageName</tt> or <tt>typeName</tt> is <tt>null</tt>.
     */
    protected final IType getJavaType(String packageName, String typeName, boolean implementation) {
        ArgumentCheck.notNull(new Object[] { packageName, typeName });
        IFolder folder = implementation ? internalFolder : modelFolder;
        String interfaceSeparator = implementation ? "" : "I";
        folder = (packageName == "") ? folder : folder.getFolder(packageName);
        return ((ICompilationUnit)JavaCore.create(folder.getFile(interfaceSeparator + typeName + ".java")))
                .getType(interfaceSeparator + typeName);
    }

}
