/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.abstracttest.core.AbstractCoreIpsPluginTest;
import org.faktorips.devtools.model.builder.naming.JavaClassNaming;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;

/**
 * Abstract base class that can be used by tests for the standard builder.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractStdBuilderTest extends AbstractCoreIpsPluginTest {

    protected IIpsProject ipsProject;

    /** A list that can be used by test cases to store the list of Java elements generated. */
    protected List<IJavaElement> generatedJavaElements;

    protected StandardBuilderSet builderSet;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        generatedJavaElements = new ArrayList<>();
        builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
    }

    @Override
    protected void setTestArtefactBuilderSet(IIpsProjectProperties properties, IIpsProject project)
            throws CoreRuntimeException {

        properties.setBuilderSetId(StandardBuilderSet.ID);
    }

    /**
     * Returns the generated Java implementation class for the given {@link IIpsObject}.
     */
    protected final IType getGeneratedJavaClass(IIpsObject ipsObject,
            boolean published,
            boolean derivedSource,
            String conceptName) {
        String javaTypeName = ipsObject.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(conceptName);
        return getGeneratedJavaType(ipsObject, published, derivedSource, javaTypeName);
    }

    /**
     * Returns the generated Java implementation class for the given {@link IIpsObject}. The
     * generated class is treated as an internal implementation.
     */
    protected final IType getGeneratedJavaClass(IIpsObject ipsObject, boolean derivedSource, String conceptName) {
        return getGeneratedJavaClass(ipsObject, false, derivedSource, conceptName);
    }

    /**
     * Returns the generated published Java interface for the given {@link IIpsObject}.
     */
    protected final IType getGeneratedJavaInterface(IIpsObject ipsObject, boolean derivedSource, String conceptName) {

        String javaTypeName = ipsObject.getIpsProject().getJavaNamingConvention()
                .getPublishedInterfaceName(conceptName);
        return getGeneratedJavaType(ipsObject, true, derivedSource, javaTypeName);
    }

    private final IType getGeneratedJavaType(IIpsObject ipsObject,
            boolean published,
            boolean derivedSource,
            String javaTypeName) {

        IPackageFragmentRoot javaRoot = ipsObject.getIpsPackageFragment().getRoot()
                .getArtefactDestination(derivedSource).unwrap();
        String packageName = builderSet.getPackageName(ipsObject.getIpsSrcFile(), !published, !derivedSource);
        IPackageFragment javaPackage = javaRoot.getPackageFragment(packageName);
        ICompilationUnit javaCompilationUnit = javaPackage
                .getCompilationUnit(javaTypeName + JavaClassNaming.JAVA_EXTENSION);
        return javaCompilationUnit.getType(javaTypeName);
    }

    /**
     * Expects a specific {@link IType} to be added to the list of generated Java elements.
     * 
     * @param type The {@link IType} that is expected to be contained in the generated Java elements
     */
    protected final void expectType(IType type) {
        assertTrue(generatedJavaElements.contains(type));
    }

    /**
     * Expects a specific {@link IField} to be added to the list of generated Java elements.
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
     * Expects a specific {@link IMethod} to be added to the list of generated Java elements.
     * 
     * @param javaType The Java type the expected method belongs to
     * @param methodName The name of the expected method
     * @param parameterTypeSignatures The parameter type signatures of the expected method (use the
     *            <code>xxxParam(...)</code> methods offered by this class)
     */
    protected final void expectMethod(IType javaType, String methodName, String... parameterTypeSignatures) {
        IMethod method = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(method));
    }

    protected final IJavaNamingConvention getJavaNamingConvention() {
        return ipsProject.getJavaNamingConvention();
    }

    protected void setGeneratorProperty(IIpsProject ipsProject, String key, String value) {
        IIpsProjectProperties properties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel builderConfig = properties.getBuilderSetConfig();
        builderConfig.setPropertyValue(key, value, null);
        ipsProject.setProperties(properties);
        ipsProject.reinitializeIpsArtefactBuilderSet();
        builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
    }

}
