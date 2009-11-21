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
public class GenChangeableAttributeTest extends GenAttributeTest {

    /** <tt>GenChangeableAttribute</tt> generator for the published attribute. */
    private GenChangeableAttribute genPublishedChangeableAttribute;

    /** <tt>GenChangeableAttribute</tt> generator for the public attribute. */
    private GenChangeableAttribute genPublicChangeableAttribute;

    public GenChangeableAttributeTest() {
        super(AttributeType.CHANGEABLE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        genPublishedChangeableAttribute = new GenChangeableAttribute(genPolicyCmptType, publishedAttribute);
        genPublicChangeableAttribute = new GenChangeableAttribute(genPolicyCmptType, publicAttribute);
    }

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElements(generatedJavaElements, getGeneratedJavaType(),
                publishedAttribute, true);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElements(generatedJavaElements, getGeneratedJavaType(),
                publicAttribute, true);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElements(generatedJavaElements, getGeneratedJavaType(),
                publishedAttribute, false);
        expectMemberVar(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElements(generatedJavaElements, getGeneratedJavaType(),
                publicAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        expectMemberVar(generatedJavaElements, genPublicChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

}
