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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Abstract base class that can be used by tests for the standard builder.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractStdBuilderTest extends AbstractIpsPluginTest {

    protected IIpsProject ipsProject;

    /** A list that can be used by test cases to store the list of Java elements generated. */
    protected List<IJavaElement> generatedJavaElements;

    protected StandardBuilderSet builderSet;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        generatedJavaElements = new ArrayList<IJavaElement>();
        builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
    }

    /** Returns the generated Java type for the given <tt>IIpsObject</tt>. */
    protected final IType getGeneratedJavaType(IIpsObject ipsObject,
            boolean derivedSource,
            boolean internalSource,
            String javaTypeName) {

        String rootFolderName = derivedSource ? OUTPUT_FOLDER_NAME_DERIVED : OUTPUT_FOLDER_NAME_MERGABLE;
        IFolder rootFolder = ipsProject.getProject().getFolder(rootFolderName);
        IPackageFragmentRoot javaPackageRoot = ipsProject.getJavaProject().getPackageFragmentRoot(rootFolder);

        String basePackageName = derivedSource ? BASE_PACKAGE_NAME_DERIVED : BASE_PACKAGE_NAME_MERGABLE;
        String internalSeparator = internalSource ? "internal" : "";
        String ipsFragmentName = ipsObject.getIpsPackageFragment().getName();
        String ipsPackageName = (ipsFragmentName.length() > 0) ? internalSeparator + "." + ipsFragmentName
                : internalSeparator;
        String javaPackageName = (ipsPackageName.length() > 0) ? basePackageName + "." + ipsPackageName
                : basePackageName;

        IPackageFragment javaPackage = javaPackageRoot.getPackageFragment(javaPackageName);
        ICompilationUnit javaCompilationUnit = javaPackage.getCompilationUnit(javaTypeName
                + JavaSourceFileBuilder.JAVA_EXTENSION);
        return javaCompilationUnit.getType(javaTypeName);
    }

}
