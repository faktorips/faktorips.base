/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XPolicyAttributeTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IPolicyCmptTypeAttribute attribute;

    @Mock
    private IPolicyCmptTypeAttribute superAttribute;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private XPolicyAttribute xSuperAttribute;

    private XPolicyAttribute xPolicyAttribute;

    private XPolicyCmptClass policyClass;

    @Before
    public void createXPolicyAttribute() throws Exception {
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        DatatypeHelper datatypeHelper = mock(DatatypeHelper.class);
        when(ipsProject.findDatatypeHelper(anyString())).thenReturn(datatypeHelper);
        when(datatypeHelper.getDatatype()).thenReturn(ValueDatatype.BOOLEAN);

        IPolicyCmptType polType = mock(IPolicyCmptType.class);
        when(attribute.getPolicyCmptType()).thenReturn(polType);

        policyClass = mock(XPolicyCmptClass.class);
        when(modelService.getModelNode(polType, XPolicyCmptClass.class, modelContext)).thenReturn(policyClass);

        xPolicyAttribute = new XPolicyAttribute(attribute, modelContext, modelService);
    }

    @Test
    public void productGenerationGetterName() throws Exception {
        xPolicyAttribute.getProductGenerationClassName();
        verify(policyClass).getProductCmptGenerationClassName();
    }

    @Test
    public void testIsGenerateAllowedValuesFor() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);

        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();
        assertEquals(false, generatedMethod);

        verify(xPolicyAttribute, never()).isValueSetEnum();
    }

    @Test
    public void testIsGenerateAllowedValuesForContentSeperatedEnum() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(true).when(xPolicyAttribute).isDatatypeExtensibleEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();
        assertEquals(false, generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesProductRelevantAndUnrestricted() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(false).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeExtensibleEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();
        assertEquals(true, generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesProductRelevantAndRestricted() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(false).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeExtensibleEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();
        assertEquals(true, generatedMethod);
        verify(xPolicyAttribute, never()).isDatatypeExtensibleEnum();
    }

    @Test
    public void testIsGenerateAllowedValuesProductRelevantAndEnumButNotContentSeperated() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeExtensibleEnum();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();
        assertEquals(true, generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesDerived() throws Exception {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isDerived();
        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();
        assertEquals(false, generatedMethod);
        verify(xPolicyAttribute, never()).isValueSetEnum();
        verify(xPolicyAttribute, never()).isValueSetUnrestricted();
        verify(xPolicyAttribute, never()).isProductRelevant();
        verify(xPolicyAttribute, never()).isDatatypeExtensibleEnum();
    }

    @Test
    public void testIsOverrideGetAllowedValuesFor() throws Exception {
        XPolicyAttribute superXPolicyAttribute = new XPolicyAttribute(superAttribute, modelContext, modelService);
        when(attribute.getName()).thenReturn("testAttribute");
        when(attribute.isOverwrite()).thenReturn(false);

        assertFalse(xPolicyAttribute.isOverrideGetAllowedValuesFor());

        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(any(IIpsProject.class))).thenReturn(superAttribute);
        when(attribute.getValueSet()).thenReturn(new RangeValueSet(attribute, "abc123"));
        when(modelService.getModelNode(superAttribute, XPolicyAttribute.class, modelContext)).thenReturn(
                superXPolicyAttribute);
        when(superAttribute.getIpsProject()).thenReturn(ipsProject);
        when(superAttribute.isChangeable()).thenReturn(false);

        assertFalse(xPolicyAttribute.isOverrideGetAllowedValuesFor());

        when(superAttribute.isChangeable()).thenReturn(true);
        when(superAttribute.getValueSet()).thenReturn(new RangeValueSet(attribute, "abc123"));
        when(superAttribute.getName()).thenReturn("testAttribute");

        assertTrue(xPolicyAttribute.isOverrideGetAllowedValuesFor());
    }

    @Test
    public void testIsGenerateInitWithProductData_notProductRelevant() throws Exception {
        when(attribute.isProductRelevant()).thenReturn(false);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(false);

        boolean generateInitWithProductData = xPolicyAttribute.isGenerateInitWithProductData();

        assertFalse(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_notCangeable() throws Exception {
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(false);
        when(attribute.isOverwrite()).thenReturn(false);

        boolean generateInitWithProductData = xPolicyAttribute.isGenerateInitWithProductData();

        assertFalse(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_notOverwrite() throws Exception {
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(false);

        boolean generateInitWithProductData = xPolicyAttribute.isGenerateInitWithProductData();

        assertTrue(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_typeNotChanged() throws Exception {
        XPolicyAttribute xPolicyAttributeSpy = spy(xPolicyAttribute);
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(true);
        when(xSuperAttribute.isDerivedOnTheFly()).thenReturn(false);
        when(xSuperAttribute.isProductRelevant()).thenReturn(true);
        when(xPolicyAttributeSpy.getOverwrittenAttribute()).thenReturn(xSuperAttribute);

        boolean generateInitWithProductData = xPolicyAttributeSpy.isGenerateInitWithProductData();

        assertFalse(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_typeChanged() throws Exception {
        XPolicyAttribute xPolicyAttributeSpy = spy(xPolicyAttribute);
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(true);
        when(xSuperAttribute.isDerivedOnTheFly()).thenReturn(true);
        when(xPolicyAttributeSpy.getOverwrittenAttribute()).thenReturn(xSuperAttribute);

        boolean generateInitWithProductData = xPolicyAttributeSpy.isGenerateInitWithProductData();

        assertTrue(generateInitWithProductData);
    }

    public void testIsGenerateSetterInternal_GenerateChangeSupportAndSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isGenerateChangeSupport();
        doReturn(true).when(xPolicyAttribute).isGenerateSetter();

        assertTrue(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateSetterInternal_DoNotGenerateChangeSupportNorSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isGenerateChangeSupport();
        doReturn(false).when(xPolicyAttribute).isGenerateSetter();

        assertFalse(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateSetterInternal_GenerateChangeSupportButDoNotGenerateSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isGenerateChangeSupport();
        doReturn(false).when(xPolicyAttribute).isGenerateSetter();

        assertFalse(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateSetterInternal_DoNotGenerateChangeSupportButGenerateSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isGenerateChangeSupport();
        doReturn(true).when(xPolicyAttribute).isGenerateSetter();

        assertFalse(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateInitWithoutProductData_ProductRelevant_Changeable() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        assertFalse(xPolicyAttribute.isGenerateInitWithoutProductData());
    }

    @Test
    public void testIsGenerateInitWithoutProductData_ProductRelevant_NotChangeable() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(false).when(xPolicyAttribute).isChangeable();

        assertFalse(xPolicyAttribute.isGenerateInitWithoutProductData());
    }

    @Test
    public void testIsGenerateInitWithoutProductData_NotProductRelevant_Changeable() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        assertTrue(xPolicyAttribute.isGenerateInitWithoutProductData());
    }

    @Test
    public void testIsGenerateInitWithoutProductData_NotProductRelevant_NotChangeable() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(false).when(xPolicyAttribute).isChangeable();

        assertFalse(xPolicyAttribute.isGenerateInitWithoutProductData());
    }

    @Test
    public void testIsProductRelevantInHierarchy() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        XPolicyAttribute superXPolicyAttribute = spy(new XPolicyAttribute(attribute, modelContext, modelService));
        doReturn(true).when(superXPolicyAttribute).isProductRelevant();
        doReturn(superXPolicyAttribute).when(xPolicyAttribute).getOverwrittenAttribute();

        assertTrue(xPolicyAttribute.isProductRelevantInHierarchy());
    }

    @Test
    public void testIsProductRelevantInHierarchy_Transitive() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        XPolicyAttribute superXPolicyAttribute = spy(new XPolicyAttribute(attribute, modelContext, modelService));
        doReturn(false).when(superXPolicyAttribute).isProductRelevant();
        doReturn(true).when(superXPolicyAttribute).isOverwrite();
        XPolicyAttribute superSuperXPolicyAttribute = spy(new XPolicyAttribute(attribute, modelContext, modelService));
        doReturn(true).when(superSuperXPolicyAttribute).isProductRelevant();

        doReturn(superSuperXPolicyAttribute).when(superXPolicyAttribute).getOverwrittenAttribute();
        doReturn(superXPolicyAttribute).when(xPolicyAttribute).getOverwrittenAttribute();

        assertTrue(xPolicyAttribute.isProductRelevantInHierarchy());
    }

    @Test
    public void testIsProductRelevantInHierarchy_Self() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        XPolicyAttribute superXPolicyAttribute = mock(XPolicyAttribute.class);
        doReturn(false).when(superXPolicyAttribute).isProductRelevant();
        doReturn(superXPolicyAttribute).when(xPolicyAttribute).getOverwrittenAttribute();

        assertTrue(xPolicyAttribute.isProductRelevantInHierarchy());
    }

    @Test
    public void testIsProductRelevantInHierarchy_NoOverwrite() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(false).when(xPolicyAttribute).isOverwrite();

        assertFalse(xPolicyAttribute.isProductRelevantInHierarchy());
    }

    @Test
    public void testIsOverrideGetDefaultValue() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        XPolicyAttribute superXPolicyAttribute = spy(new XPolicyAttribute(attribute, modelContext, modelService));
        doReturn(true).when(superXPolicyAttribute).isGenerateGetAllowedValuesForAndGetDefaultValue();
        doReturn(superXPolicyAttribute).when(xPolicyAttribute).getOverwrittenAttribute();

        assertTrue(xPolicyAttribute.isOverrideGetDefaultValue());
    }

    @Test
    public void testIsOverrideGetDefaultValue_NoOverwrite() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isOverwrite();

        assertFalse(xPolicyAttribute.isOverrideGetDefaultValue());
    }

    @Test
    public void testIsOverrideGetDefaultValue_OverwrittenAttributeDoesNotGenerateGetAllowedValuesForAndGetDefaultValue() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        XPolicyAttribute superXPolicyAttribute = spy(new XPolicyAttribute(attribute, modelContext, modelService));
        doReturn(false).when(superXPolicyAttribute).isGenerateGetAllowedValuesForAndGetDefaultValue();
        doReturn(superXPolicyAttribute).when(xPolicyAttribute).getOverwrittenAttribute();

        assertFalse(xPolicyAttribute.isOverrideGetDefaultValue());
    }

}
