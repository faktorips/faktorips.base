/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.junit.Before;
import org.junit.Test;

public class GenDerivedAttributeTest extends GenPolicyCmptTypeAttributeTest {

    private GenDerivedAttribute genPublishedDerivedAttribute;

    private GenDerivedAttribute genPublicDerivedAttribute;

    public GenDerivedAttributeTest() {
        super(AttributeType.DERIVED_ON_THE_FLY);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        genPublishedDerivedAttribute = new GenDerivedAttribute(genPolicyCmptType, publishedAttribute);
        genPublicDerivedAttribute = new GenDerivedAttribute(genPolicyCmptType, publicAttribute);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(0, javaInterface, genPublishedDerivedAttribute);
        expectGetterMethod(javaInterface, genPublishedDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevant() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(0, javaInterface, genPublishedDerivedAttribute);
        expectGetterMethod(javaInterface, genPublishedDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() {
        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectGetterMethod(javaClass, genPublishedDerivedAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(0, javaClass, genPublicDerivedAttribute);
        expectGetterMethod(javaClass, genPublicDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectGetterMethod(javaClass, genPublishedDerivedAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(0, javaClass, genPublicDerivedAttribute);
        expectGetterMethod(javaClass, genPublicDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationProductRelevant() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectGetterMethod(javaClass, genPublishedDerivedAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(0, javaClass, genPublicDerivedAttribute);
        expectGetterMethod(javaClass, genPublicDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationProductRelevantOverwritten() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectGetterMethod(javaClass, genPublishedDerivedAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(0, javaClass, genPublicDerivedAttribute);
        expectGetterMethod(javaClass, genPublicDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

}
