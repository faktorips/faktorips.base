/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
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
    private GeneratorConfig generatorConfig;

    @Mock
    private ModelService modelService;

    @Mock
    private XPolicyAttribute xSuperAttribute;

    private XPolicyAttribute xPolicyAttribute;

    private XPolicyCmptClass policyClass;

    private DatatypeHelper datatypeHelper;

    @Before
    public void createXPolicyAttribute() {
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        datatypeHelper = mock(DatatypeHelper.class);
        when(ipsProject.findDatatypeHelper(anyString())).thenReturn(datatypeHelper);
        when(datatypeHelper.getDatatype()).thenReturn(ValueDatatype.BOOLEAN);

        IPolicyCmptType polType = mock(IPolicyCmptType.class);
        when(attribute.getPolicyCmptType()).thenReturn(polType);

        policyClass = mock(XPolicyCmptClass.class);
        when(modelService.getModelNode(polType, XPolicyCmptClass.class, modelContext)).thenReturn(policyClass);

        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);

        xPolicyAttribute = new XPolicyAttribute(attribute, modelContext, modelService);
    }

    @Test
    public void productGenerationGetterName() {
        xPolicyAttribute.getProductGenerationClassName();
        verify(policyClass).getProductCmptGenerationClassName();
    }

    @Test
    public void testIsGenerateAllowedValuesFor() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(new UnrestrictedValueSet(attribute, "unrestrictedSet")).when(attribute).getValueSet();
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();

        assertFalse(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesForAndGetDefaultValue_ContentSeperatedEnum() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesForAndGetDefaultValue_UnrestrictedAndNonProductRelevant() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).isContainsNull();
        doReturn(valueSet).when(attribute).getValueSet();

        assertFalse(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesForAndGetDefaultValue_UnrestrictedWithoutNullAndNonProductRelevant() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(false).when(valueSet).isContainsNull();
        doReturn(valueSet).when(attribute).getValueSet();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesForAndGetDefaultValue_UnrestrictedPrimitiveAndNonProductRelevant() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(false).when(valueSet).isContainsNull();
        doReturn(valueSet).when(attribute).getValueSet();
        when(datatypeHelper.getDatatype()).thenReturn(ValueDatatype.PRIMITIVE_INT);

        assertFalse(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesForAndGetDefaultValue_EnumAndNonProductRelevant() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesAndGetDefaultValue_ProductRelevantAndUnrestricted() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(false).when(valueSet).isContainsNull();
        doReturn(valueSet).when(attribute).getValueSet();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesAndGetDefaultValue_ProductRelevantAndUnrestrictedWithNull() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).isContainsNull();
        doReturn(valueSet).when(attribute).getValueSet();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesAndGetDefaultValue_ProductRelevantAndRestricted() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesAndGetDefaultValue_ProductRelevantAndEnumButNotContentSeperated() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isValueSetUnrestricted();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isChangeable();
        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeExtensibleEnum();

        assertTrue(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue());
    }

    @Test
    public void testIsGenerateAllowedValuesAndGetDefaultValue_Derived() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(new RangeValueSet(attribute, "rangeSet")).when(attribute).getValueSet();
        doReturn(true).when(xPolicyAttribute).isDerived();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();

        assertTrue(generatedMethod);
    }

    @Test
    public void testIsGenerateAllowedValuesAndGetDefaultValue_UnrestrictedNonChangeableProductRelevant() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(AttributeType.DERIVED_ON_THE_FLY).when(attribute).getAttributeType();
        doReturn(new UnrestrictedValueSet(attribute, "set")).when(attribute).getValueSet();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();

        boolean generatedMethod = xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue();

        assertFalse(generatedMethod);
    }

    @Test
    public void testIsOverrideGetAllowedValuesFor_NoOverride() {
        when(attribute.getName()).thenReturn("testAttribute");
        when(attribute.isOverwrite()).thenReturn(false);

        assertFalse(xPolicyAttribute.isOverrideGetAllowedValuesFor());
    }

    @Test
    public void testIsOverrideGetAllowedValuesFor_SuperConstant() {
        XPolicyAttribute superXPolicyAttribute = new XPolicyAttribute(superAttribute, modelContext, modelService);
        when(attribute.getName()).thenReturn("testAttribute");
        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(any(IIpsProject.class))).thenReturn(superAttribute);
        when(attribute.getValueSet()).thenReturn(new RangeValueSet(attribute, "abc123"));
        when(modelService.getModelNode(superAttribute, XPolicyAttribute.class, modelContext))
                .thenReturn(superXPolicyAttribute);
        when(superAttribute.getIpsProject()).thenReturn(ipsProject);
        when(superAttribute.getAttributeType()).thenReturn(AttributeType.CONSTANT);

        assertFalse(xPolicyAttribute.isOverrideGetAllowedValuesFor());
    }

    @Test
    public void testIsOverrideGetAllowedValuesFor_SuperOverride() {
        XPolicyAttribute superXPolicyAttribute = new XPolicyAttribute(superAttribute, modelContext, modelService);
        when(attribute.getName()).thenReturn("testAttribute");
        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(any(IIpsProject.class))).thenReturn(superAttribute);
        when(attribute.getValueSet()).thenReturn(new RangeValueSet(attribute, "abc123"));
        when(modelService.getModelNode(superAttribute, XPolicyAttribute.class, modelContext))
                .thenReturn(superXPolicyAttribute);
        when(superAttribute.getIpsProject()).thenReturn(ipsProject);
        when(superAttribute.getAttributeType()).thenReturn(AttributeType.CHANGEABLE);
        when(superAttribute.getValueSet()).thenReturn(new RangeValueSet(superAttribute, "abc123"));
        when(superAttribute.getName()).thenReturn("testAttribute");

        assertTrue(xPolicyAttribute.isOverrideGetAllowedValuesFor());
    }

    @Test
    public void testIsGenerateInitWithProductData_NotProductRelevant() {
        when(attribute.isProductRelevant()).thenReturn(false);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(false);

        boolean generateInitWithProductData = xPolicyAttribute.isGenerateInitWithProductData();

        assertFalse(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_NotCangeable() {
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(false);
        when(attribute.isOverwrite()).thenReturn(false);

        boolean generateInitWithProductData = xPolicyAttribute.isGenerateInitWithProductData();

        assertFalse(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_NotOverwrite() {
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(false);

        boolean generateInitWithProductData = xPolicyAttribute.isGenerateInitWithProductData();

        assertTrue(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateInitWithProductData_TypeNotChanged() {
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
    public void testIsGenerateInitWithProductData_TypeChanged() {
        XPolicyAttribute xPolicyAttributeSpy = spy(xPolicyAttribute);
        when(attribute.isProductRelevant()).thenReturn(true);
        when(attribute.isChangeable()).thenReturn(true);
        when(attribute.isOverwrite()).thenReturn(true);
        when(xSuperAttribute.isDerivedOnTheFly()).thenReturn(true);
        when(xPolicyAttributeSpy.getOverwrittenAttribute()).thenReturn(xSuperAttribute);

        boolean generateInitWithProductData = xPolicyAttributeSpy.isGenerateInitWithProductData();

        assertTrue(generateInitWithProductData);
    }

    @Test
    public void testIsGenerateSetterInternal_GenerateChangeSupportAndSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(generatorConfig).isGenerateChangeSupport();
        doReturn(true).when(xPolicyAttribute).isGenerateSetter();

        assertTrue(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateSetterInternal_DoNotGenerateChangeSupportNorSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(generatorConfig).isGenerateChangeSupport();
        doReturn(false).when(xPolicyAttribute).isGenerateSetter();

        assertFalse(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateSetterInternal_GenerateChangeSupportButDoNotGenerateSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(generatorConfig).isGenerateChangeSupport();
        doReturn(false).when(xPolicyAttribute).isGenerateSetter();

        assertFalse(xPolicyAttribute.isGenerateSetterInternal());
    }

    @Test
    public void testIsGenerateSetterInternal_DoNotGenerateChangeSupportButGenerateSetters() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(generatorConfig).isGenerateChangeSupport();
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

    @Test
    public void testGetDefaultValueCode() {
        EnumTypeDatatypeAdapter adapter = mock(EnumTypeDatatypeAdapter.class);
        IEnumType enumType = mock(IEnumType.class);
        when(datatypeHelper.getDatatype()).thenReturn(adapter);
        when(adapter.getEnumType()).thenReturn(enumType);
        when(enumType.isExtensible()).thenReturn(true);

        String defaultValueCode = xPolicyAttribute.getDefaultValueCode();
        assertEquals("null", defaultValueCode);
    }

    @Test
    public void testIsGenerateConstantForValueSetEnumNonExtensible() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isValueSetRange();
        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(false).when(xPolicyAttribute).isDatatypeExtensibleEnum();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(ValueSetType.ENUM).when(valueSet).getValueSetType();
        doReturn(valueSet).when(attribute).getValueSet();

        assertTrue(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForValueSetEnumExtensible() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isValueSetRange();
        doReturn(true).when(xPolicyAttribute).isValueSetEnum();
        doReturn(true).when(xPolicyAttribute).isDatatypeExtensibleEnum();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(ValueSetType.ENUM).when(valueSet).getValueSetType();
        doReturn(valueSet).when(attribute).getValueSet();

        assertFalse(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForValueSetRange() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isValueSetRange();

        assertTrue(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForValueSetRangeWithoutValues() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isValueSetRange();

        assertTrue(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForProductRelevantValueSetRangeWithoutValues() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(true).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(true).when(xPolicyAttribute).isProductRelevant();
        doReturn(true).when(xPolicyAttribute).isValueSetRange();

        assertFalse(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForValueSetUnrestrictedWithoutNull() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(false).when(xPolicyAttribute).isValueSetRange();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(false).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        doReturn(valueSet).when(attribute).getValueSet();

        assertTrue(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForValueSetUnrestrictedPrimitive() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(false).when(xPolicyAttribute).isValueSetRange();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(false).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        doReturn(valueSet).when(attribute).getValueSet();
        when(datatypeHelper.getDatatype()).thenReturn(ValueDatatype.PRIMITIVE_INT);

        assertFalse(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateConstantForValueSetUnrestrictedWithNull() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(false).when(xPolicyAttribute).isAbstractValueSet();
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        doReturn(false).when(xPolicyAttribute).isValueSetRange();
        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        doReturn(valueSet).when(attribute).getValueSet();

        assertFalse(xPolicyAttribute.isGenerateConstantForValueSet());
    }

    @Test
    public void testIsGenerateGetAllowedValuesForAndGetDefaultValue_isNotProductConfiguredButSuperTypeIs() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(xSuperAttribute).when(xPolicyAttribute).getOverwrittenAttribute();
        doReturn(superAttribute).when(xSuperAttribute).getAttribute();

        doReturn(false).when(xPolicyAttribute).isValueSetConfiguredByProduct();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        doReturn(true).when(xSuperAttribute).isValueSetConfiguredByProduct();
        doReturn(false).when(xSuperAttribute).isOverwrite();
        doReturn(true).when(xSuperAttribute).isOverrideGetAllowedValuesFor();

        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        when(attribute.getValueSet()).thenReturn(valueSet);

        IValueSet superValueSet = mock(IValueSet.class);
        doReturn(false).when(superValueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(superValueSet).getValueSetType();
        doReturn(superValueSet).when(superAttribute).getValueSet();

        assertThat(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue(), is(true));
    }

    @Test
    public void testIsGenerateGetAllowedValuesForAndGetDefaultValue_isProductConfigured() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(xSuperAttribute).when(xPolicyAttribute).getOverwrittenAttribute();
        doReturn(superAttribute).when(xSuperAttribute).getAttribute();

        doReturn(true).when(xPolicyAttribute).isValueSetConfiguredByProduct();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        doReturn(true).when(xSuperAttribute).isValueSetConfiguredByProduct();
        doReturn(false).when(xSuperAttribute).isOverwrite();
        doReturn(true).when(xSuperAttribute).isOverrideGetAllowedValuesFor();

        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        when(attribute.getValueSet()).thenReturn(valueSet);

        IValueSet superValueSet = mock(IValueSet.class);
        doReturn(false).when(superValueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(superValueSet).getValueSetType();
        doReturn(superValueSet).when(superAttribute).getValueSet();

        assertThat(xPolicyAttribute.isGenerateGetAllowedValuesForAndGetDefaultValue(), is(false));
    }

    @Test
    public void testIsGenerateConstantForValueSet_isNotProductConfiguredButSuperTypeIs() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(xSuperAttribute).when(xPolicyAttribute).getOverwrittenAttribute();
        doReturn(superAttribute).when(xSuperAttribute).getAttribute();

        doReturn(false).when(xPolicyAttribute).isValueSetConfiguredByProduct();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        doReturn(true).when(xSuperAttribute).isValueSetConfiguredByProduct();
        doReturn(false).when(xSuperAttribute).isOverwrite();
        doReturn(true).when(xSuperAttribute).isOverrideGetAllowedValuesFor();

        IValueSet valueSet = mock(IValueSet.class);
        doReturn(false).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        when(attribute.getValueSet()).thenReturn(valueSet);

        IValueSet superValueSet = mock(IValueSet.class);
        doReturn(false).when(superValueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(superValueSet).getValueSetType();
        doReturn(superValueSet).when(superAttribute).getValueSet();

        assertThat(xPolicyAttribute.isGenerateConstantForValueSet(), is(true));

    }

    @Test
    public void testIsGenerateConstantForValueSet_isProductConfigured() {
        xPolicyAttribute = spy(xPolicyAttribute);
        doReturn(xSuperAttribute).when(xPolicyAttribute).getOverwrittenAttribute();
        doReturn(superAttribute).when(xSuperAttribute).getAttribute();

        doReturn(true).when(xPolicyAttribute).isValueSetConfiguredByProduct();
        doReturn(true).when(xPolicyAttribute).isOverwrite();

        doReturn(true).when(xSuperAttribute).isValueSetConfiguredByProduct();
        doReturn(false).when(xSuperAttribute).isOverwrite();
        doReturn(true).when(xSuperAttribute).isOverrideGetAllowedValuesFor();

        IValueSet valueSet = mock(IValueSet.class);
        doReturn(true).when(valueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(valueSet).getValueSetType();
        when(attribute.getValueSet()).thenReturn(valueSet);

        IValueSet superValueSet = mock(IValueSet.class);
        doReturn(false).when(superValueSet).isContainsNull();
        doReturn(ValueSetType.UNRESTRICTED).when(superValueSet).getValueSetType();
        doReturn(superValueSet).when(superAttribute).getValueSet();

        assertThat(xPolicyAttribute.isGenerateConstantForValueSet(), is(false));
    }

    @Test
    public void testIsAbstract() {
        xPolicyAttribute = spy(xPolicyAttribute);
        var abstractDatatype = mock(ValueDatatype.class);
        when(abstractDatatype.isAbstract()).thenReturn(true);
        when(datatypeHelper.getDatatype()).thenReturn(abstractDatatype);
        doReturn(true).when(xPolicyAttribute).isProductRelevant();

        assertThat(xPolicyAttribute.isAbstract(), is(true));
    }

    @Test
    public void testIsAbstract_NotProductRelevant() {
        xPolicyAttribute = spy(xPolicyAttribute);
        var abstractDatatype = mock(ValueDatatype.class);
        when(abstractDatatype.isAbstract()).thenReturn(true);
        when(datatypeHelper.getDatatype()).thenReturn(abstractDatatype);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();

        assertThat(xPolicyAttribute.isAbstract(), is(false));
    }

    @Test
    public void testIsAbstract_NotProductRelevant_AbstractType() {
        xPolicyAttribute = spy(xPolicyAttribute);
        var abstractDatatype = mock(ValueDatatype.class);
        when(abstractDatatype.isAbstract()).thenReturn(true);
        when(datatypeHelper.getDatatype()).thenReturn(abstractDatatype);
        doReturn(false).when(xPolicyAttribute).isProductRelevant();
        when(policyClass.isAbstract()).thenReturn(true);

        assertThat(xPolicyAttribute.isAbstract(), is(true));
    }

    @Test
    public void testIsAbstract_Not() {
        xPolicyAttribute = spy(xPolicyAttribute);
        var abstractDatatype = mock(ValueDatatype.class);
        when(abstractDatatype.isAbstract()).thenReturn(false);
        when(datatypeHelper.getDatatype()).thenReturn(abstractDatatype);
        doReturn(true).when(xPolicyAttribute).isProductRelevant();

        assertThat(xPolicyAttribute.isAbstract(), is(false));
    }
}
