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

package org.faktorips.devtools.stdbuilder.productcmpttype.attribute;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class GenProductCmptTypeAttributeTest extends ProductCmptTypeBuilderTest {

    /** A published <tt>IProductCmptTypeAttribute</tt> that can be used for tests. */
    private IProductCmptTypeAttribute publishedAttribute;

    /** A public <tt>IProductCmptTypeAttribute</tt> that can be used for tests. */
    private IProductCmptTypeAttribute publicAttribute;

    /** <tt>GenProductCmptTypeAttribute</tt> generator for the published attribute. */
    private GenProductCmptTypeAttribute genPublishedAttribute;

    /** <tt>GenProductCmptTypeAttribute</tt> generator for the public attribute. */
    private GenProductCmptTypeAttribute genPublicAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        publishedAttribute = productCmptType.newProductCmptTypeAttribute();
        publishedAttribute.setName("publishedAttribute");
        publishedAttribute.setDatatype(Datatype.STRING.getName());
        publishedAttribute.setModifier(Modifier.PUBLISHED);

        publicAttribute = productCmptType.newProductCmptTypeAttribute();
        publicAttribute.setName("publicAttribute");
        publicAttribute.setDatatype(Datatype.STRING.getName());
        publicAttribute.setModifier(Modifier.PUBLIC);

        genPublishedAttribute = new GenProductCmptTypeAttribute(genProductCmptType, publishedAttribute);
        genPublicAttribute = new GenProductCmptTypeAttribute(genProductCmptType, publicAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, publishedAttribute);
        expectStaticConstant(0, javaInterfaceGeneration, genPublishedAttribute);
        expectGetterMethod(1, javaInterfaceGeneration, genPublishedAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, publicAttribute);
        expectGetterMethod(0, javaInterfaceGeneration, genPublicAttribute);
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() throws CoreException {
        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedAttribute);
        expectMemberVar(0, javaClassGeneration, genPublishedAttribute);
        expectGetterMethod(1, javaClassGeneration, genPublishedAttribute);
        expectSetterMethod(2, javaClassGeneration, genPublishedAttribute);
        expectGetterMethod(3, genPublishedAttribute.findGeneratedJavaTypeForPolicyCmptType(false),
                genPublishedAttribute);
        assertEquals(4, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicAttribute);
        expectMemberVar(0, javaClassGeneration, genPublicAttribute);
        expectGetterMethod(1, javaClassGeneration, genPublicAttribute);
        expectSetterMethod(2, javaClassGeneration, genPublicAttribute);
        expectGetterMethod(3, genPublicAttribute.findGeneratedJavaTypeForPolicyCmptType(false), genPublicAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationNotConfiguring() {
        productCmptType.setConfigurationForPolicyCmptType(false);

        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedAttribute);
        expectMemberVar(0, javaClassGeneration, genPublishedAttribute);
        expectGetterMethod(1, javaClassGeneration, genPublishedAttribute);
        expectSetterMethod(2, javaClassGeneration, genPublishedAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicAttribute);
        expectMemberVar(0, javaClassGeneration, genPublicAttribute);
        expectGetterMethod(1, javaClassGeneration, genPublicAttribute);
        expectSetterMethod(2, javaClassGeneration, genPublicAttribute);
        assertEquals(3, generatedJavaElements.size());
    }

    private void expectStaticConstant(int index, IType javaType, GenProductCmptTypeAttribute genAttribute) {
        IField field = javaType.getField(genAttribute.getStaticConstantPropertyName());
        assertEquals(field, generatedJavaElements.get(index));
    }

    private void expectGetterMethod(int index, IType javaType, GenAttribute genAttribute) {
        IMethod expectedGetterMethod = javaType.getMethod(genAttribute.getGetterMethodName(), new String[] {});
        assertEquals(expectedGetterMethod, generatedJavaElements.get(index));
    }

    private void expectSetterMethod(int index, IType javaType, GenAttribute genAttribute) {
        IMethod expectedSetterMethod = javaType.getMethod(genAttribute.getSetterMethodName(), new String[] { "Q"
                + genAttribute.getDatatype().getName() + ";" });
        assertEquals(expectedSetterMethod, generatedJavaElements.get(index));
    }

    private void expectMemberVar(int index, IType javaType, GenAttribute genAttribute) {
        IField expectedMemberVar = javaType.getField(genAttribute.getMemberVarName());
        assertEquals(expectedMemberVar, generatedJavaElements.get(index));
    }

}
