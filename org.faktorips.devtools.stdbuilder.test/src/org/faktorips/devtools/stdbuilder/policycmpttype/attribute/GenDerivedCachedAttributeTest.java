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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.core.model.pctype.AttributeType;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class GenDerivedCachedAttributeTest extends GenPolicyCmptTypeAttributeTest {

    /** <tt>GenDerivedAttribute</tt> generator for the published attribute. */
    private GenDerivedAttribute genPublishedDerivedAttribute;

    /** <tt>GenDerivedAttribute</tt> generator for the public attribute. */
    private GenDerivedAttribute genPublicDerivedAttribute;

    public GenDerivedCachedAttributeTest() {
        super(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        genPublishedDerivedAttribute = new GenDerivedAttribute(genPolicyCmptType, publishedAttribute);
        genPublicDerivedAttribute = new GenDerivedAttribute(genPolicyCmptType, publicAttribute);
    }

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedDerivedAttribute, true);
        expectGetterMethod(generatedJavaElements, genPublishedDerivedAttribute, true);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevant() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedDerivedAttribute, true);
        expectGetterMethod(generatedJavaElements, genPublishedDerivedAttribute, true);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(true), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publishedAttribute);
        expectMemberVar(generatedJavaElements, genPublishedDerivedAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublishedDerivedAttribute, false);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicDerivedAttribute, false);
        expectMemberVar(generatedJavaElements, genPublicDerivedAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublicDerivedAttribute, false);
        assertEquals(3, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicDerivedAttribute, false);
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevant() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publishedAttribute);
        expectMemberVar(generatedJavaElements, genPublishedDerivedAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublishedDerivedAttribute, false);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicDerivedAttribute, false);
        expectMemberVar(generatedJavaElements, genPublicDerivedAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublicDerivedAttribute, false);
        assertEquals(3, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevantOverwritten() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(false), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicDerivedAttribute, false);
        assertEquals(1, generatedJavaElements.size());
    }

}
