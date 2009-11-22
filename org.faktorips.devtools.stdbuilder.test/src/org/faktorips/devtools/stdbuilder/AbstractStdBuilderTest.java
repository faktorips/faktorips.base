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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Abstract base class that can be used by tests for the standard builder.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractStdBuilderTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.setOutputFolderForMergableSources(ipsProject.getProject().getFolder("output"));
        ipsProject.setIpsObjectPath(ipsObjectPath);
    }

    /**
     * Creates and returns a Java type that can be used to test whether the generator correctly
     * returns generated <tt>IJavaElement</tt>s.
     */
    protected final IType getGeneratedJavaType() {
        IFile javaSourceFile = ipsProject.getProject().getFile("Type.java");
        ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(javaSourceFile);
        return compilationUnit.getType("Type");
    }

}
