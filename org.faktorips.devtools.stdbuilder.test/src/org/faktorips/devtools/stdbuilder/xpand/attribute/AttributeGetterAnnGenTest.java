/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.attribute;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeGetterAnnGenTest {

    @Mock
    private GeneratorModelContext modelContext;

    private AttributeGetterAnnGen attributeGetterAnnGen = new AttributeGetterAnnGen();

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertThat(attributeGetterAnnGen.isGenerateAnnotationFor(mock(XPolicyAttribute.class)), is(true));
        assertThat(attributeGetterAnnGen.isGenerateAnnotationFor(mock(XProductAttribute.class)), is(true));
        assertThat(attributeGetterAnnGen.isGenerateAnnotationFor(mock(XProductAssociation.class)), is(false));
    }

    @Test
    public void testCreateAnnotation_product_enum() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.ENUM);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"foo\", type = AttributeType.CONSTANT, valueSetType = ValueSetType.Enum)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_product_range() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.RANGE);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"foo\", type = AttributeType.CONSTANT, valueSetType = ValueSetType.Range)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_product_unrestricted() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"foo\", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_enum() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.ENUM);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Enum)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_range() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.RANGE);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.Range)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_unrestricted() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_constant() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CONSTANT, ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.CONSTANT, valueSetType = ValueSetType.AllValues)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_computed() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL,
                ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL, valueSetType = ValueSetType.AllValues)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_derived() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.DERIVED_ON_THE_FLY,
                ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.DERIVED_ON_THE_FLY, valueSetType = ValueSetType.AllValues)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_notChangingOverTime() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.UNRESTRICTED);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = xPolicyAttribute.getAttribute();
        when(policyCmptTypeAttribute.isProductRelevant()).thenReturn(true);
        when(policyCmptTypeAttribute.isChangingOverTime()).thenReturn(false);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)"
                        + System.getProperty("line.separator") + "@IpsConfiguredAttribute(changingOverTime = false)" + System
                        .getProperty("line.separator")))));
    }

    @Test
    public void testCreateAnnotation_policy_changingOverTime() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.UNRESTRICTED);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = xPolicyAttribute.getAttribute();
        when(policyCmptTypeAttribute.isProductRelevant()).thenReturn(true);
        when(policyCmptTypeAttribute.isChangingOverTime()).thenReturn(true);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(("@IpsAttribute(name = \"bar\", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)"
                        + System.getProperty("line.separator") + "@IpsConfiguredAttribute(changingOverTime = true)" + System
                        .getProperty("line.separator")))));
    }

    private XPolicyAttribute xPolicyAttribute(String name, AttributeType attributeType, ValueSetType valueSetType) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = mock(IPolicyCmptTypeAttribute.class);
        when(policyCmptTypeAttribute.getName()).thenReturn(name);
        when(policyCmptTypeAttribute.getAttributeType()).thenReturn(attributeType);
        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.getValueSetType()).thenReturn(valueSetType);
        when(policyCmptTypeAttribute.getValueSet()).thenReturn(valueSet);
        return new XPolicyAttribute(policyCmptTypeAttribute, modelContext, null);
    }

    private XProductAttribute xProductAttribute(String name, ValueSetType valueSetType) {
        IProductCmptTypeAttribute productCmptTypeAttribute = mock(IProductCmptTypeAttribute.class);
        when(productCmptTypeAttribute.getName()).thenReturn(name);
        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.getValueSetType()).thenReturn(valueSetType);
        when(productCmptTypeAttribute.getValueSet()).thenReturn(valueSet);
        return new XProductAttribute(productCmptTypeAttribute, modelContext, null);
    }

}
