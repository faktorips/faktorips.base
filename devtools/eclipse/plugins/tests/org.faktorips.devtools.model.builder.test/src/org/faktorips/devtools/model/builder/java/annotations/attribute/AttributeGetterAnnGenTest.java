/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.attribute;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
                is(equalTo(
                        "@IpsAttribute(name = \"foo\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Enum)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_product_range() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.RANGE);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"foo\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Range)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_product_unrestricted() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"foo\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_product_MultiValuePrimitive() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.UNRESTRICTED, true);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"foo\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues, primitive = true)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_product_stringlength() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.STRINGLENGTH);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"foo\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.StringLength)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_product_derived() throws Exception {
        XProductAttribute xProductAttribute = xProductAttribute("foo", ValueSetType.DERIVED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xProductAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"foo\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.Derived)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_enum() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.ENUM);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Enum)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_range() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE, ValueSetType.RANGE);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Range)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_unrestricted() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE,
                ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        ("@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator()))));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_stringlength() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE,
                ValueSetType.STRINGLENGTH);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        ("@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.StringLength)"
                                + System.lineSeparator()))));
    }

    @Test
    public void testCreateAnnotation_policy_changeable_derived() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE,
                ValueSetType.DERIVED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        ("@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.Derived)"
                                + System.lineSeparator()))));
    }

    @Test
    public void testCreateAnnotation_policy_constant() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CONSTANT, ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.CONSTANT, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_computed() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL,
                ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.DERIVED_BY_EXPLICIT_METHOD_CALL, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_derived() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.DERIVED_ON_THE_FLY,
                ValueSetType.UNRESTRICTED);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.DERIVED_ON_THE_FLY, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_notChangingOverTime() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE,
                ValueSetType.UNRESTRICTED);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = xPolicyAttribute.getAttribute();
        when(policyCmptTypeAttribute.isProductRelevant()).thenReturn(true);
        when(policyCmptTypeAttribute.isChangingOverTime()).thenReturn(false);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator() + "@IpsConfiguredAttribute(changingOverTime = false)"
                                + System.lineSeparator())));
    }

    @Test
    public void testCreateAnnotation_policy_changingOverTime() throws Exception {
        XPolicyAttribute xPolicyAttribute = xPolicyAttribute("bar", AttributeType.CHANGEABLE,
                ValueSetType.UNRESTRICTED);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = xPolicyAttribute.getAttribute();
        when(policyCmptTypeAttribute.isProductRelevant()).thenReturn(true);
        when(policyCmptTypeAttribute.isChangingOverTime()).thenReturn(true);

        JavaCodeFragment codeFragment = attributeGetterAnnGen.createAnnotation(xPolicyAttribute);

        assertThat(
                codeFragment.getSourcecode(),
                is(equalTo(
                        "@IpsAttribute(name = \"bar\", kind = AttributeKind.CHANGEABLE, valueSetKind = ValueSetKind.AllValues)"
                                + System.lineSeparator() + "@IpsConfiguredAttribute(changingOverTime = true)"
                                + System.lineSeparator())));
    }

    private XPolicyAttribute xPolicyAttribute(String name, AttributeType attributeType, ValueSetType valueSetType) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = mock(IPolicyCmptTypeAttribute.class);
        when(policyCmptTypeAttribute.getName()).thenReturn(name);
        when(policyCmptTypeAttribute.getAttributeType()).thenReturn(attributeType);
        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.getValueSetType()).thenReturn(valueSetType);
        when(policyCmptTypeAttribute.getValueSet()).thenReturn(valueSet);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(policyCmptTypeAttribute.getIpsProject()).thenReturn(ipsProject);
        when(policyCmptTypeAttribute.getDatatype()).thenReturn("fake");
        DatatypeHelper datatypeHelper = mock(DatatypeHelper.class);
        ValueDatatype datatype = mock(ValueDatatype.class);
        when(datatype.isPrimitive()).thenReturn(false);
        when(datatypeHelper.getDatatype()).thenReturn(datatype);
        when(ipsProject.findDatatypeHelper("fake")).thenReturn(datatypeHelper);
        return new XPolicyAttribute(policyCmptTypeAttribute, modelContext, null);
    }

    private XProductAttribute xProductAttribute(String name, ValueSetType valueSetType) {
        return xProductAttribute(name, valueSetType, false);
    }

    private XProductAttribute xProductAttribute(String name, ValueSetType valueSetType, boolean primitive) {
        IProductCmptTypeAttribute productCmptTypeAttribute = mock(IProductCmptTypeAttribute.class);
        when(productCmptTypeAttribute.getName()).thenReturn(name);
        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.getValueSetType()).thenReturn(valueSetType);
        when(productCmptTypeAttribute.getValueSet()).thenReturn(valueSet);
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(productCmptTypeAttribute.getIpsProject()).thenReturn(ipsProject);
        when(productCmptTypeAttribute.getDatatype()).thenReturn("fake");
        DatatypeHelper datatypeHelper = mock(DatatypeHelper.class);
        ValueDatatype datatype = mock(ValueDatatype.class);
        when(datatype.isPrimitive()).thenReturn(primitive);
        when(datatypeHelper.getDatatype()).thenReturn(datatype);
        when(ipsProject.findDatatypeHelper("fake")).thenReturn(datatypeHelper);
        return new XProductAttribute(productCmptTypeAttribute, modelContext, null);
    }

}
