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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import org.eclipse.jdt.core.IField;
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
        genPublishedConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(javaInterface, genPublishedConstantAttribute);
        expectMemberConstant(javaInterface, genPublishedConstantAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(javaInterface, genPublishedConstantAttribute);
        expectMemberConstant(javaInterface, genPublishedConstantAttribute);
        assertEquals(2, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        genPublishedConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(javaClass, genPublicConstantAttribute);
        expectMemberConstant(javaClass, genPublicConstantAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicConstantAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(javaClass, genPublicConstantAttribute);
        expectMemberConstant(javaClass, genPublicConstantAttribute);
        assertEquals(2, generatedJavaElements.size());
    }

    private void expectMemberConstant(IType javaType, GenConstantAttribute genConstantAttribute) {
        String fieldName = genConstantAttribute.getConstantMemberVarName();
        IField expectedMemberConstant = javaType.getField(fieldName);
        assertTrue(generatedJavaElements.contains(expectedMemberConstant));
    }

}
