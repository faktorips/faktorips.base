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

package org.faktorips.devtools.stdbuilder.productcmpttype.attribute;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;

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
    protected void setUp() throws Exception {
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

    /** Expects, that the getter method is contained in the given list. */
    private final void expectGetterMethod(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IMethod expectedGetterMethod = getGeneratedJavaType().getMethod(genAttribute.getGetterMethodName(),
                new String[] {});
        assertTrue(javaElements.contains(expectedGetterMethod));
    }

    /** Expects, that the setter method is contained in the given list. */
    private final void expectSetterMethod(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IMethod expectedSetterMethod = getGeneratedJavaType().getMethod(genAttribute.getSetterMethodName(),
                new String[] { "Q" + genAttribute.getDatatype().getName() + ";" });
        assertTrue(javaElements.contains(expectedSetterMethod));
    }

    /** Expects, that the member variable is contained in the given list. */
    private final void expectMemberVar(List<IJavaElement> javaElements, GenAttribute genAttribute) {
        IField expectedMemberVar = getGeneratedJavaType().getField(genAttribute.getMemberVarName());
        assertTrue(javaElements.contains(expectedMemberVar));
    }

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublishedAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, getGeneratedJavaType(),
                publicAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublicAttribute);
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, getGeneratedJavaType(),
                publishedAttribute, false);
        expectMemberVar(generatedJavaElements, genPublishedAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, getGeneratedJavaType(),
                publicAttribute, false);
        expectMemberVar(generatedJavaElements, genPublicAttribute);
        expectGetterMethod(generatedJavaElements, genPublicAttribute);
        expectSetterMethod(generatedJavaElements, genPublicAttribute);
        assertEquals(3, generatedJavaElements.size());
    }

}
