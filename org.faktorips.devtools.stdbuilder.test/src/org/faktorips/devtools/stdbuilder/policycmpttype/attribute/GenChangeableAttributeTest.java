/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
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
        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(javaInterface, genPublishedChangeableAttribute);
        expectGetterMethod(javaInterface, genPublishedChangeableAttribute);
        expectSetterMethod(javaInterface, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevant() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        performGetGeneratedJavaElementsForPublishedInterfaceProductRelevant();

        publishedAttribute.setValueSetType(ValueSetType.ENUM);
        publicAttribute.setValueSetType(ValueSetType.ENUM);
        performGetGeneratedJavaElementsForPublishedInterfaceProductRelevant();

        publishedAttribute.setValueSetType(ValueSetType.RANGE);
        publicAttribute.setValueSetType(ValueSetType.RANGE);
        performGetGeneratedJavaElementsForPublishedInterfaceProductRelevant();
    }

    private void performGetGeneratedJavaElementsForPublishedInterfaceProductRelevant() throws CoreException {
        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(javaInterface, genPublishedChangeableAttribute);
        expectGetterMethod(javaInterface, genPublishedChangeableAttribute);
        expectSetterMethod(javaInterface, genPublishedChangeableAttribute);
        expectGetValueSetMethod(javaInterface, genPublishedChangeableAttribute);

        IType javaInterfaceProductGen = genPublishedChangeableAttribute
                .findGeneratedJavaTypeForProductCmptTypeGen(true);
        expectGetValueSetMethod(javaInterfaceProductGen, genPublishedChangeableAttribute);
        expectGetDefaultValueMethod(javaInterfaceProductGen, genPublishedChangeableAttribute);
        assertEquals(6, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(javaInterface, genPublishedChangeableAttribute);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        performGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten();

        publishedAttribute.setValueSetType(ValueSetType.ENUM);
        publicAttribute.setValueSetType(ValueSetType.ENUM);
        performGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten();

        publishedAttribute.setValueSetType(ValueSetType.RANGE);
        publicAttribute.setValueSetType(ValueSetType.RANGE);
        performGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten();
    }

    private void performGetGeneratedJavaElementsForPublishedInterfaceProductRelevantOverwritten() throws CoreException {
        genPublishedChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publishedAttribute);
        expectPropertyConstant(javaInterface, genPublishedChangeableAttribute);
        expectGetValueSetMethod(javaInterface, genPublishedChangeableAttribute);

        IType javaInterfaceProductGen = genPublishedChangeableAttribute
                .findGeneratedJavaTypeForProductCmptTypeGen(true);
        expectGetValueSetMethod(javaInterfaceProductGen, genPublishedChangeableAttribute);
        expectGetDefaultValueMethod(javaInterfaceProductGen, genPublishedChangeableAttribute);
        assertEquals(4, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterface, publicAttribute);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectMemberVar(javaClass, genPublishedChangeableAttribute);
        expectGetterMethod(javaClass, genPublishedChangeableAttribute);
        expectSetterMethod(javaClass, genPublishedChangeableAttribute);
        assertEquals(3, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(javaClass, genPublicChangeableAttribute);
        expectMemberVar(javaClass, genPublicChangeableAttribute);
        expectGetterMethod(javaClass, genPublicChangeableAttribute);
        expectSetterMethod(javaClass, genPublicChangeableAttribute);
        assertEquals(4, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevant() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        performGetGeneratedJavaElementsForImplementationProductRelevant();

        publishedAttribute.setValueSetType(ValueSetType.ENUM);
        publicAttribute.setValueSetType(ValueSetType.ENUM);
        performGetGeneratedJavaElementsForImplementationProductRelevant();

        publishedAttribute.setValueSetType(ValueSetType.RANGE);
        publicAttribute.setValueSetType(ValueSetType.RANGE);
        performGetGeneratedJavaElementsForImplementationProductRelevant();
    }

    private void performGetGeneratedJavaElementsForImplementationProductRelevant() throws CoreException {
        generatedJavaElements.clear();
        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        expectMemberVar(javaClass, genPublishedChangeableAttribute);
        expectGetterMethod(javaClass, genPublishedChangeableAttribute);
        expectSetterMethod(javaClass, genPublishedChangeableAttribute);
        expectGetValueSetMethod(javaClass, genPublishedChangeableAttribute);

        IType javaClassProductGen = genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false);
        expectDefaultMemberVariable(javaClassProductGen, genPublishedChangeableAttribute);
        expectValueSetMemberVariable(javaClassProductGen, genPublishedChangeableAttribute);
        expectGetDefaultValueMethod(javaClassProductGen, genPublishedChangeableAttribute);
        expectGetValueSetMethod(javaClassProductGen, genPublishedChangeableAttribute);
        assertEquals(8, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(javaClass, genPublicChangeableAttribute);
        expectMemberVar(javaClass, genPublicChangeableAttribute);
        expectGetterMethod(javaClass, genPublicChangeableAttribute);
        expectSetterMethod(javaClass, genPublicChangeableAttribute);
        expectGetValueSetMethod(javaClass, genPublicChangeableAttribute);

        javaClassProductGen = genPublicChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false);
        expectDefaultMemberVariable(javaClassProductGen, genPublicChangeableAttribute);
        expectValueSetMemberVariable(javaClassProductGen, genPublicChangeableAttribute);
        expectGetDefaultValueMethod(javaClassProductGen, genPublicChangeableAttribute);
        expectGetValueSetMethod(javaClassProductGen, genPublicChangeableAttribute);
        assertEquals(9, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationOverwritten() {
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);

        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        expectPropertyConstant(javaClass, genPublicChangeableAttribute);
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationProductRelevantOverwritten() throws CoreException {
        publishedAttribute.setProductRelevant(true);
        publicAttribute.setProductRelevant(true);
        publishedAttribute.setOverwrite(true);
        publicAttribute.setOverwrite(true);
        performGetGeneratedJavaElementsForImplementationProductRelevantOverwritten();

        publishedAttribute.setValueSetType(ValueSetType.ENUM);
        publicAttribute.setValueSetType(ValueSetType.ENUM);
        performGetGeneratedJavaElementsForImplementationProductRelevantOverwritten();

        publishedAttribute.setValueSetType(ValueSetType.RANGE);
        publicAttribute.setValueSetType(ValueSetType.RANGE);
        performGetGeneratedJavaElementsForImplementationProductRelevantOverwritten();
    }

    private void performGetGeneratedJavaElementsForImplementationProductRelevantOverwritten() throws CoreException {
        generatedJavaElements.clear();
        genPublishedChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publishedAttribute);
        IType javaClassProductGen = genPublishedChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false);
        expectDefaultMemberVariable(javaClassProductGen, genPublishedChangeableAttribute);
        expectValueSetMemberVariable(javaClassProductGen, genPublishedChangeableAttribute);
        expectGetValueSetMethod(javaClass, genPublishedChangeableAttribute);
        expectGetValueSetMethod(javaClassProductGen, genPublishedChangeableAttribute);
        expectGetDefaultValueMethod(javaClassProductGen, genPublishedChangeableAttribute);
        assertEquals(5, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicChangeableAttribute.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass,
                publicAttribute);
        javaClassProductGen = genPublicChangeableAttribute.findGeneratedJavaTypeForProductCmptTypeGen(false);
        expectPropertyConstant(javaClass, genPublicChangeableAttribute);
        expectDefaultMemberVariable(javaClassProductGen, genPublicChangeableAttribute);
        expectValueSetMemberVariable(javaClassProductGen, genPublicChangeableAttribute);
        expectGetValueSetMethod(javaClass, genPublicChangeableAttribute);
        expectGetValueSetMethod(javaClassProductGen, genPublicChangeableAttribute);
        expectGetDefaultValueMethod(javaClassProductGen, genPublicChangeableAttribute);
        assertEquals(6, generatedJavaElements.size());
    }

    private void expectGetValueSetMethod(IType javaType, GenChangeableAttribute genChangeableAttribute) {
        String methodName = genChangeableAttribute.getMethodNameGetSetOfAllowedValues();
        String[] parameterTypeSignatures = new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" };
        IMethod expectedGetSetOfAllowedValuesMethod = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(expectedGetSetOfAllowedValuesMethod));
    }

    private void expectGetDefaultValueMethod(IType javaType, GenChangeableAttribute genChangeableAttribute) {
        String methodName = genChangeableAttribute.getMethodNameGetDefaultValue();
        IMethod expectedGetDefaultValueMethod = javaType.getMethod(methodName, new String[] {});
        assertTrue(generatedJavaElements.contains(expectedGetDefaultValueMethod));
    }

    private void expectDefaultMemberVariable(IType javaType, GenChangeableAttribute genChangeableAttribute) {
        String fieldName = genChangeableAttribute.getFieldNameDefaultValue();
        IField expectedDefaultMemberVariable = javaType.getField(fieldName);
        assertTrue(generatedJavaElements.contains(expectedDefaultMemberVariable));
    }

    private void expectValueSetMemberVariable(IType javaType, GenChangeableAttribute genChangeableAttribute) {
        String fieldName = genChangeableAttribute.getFieldNameSetOfAllowedValues();
        IField expectedSetOfAllowedValuesMemberVariable = javaType.getField(fieldName);
        assertTrue(generatedJavaElements.contains(expectedSetOfAllowedValuesMemberVariable));
    }

}
