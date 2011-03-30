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

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.junit.Before;
import org.junit.Test;

public class GenProductCmptTypeAttributeTest extends ProductCmptTypeBuilderTest {

    private IProductCmptTypeAttribute publishedAttribute;

    private IProductCmptTypeAttribute publicAttribute;

    private GenProductCmptTypeAttribute genPublishedAttribute;

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
        expectGetterMethod(javaInterfaceGeneration, genPublishedAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, publicAttribute);
        expectGetterMethod(javaInterfaceGeneration, genPublicAttribute);
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() throws CoreException {
        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedAttribute);
        expectMemberVar(0, javaClassGeneration, genPublishedAttribute);
        expectGetterMethod(javaClassGeneration, genPublishedAttribute);
        expectSetterMethod(javaClassGeneration, genPublishedAttribute);
        expectGetterMethod(genPublishedAttribute.findGeneratedJavaTypeForPolicyCmptType(false), genPublishedAttribute);
        assertEquals(4, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicAttribute);
        expectMemberVar(0, javaClassGeneration, genPublicAttribute);
        expectGetterMethod(javaClassGeneration, genPublicAttribute);
        expectSetterMethod(javaClassGeneration, genPublicAttribute);
        expectGetterMethod(genPublicAttribute.findGeneratedJavaTypeForPolicyCmptType(false), genPublicAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationNotConfiguring() {
        productCmptType.setConfigurationForPolicyCmptType(false);

        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedAttribute);
        expectMemberVar(0, javaClassGeneration, genPublishedAttribute);
        expectGetterMethod(javaClassGeneration, genPublishedAttribute);
        expectSetterMethod(javaClassGeneration, genPublishedAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicAttribute);
        expectMemberVar(0, javaClassGeneration, genPublicAttribute);
        expectGetterMethod(javaClassGeneration, genPublicAttribute);
        expectSetterMethod(javaClassGeneration, genPublicAttribute);
        assertEquals(3, generatedJavaElements.size());
    }

    private void expectStaticConstant(int index, IType javaType, GenProductCmptTypeAttribute genAttribute) {
        expectField(index, javaType, genAttribute.getStaticConstantPropertyName());
    }

    private void expectMemberVar(int index, IType javaType, GenAttribute genAttribute) {
        expectField(index, javaType, genAttribute.getMemberVarName());
    }

    private void expectGetterMethod(IType javaType, GenAttribute genAttribute) {
        expectMethod(javaType, genAttribute.getGetterMethodName());
    }

    private void expectSetterMethod(IType javaType, GenAttribute genAttribute) {
        expectMethod(javaType, genAttribute.getSetterMethodName(),
                unresolvedParam(genAttribute.getDatatype().getName()));
    }

}
