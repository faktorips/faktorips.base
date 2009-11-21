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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptTypeBuilderTest;

/**
 * Abstract base class for tests concerning the generators for <tt>IPolicyCmptTypeAttribute</tt>s.
 * <p>
 * Provides convenient methods.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenAttributeTest extends PolicyCmptTypeBuilderTest {

    protected IPolicyCmptTypeAttribute publishedAttribute;

    protected IPolicyCmptTypeAttribute publicAttribute;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        publishedAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        publishedAttribute.setName("publishedAttribute");
        publishedAttribute.setDatatype(Datatype.STRING.getName());
        publishedAttribute.setModifier(Modifier.PUBLISHED);

        publicAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        publicAttribute.setName("publicAttribute");
        publicAttribute.setDatatype(Datatype.STRING.getName());
        publicAttribute.setModifier(Modifier.PUBLIC);
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

    /** Expects, that the property constant field is contained in the given list. */
    protected final void expectPropertyConstant(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IField expectedPropertyConstant = getGeneratedJavaType().getField(genAttribute.getStaticConstantPropertyName());
        assertTrue(javaElements.contains(expectedPropertyConstant));
    }

    /** Expects, that the getter method is contained in the given list. */
    protected final void expectGetterMethod(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IMethod expectedGetterMethod = getGeneratedJavaType().getMethod(genAttribute.getGetterMethodName(),
                new String[] {});
        assertTrue(javaElements.contains(expectedGetterMethod));
    }

    /** Expects, that the setter method is contained in the given list. */
    protected final void expectSetterMethod(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IMethod expectedSetterMethod = getGeneratedJavaType().getMethod(genAttribute.getSetterMethodName(),
                new String[] { "Q" + genAttribute.getDatatype().getName() + ";" });
        assertTrue(javaElements.contains(expectedSetterMethod));
    }

    /** Expects, that the member variable is contained in the given list. */
    protected final void expectMemberVar(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IField expectedMemberVar = getGeneratedJavaType().getField(genAttribute.getMemberVarName());
        assertTrue(javaElements.contains(expectedMemberVar));
    }

}
