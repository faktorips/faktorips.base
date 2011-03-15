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

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;

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
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        generatedJavaElements = new ArrayList<IJavaElement>();
        builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
    }

    /**
     * Returns the generated Java type for the given {@link IIpsObject}.
     */
    protected final IType getGeneratedJavaType(IIpsObject ipsObject,
            boolean derivedSource,
            String kindId,
            String javaTypeName) {

        try {
            IFolder outputFolder = ipsObject.getIpsPackageFragment().getRoot().getArtefactDestination(derivedSource);
            IPackageFragmentRoot javaRoot = ipsObject.getIpsProject().getJavaProject()
                    .getPackageFragmentRoot(outputFolder);
            String packageName = builderSet.getPackage(kindId, ipsObject.getIpsSrcFile());
            IPackageFragment javaPackage = javaRoot.getPackageFragment(packageName);
            ICompilationUnit javaCompilationUnit = javaPackage.getCompilationUnit(javaTypeName
                    + JavaSourceFileBuilder.JAVA_EXTENSION);
            return javaCompilationUnit.getType(javaTypeName);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Expects a specific field to be added to the list of generated Java elements.
     * 
     * @param index The position at which the field is expected in the list of generated Java
     *            elements
     * @param javaType The Java type the expected field belongs to
     * @param fieldName The name of the expected field
     */
    protected final void expectField(int index, IType javaType, String fieldName) {
        IField field = javaType.getField(fieldName);
        assertEquals(field, generatedJavaElements.get(index));
    }

    /**
     * Expects a specific method to be added to the list of generated Java elements.
     * 
     * @param javaType The Java type the expected method belongs to
     * @param methodName The name of the expected method
     * @param parameterTypeSignatures The parameter type signatures of the expected method (use the
     *            <tt>xxxParam(...)</tt> methods offered by this class)
     */
    protected final void expectMethod(IType javaType, String methodName, String... parameterTypeSignatures) {
        IMethod method = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(method));
    }

    /**
     * Returns the JDT signature for a primitive integer parameter.
     */
    protected final String intParam() {
        return Signature.SIG_INT;
    }

    /**
     * Returns the JDT signature for a void parameter.
     */
    protected final String voidParam() {
        return Signature.SIG_VOID;
    }

    /**
     * Returns the JDT signature for a primitive boolean parameter.
     */
    protected final String booleanParam() {
        return Signature.SIG_BOOLEAN;
    }

    /**
     * Returns the JDT signature for a {@link String} parameter.
     */
    protected final String stringParam() {
        return Signature.createTypeSignature(String.class.getSimpleName(), false);
    }

    /**
     * Returns the JDT type signature for unresolved types.
     * <p>
     * Use this method to create type signatures for types that are found in the source code or via
     * import.
     */
    protected final String unresolvedParam(String unqualifiedTypeName) {
        return Signature.createTypeSignature(unqualifiedTypeName, false);
    }

    /**
     * Returns the JDT type signature for resolved types.
     * <p>
     * Use this method to create type signatures for types that are not found in the source code or
     * via import (e.g. if a type is written fully qualified in the source code such as
     * <tt>java.util.Calendar</tt>).
     */
    protected final String resolvedParam(String qualifiedTypeName) {
        return Signature.createTypeSignature(qualifiedTypeName, true);
    }

}
