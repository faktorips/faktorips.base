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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AttributeType;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class GenConstantAttributeTest extends GenPolicyCmptTypeAttributeTest {

    /** <tt>GenConstantAttribute</tt> generator for the published attribute. */
    private GenConstantAttribute genPublishedConstantAttribute;

    /** <tt>GenConstantAttribute</tt> generator for the public attribute. */
    private GenConstantAttribute genPublicConstantAttribute;

    public GenConstantAttributeTest() {
        super(AttributeType.CONSTANT);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        genPublishedConstantAttribute = new GenConstantAttribute(genPolicyCmptType, publishedAttribute);
        genPublicConstantAttribute = new GenConstantAttribute(genPolicyCmptType, publicAttribute);
    }

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedConstantAttribute);
        expectMemberConstant(generatedJavaElements, genPublishedConstantAttribute, getGeneratedJavaType());
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedConstantAttribute);
        expectMemberConstant(generatedJavaElements, genPublishedConstantAttribute, getGeneratedJavaType());
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicConstantAttribute);
        expectMemberConstant(generatedJavaElements, genPublicConstantAttribute, getGeneratedJavaType());
        assertEquals(2, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicConstantAttribute);
        expectMemberConstant(generatedJavaElements, genPublicConstantAttribute, getGeneratedJavaType());
        assertEquals(2, generatedJavaElements.size());
    }

    private void expectMemberConstant(List<IJavaElement> javaElements,
            GenConstantAttribute genConstantAttribute,
            IType javaType) {

        IField expectedMemberConstant = javaType.getField(genConstantAttribute.getConstantMemberVarName());
        assertTrue(javaElements.contains(expectedMemberConstant));
    }

}
