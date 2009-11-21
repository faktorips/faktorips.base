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
public class GenDerivedAttributeTest extends GenAttributeTest {

    /** <tt>GenDerivedAttribute</tt> generator for the published attribute. */
    private GenDerivedAttribute genPublishedDerivedAttribute;

    /** <tt>GenDerivedAttribute</tt> generator for the public attribute. */
    private GenDerivedAttribute genPublicDerivedAttribute;

    public GenDerivedAttributeTest() {
        super(AttributeType.DERIVED_ON_THE_FLY);
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
                getGeneratedJavaType(), publishedAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublishedDerivedAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        expectGetterMethod(generatedJavaElements, genPublishedDerivedAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicDerivedAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublicDerivedAttribute);
        expectGetterMethod(generatedJavaElements, genPublicDerivedAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

}
