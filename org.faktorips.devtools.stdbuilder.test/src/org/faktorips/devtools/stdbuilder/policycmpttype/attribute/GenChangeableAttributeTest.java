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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.runtime.IValidationContext;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class GenChangeableAttributeTest extends GenPolicyCmptTypeAttributeTest {

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

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevant() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaType());
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(true));
        expectGetDefaultValueMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(true));
        assertEquals(6, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaType());
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(true));
        expectGetDefaultValueMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(true));
        assertEquals(4, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectMemberVar(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        expectMemberVar(generatedJavaElements, genPublicChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevant() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectMemberVar(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaType());
        expectDefaultMemberVariable(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectSetOfAllowedValuesMemberVariable(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetDefaultValueMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        assertEquals(8, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        expectMemberVar(generatedJavaElements, genPublicChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublicChangeableAttribute, getGeneratedJavaType());
        expectDefaultMemberVariable(generatedJavaElements, genPublicChangeableAttribute, genPublicChangeableAttribute
                .findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectSetOfAllowedValuesMemberVariable(generatedJavaElements, genPublicChangeableAttribute,
                genPublicChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetDefaultValueMethod(generatedJavaElements, genPublicChangeableAttribute, genPublicChangeableAttribute
                .findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublicChangeableAttribute,
                genPublicChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        assertEquals(9, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevantOverwritten() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute);
        expectDefaultMemberVariable(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectSetOfAllowedValuesMemberVariable(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaType());
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetDefaultValueMethod(generatedJavaElements, genPublishedChangeableAttribute,
                genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        assertEquals(5, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        expectDefaultMemberVariable(generatedJavaElements, genPublicChangeableAttribute, genPublicChangeableAttribute
                .findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectSetOfAllowedValuesMemberVariable(generatedJavaElements, genPublicChangeableAttribute,
                genPublicChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublicChangeableAttribute, getGeneratedJavaType());
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublicChangeableAttribute,
                genPublicChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetDefaultValueMethod(generatedJavaElements, genPublicChangeableAttribute, genPublicChangeableAttribute
                .findGeneratedJavaTypeForProductCmptTypeGen(false));
        assertEquals(6, generatedJavaElements.size());
    }

    private void expectGetSetOfAllowedValuesMethod(List<IJavaElement> javaElements,
            GenChangeableAttribute genChangeableAttribute,
            IType javaType) {

        IMethod expectedGetSetOfAllowedValuesMethod = javaType.getMethod(genChangeableAttribute
                .getMethodNameGetSetOfAllowedValues(), new String[] { "Q" + IValidationContext.class.getSimpleName()
                + ";" });
        assertTrue(javaElements.contains(expectedGetSetOfAllowedValuesMethod));
    }

    private void expectGetDefaultValueMethod(List<IJavaElement> javaElements,
            GenChangeableAttribute genChangeableAttribute,
            IType javaType) {

        IMethod expectedGetDefaultValueMethod = javaType.getMethod(genChangeableAttribute
                .getMethodNameGetDefaultValue(), new String[] {});
        assertTrue(javaElements.contains(expectedGetDefaultValueMethod));
    }

    private void expectDefaultMemberVariable(List<IJavaElement> javaElements,
            GenChangeableAttribute genChangeableAttribute,
            IType javaType) {

        IField expectedDefaultMemberVariable = javaType.getField(genChangeableAttribute.getFieldNameDefaultValue());
        assertTrue(javaElements.contains(expectedDefaultMemberVariable));
    }

    private void expectSetOfAllowedValuesMemberVariable(List<IJavaElement> javaElements,
            GenChangeableAttribute genChangeableAttribute,
            IType javaType) {

        IField expectedSetOfAllowedValuesMemberVariable = javaType.getField(genChangeableAttribute
                .getFieldNameSetOfAllowedValues());
        assertTrue(javaElements.contains(expectedSetOfAllowedValuesMemberVariable));
    }

}
