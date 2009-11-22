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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
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
                getGeneratedJavaType(), publishedAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevant() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaType());
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(true));
        expectGetDefaultValueMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(true));
        assertEquals(6, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        expectMemberVar(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        expectMemberVar(generatedJavaElements, genPublicChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevant() {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        expectMemberVar(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublishedChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaType());
        expectDefaultMemberVariable(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        expectSetOfAllowedValuesMemberVariable(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetDefaultValueMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublishedChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        assertEquals(8, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        expectMemberVar(generatedJavaElements, genPublicChangeableAttribute);
        expectGetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectSetterMethod(generatedJavaElements, genPublicChangeableAttribute);
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublicChangeableAttribute, getGeneratedJavaType());
        expectDefaultMemberVariable(generatedJavaElements, genPublicChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        expectSetOfAllowedValuesMemberVariable(generatedJavaElements, genPublicChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetDefaultValueMethod(generatedJavaElements, genPublicChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        expectGetSetOfAllowedValuesMethod(generatedJavaElements, genPublicChangeableAttribute,
                getGeneratedJavaTypeForProductCmptTypeGen(false));
        assertEquals(9, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        List<IJavaElement> generatedJavaElements = new ArrayList<IJavaElement>();

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publishedAttribute, false);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements,
                getGeneratedJavaType(), publicAttribute, false);
        expectPropertyConstant(generatedJavaElements, genPublicChangeableAttribute);
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectGetSetOfAllowedValuesMethod(List<IJavaElement> javaElements,
            GenChangeableAttribute genChangeableAttribute,
            IType javaType) {

        IMethod expectedGetSetOfAllowedValuesMethod = javaType.getMethod(genChangeableAttribute
                .getMethodNameGetSetOfAllowedValues(), new String[] { "Q" + IValidationContext.class.getName() + ";" });
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

    private IType getGeneratedJavaTypeForProductCmptTypeGen(boolean forInterface) {
        StandardBuilderSet builderSet = genPolicyCmptType.getBuilderSet();
        BasePolicyCmptTypeBuilder policyCmptTypeBuilder = forInterface ? builderSet.getPolicyCmptInterfaceBuilder()
                : builderSet.getPolicyCmptImplClassBuilder();
        return policyCmptTypeBuilder.getGeneratedJavaTypeForProductCmptTypeGen(policyCmptType);
    }

}
