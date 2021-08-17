/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class DatatypeMismatchEntryTest extends AbstractIpsPluginTest {

    private IAttributeValue attrValue;
    private IConfiguredDefault configuredDefault;
    private IIpsProject ipsProject;
    private ProductCmpt productCmpt;
    private final List<String> result = new LinkedList<>();
    private List<DatatypeMismatchEntry> entries;
    private IConfiguredValueSet configuredValueSet;
    private Consumer<List<String>> valueConsumer = result::addAll;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        ProductCmptType productCmptType = (ProductCmptType)policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "ProductA");

        IProductCmptTypeAttribute attribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        attribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        attribute.setName("attribute");
        attribute.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        attrValue = spy(productCmpt.getAttributeValue("attribute"));
        attribute.setDatatype(Datatype.MONEY.getQualifiedName());

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute("polAttr");
        policyCmptTypeAttribute.setDatatype(Datatype.DECIMAL.getQualifiedName());
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        policyCmptTypeAttribute.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        configuredDefault = productCmpt.getPropertyValue(policyCmptTypeAttribute, IConfiguredDefault.class);
        configuredValueSet = productCmpt.getPropertyValue(policyCmptTypeAttribute, IConfiguredValueSet.class);

        policyCmptTypeAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
    }

    @Test
    public void testFix() throws Exception {
        DatatypeMismatchEntry datatypeMismatchEntry = new DatatypeMismatchEntry(attrValue,
                Collections.singletonList("10.00"), ValueConverter.TO_MONEY, valueConsumer);

        datatypeMismatchEntry.fix();

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is("10.00 EUR"));
    }

    @Test
    public void testFixForListWithUnconvertibleValues() {
        List<String> list = Arrays.asList("10.00", "10", "10.12341234");

        DatatypeMismatchEntry dataTypeMismatchEntry = new DatatypeMismatchEntry(attrValue, list,
                ValueConverter.TO_MONEY, valueConsumer);

        dataTypeMismatchEntry.fix();

        assertThat(result.size(), is(3));
        assertThat(result.get(0), is("10.00 EUR"));
        // € always has two digits after the .
        assertThat(result.get(1), is("10.00 EUR"));
        // can't convert because of precision loss
        assertThat(result.get(2), is("10.12341234"));
    }

    @Test
    public void testFixForEmptyList() {
        DatatypeMismatchEntry dataTypeMismatchEntry = new DatatypeMismatchEntry(attrValue,
                Collections.<String> emptyList(), ValueConverter.TO_MONEY, valueConsumer);

        dataTypeMismatchEntry.fix();

        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    public void testCreateAttributeValueMismatch() {
        assertThat(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(attrValue)).isEmpty(),
                equalTo(true));

        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.0"));
        assertThat(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(attrValue)).isEmpty(),
                equalTo(false));
    }

    @Test
    public void testCreateIfValidationFailsAttributeValue() throws CoreException {
        doThrow(new CoreException(Status.CANCEL_STATUS)).when(attrValue).validate(ipsProject);

        assertThat(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(attrValue)).isEmpty(),
                equalTo(true));
    }

    @Test
    public void testCreateConfiguredDefaultMismatch() {
        assertThat(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(configuredDefault)).isEmpty(),
                equalTo(true));

        configuredDefault.setValue("10.0");
        assertThat(DatatypeMismatchEntry.forEachMismatch(Collections.singletonList(configuredDefault)).isEmpty(),
                equalTo(false));
    }

    @Test
    public void testFixConfiguredDefault() throws Exception {
        DatatypeMismatchEntry datatypeMismatchEntry = new DatatypeMismatchEntry(configuredDefault,
                Collections.singletonList("10.00"), ValueConverter.TO_MONEY, valueConsumer);

        datatypeMismatchEntry.fix();

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is("10.00 EUR"));
    }

    @Test
    public void testFixConfiguredValueSetEnum() {
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        DatatypeMismatchEntry datatypeMismatchEntry = new DatatypeMismatchEntry(configuredValueSet,
                Collections.singletonList("10.00"), ValueConverter.TO_MONEY, valueConsumer);

        datatypeMismatchEntry.fix();

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is("10.00 EUR"));
    }

    @Test
    public void testFixConfiguredValueSetRange() {
        configuredValueSet.setValueSetType(ValueSetType.RANGE);
        DatatypeMismatchEntry datatypeMismatchEntry = new DatatypeMismatchEntry(configuredValueSet,
                Arrays.asList("1.00", "10.00", "1.00"), ValueConverter.TO_MONEY, valueConsumer);

        datatypeMismatchEntry.fix();

        assertThat(result.size(), is(3));
        assertThat(result.get(0), is("1.00 EUR"));
        assertThat(result.get(1), is("10.00 EUR"));
        assertThat(result.get(2), is("1.00 EUR"));
    }

    @Test
    public void testForEachMismatchSingleValue() throws CoreException {
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.0"));
        configuredDefault.setValue("11.0");

        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(attrValue, configuredDefault, mockFormula()));
        assertThat(entries.size(), is(2));
        assertThat(entries.get(0).getPropertyName(), is(attrValue.getPropertyName()));
        assertThat(entries.get(1).getPropertyName(), is(configuredDefault.getPropertyName()));
    }

    private IFormula mockFormula() throws CoreException {
        IFormula formula = mock(IFormula.class);
        when(formula.getIpsProject()).thenReturn(ipsProject);
        when(formula.validate(ipsProject)).thenReturn(new MessageList());
        return formula;
    }

    @Test
    public void testForEachMismatchConfiguredValueSetEnum() {
        configuredValueSet.setValueSet(new EnumValueSet(configuredValueSet, Arrays.asList("10.00", "5.00"),
                ipsProject.getIpsModel().getNextPartId(configuredValueSet)));

        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(configuredValueSet));
        assertThat(entries.size(), is(1));
        assertThat(entries.get(0).getPropertyName(), is(configuredValueSet.getPropertyName()));
    }

    @Test
    public void testForEachMismatchConfiguredValueSetRange() {
        configuredValueSet.setValueSet(new RangeValueSet(configuredValueSet,
                ipsProject.getIpsModel().getNextPartId(configuredValueSet), "0.00", "10.00", "1.00"));

        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(configuredValueSet));
        assertThat(entries.size(), is(1));
        assertThat(entries.get(0).getPropertyName(), is(configuredValueSet.getPropertyName()));
    }

    @Test
    public void testForEachMismatchMultiValue() {
        attrValue.setValueHolder(
                new MultiValueHolder(attrValue, Arrays.asList(new SingleValueHolder(attrValue, "10.99999"),
                        new SingleValueHolder(attrValue, "10"), new SingleValueHolder(attrValue, "10 EUR"))));
        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(attrValue));

        assertThat(entries.size(), is(1));
        assertThat(entries.get(0).getPropertyName(), is(attrValue.getPropertyName()));
    }

    @Test
    public void testForEachMismatchNoConversionNeeded() {
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "10.00 EUR"));
        entries = DatatypeMismatchEntry.forEachMismatch(Arrays.asList(attrValue));

        assertThat(entries.size(), is(0));
    }
}
