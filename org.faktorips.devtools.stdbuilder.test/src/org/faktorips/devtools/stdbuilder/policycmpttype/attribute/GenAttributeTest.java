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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptTypeBuilderTest;

/**
 * 
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

    protected IType getGeneratedJavaType() {
        IFile javaSourceFile = ipsProject.getProject().getFile("Type.java");
        ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(javaSourceFile);
        return compilationUnit.getType("Type");
    }

    // public void testGetGeneratedJavaElementsInterface() throws CoreException {
    // List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();
    // genAttribute.getGeneratedJavaElements(generatedJavaElements, getGeneratedJavaType(),
    // policyCmptTypeAttribute,
    // true);
    //
    // IField expectedPropertyConstant =
    // getGeneratedJavaType().getField(genAttribute.getStaticConstantPropertyName());
    // IMethod expectedGetterMethodDeclaration =
    // getGeneratedJavaType().getMethod(genAttribute.getGetterMethodName(),
    // new String[] {});
    // IMethod expectedSetterMethodDeclaration =
    // getGeneratedJavaType().getMethod(genAttribute.getSetterMethodName(),
    // new String[] { "Q" + policyCmptTypeAttribute.getDatatype() + ";" });
    //
    // assertTrue(generatedJavaElements.contains(expectedPropertyConstant));
    // assertTrue(generatedJavaElements.contains(expectedGetterMethodDeclaration));
    // assertTrue(generatedJavaElements.contains(expectedSetterMethodDeclaration));
    //
    // assertEquals(3, generatedJavaElements.size());
    //
    // }
    //
    // public void testGetGeneratedJavaElementsImpl() throws CoreException {
    // List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();
    // genAttribute.getGeneratedJavaElements(generatedJavaElements, getGeneratedJavaType(),
    // policyCmptTypeAttribute,
    // false);
    //
    // IField expectedMemberVar = getGeneratedJavaType().getField(genAttribute.getMemberVarName());
    // IMethod expectedGetterMethod =
    // getGeneratedJavaType().getMethod(genAttribute.getGetterMethodName(),
    // new String[] {});
    // IMethod expectedSetterMethod =
    // getGeneratedJavaType().getMethod(genAttribute.getSetterMethodName(),
    // new String[] { "Q" + policyCmptTypeAttribute.getDatatype() + ";" });
    //
    // assertTrue(generatedJavaElements.contains(expectedMemberVar));
    // assertTrue(generatedJavaElements.contains(expectedGetterMethod));
    // assertTrue(generatedJavaElements.contains(expectedSetterMethod));
    //
    // assertEquals(3, generatedJavaElements.size());
    // }
    //
    // private static class GenAttributeDummy extends GenAttribute {
    //
    // public GenAttributeDummy(GenPolicyCmptType genPolicyCmptType, IPolicyCmptTypeAttribute a)
    // throws CoreException {
    // super(genPolicyCmptType, a);
    // }
    //
    // @Override
    // protected void generateConstants(JavaCodeFragmentBuilder builder,
    // IIpsProject ipsProject,
    // boolean generatesInterface) throws CoreException {
    //
    // }
    //
    // @Override
    // protected void generateMemberVariables(JavaCodeFragmentBuilder builder,
    // IIpsProject ipsProject,
    // boolean generatesInterface) throws CoreException {
    //
    // }
    //
    // @Override
    // protected void generateMethods(JavaCodeFragmentBuilder builder,
    // IIpsProject ipsProject,
    // boolean generatesInterface) throws CoreException {
    //
    // }
    //
    // }

}
