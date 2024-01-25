/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Comparator;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.ConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PropertyValueTypeTest {

    private static final String ANY_VALUE = "anyValue";

    @Mock
    private IValueSet valueSet1;

    @Mock
    private IValueSet valueSet2;

    @Mock
    private IAttributeValue attributeValue;

    @Mock
    private IConfiguredDefault configuredDefault;

    @Mock
    private IConfiguredValueSet configuredValueSet;

    @Mock
    private ITableContentUsage tableContentUsage;

    @Mock
    private IFormula formula;

    @Mock
    private IValidationRuleConfig ruleConfig;

    @Test
    public void testGetValueComparator_other_eq() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertThat(valueComparator.compare("abc", "abc"), is(0));
    }

    @Test
    public void testGetValueComparator_other_gt() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertTrue(valueComparator.compare("abx", "abc") > 0);
    }

    @Test
    public void testGetValueComparator_other_lt() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertTrue(valueComparator.compare("abc", "abx") < 0);
    }

    @Test
    public void testGetValueComparator_nullValues() throws Exception {
        Comparator<Object> valueComparator = PropertyValueType.ATTRIBUTE_VALUE.getValueComparator();
        assertTrue(valueComparator.compare(null, "abx") < 0);
        assertTrue(valueComparator.compare("abc", null) > 0);
        assertTrue(valueComparator.compare(null, null) == 0);
    }

    @Test
    public void testGetValueGetter_attributeValue() throws Exception {
        IValueHolder<?> value = mock(IValueHolder.class);
        doReturn(value).when(attributeValue).getValueHolder();

        assertThat(PropertyValueType.ATTRIBUTE_VALUE.getValueGetter().apply(attributeValue), is((Object)value));
    }

    @Test
    public void testGetValueGetter_configuredDefault() throws Exception {
        when(configuredDefault.getValue()).thenReturn("10");

        assertThat(PropertyValueType.CONFIGURED_DEFAULT.getValueGetter().apply(configuredDefault), is((Object)"10"));
    }

    @Test
    public void testGetValueGetter_configuredValueSet() throws Exception {
        when(configuredValueSet.getValueSet()).thenReturn(valueSet1);

        assertThat(PropertyValueType.CONFIGURED_VALUESET.getValueGetter().apply(configuredValueSet),
                is((Object)valueSet1));
    }

    @Test
    public void testGetValueGetter_tableContent() throws Exception {
        when(tableContentUsage.getTableContentName()).thenReturn(ANY_VALUE);

        assertThat(PropertyValueType.TABLE_CONTENT_USAGE.getValueGetter().apply(tableContentUsage),
                is((Object)ANY_VALUE));
    }

    @Test
    public void testGetValueGetter_formula() throws Exception {
        when(formula.getExpression()).thenReturn(ANY_VALUE);

        assertThat(PropertyValueType.FORMULA.getValueGetter().apply(formula), is((Object)ANY_VALUE));
    }

    @Test
    public void testGetValueGetter_ruleConfig() throws Exception {
        when(ruleConfig.isActive()).thenReturn(true);

        assertThat(PropertyValueType.VALIDATION_RULE_CONFIG.getValueGetter().apply(ruleConfig), is((Object)true));
    }

    @Test
    public void testGetValueSetter_AttributeValue() throws Exception {
        IValueHolder<?> value = mock(IValueHolder.class);
        IValueHolder<?> copy = mock(IValueHolder.class);
        doReturn(copy).when(value).copy(attributeValue);

        PropertyValueType.ATTRIBUTE_VALUE.getValueSetter().accept(attributeValue, value);
        verify(attributeValue).setValueHolder(copy);
    }

    @Test
    public void testGetValueSetter_TableContent() throws Exception {
        PropertyValueType.TABLE_CONTENT_USAGE.getValueSetter().accept(tableContentUsage, ANY_VALUE);
        verify(tableContentUsage).setTableContentName(ANY_VALUE);
    }

    @Test
    public void testGetValueSetter_TableContent_null() throws Exception {
        PropertyValueType.TABLE_CONTENT_USAGE.getValueSetter().accept(tableContentUsage, null);
        verify(tableContentUsage).setTableContentName(null);
    }

    @Test
    public void testGetValueSetter_Formula() throws Exception {
        PropertyValueType.FORMULA.getValueSetter().accept(formula, ANY_VALUE);
        verify(formula).setExpression(ANY_VALUE);
    }

    @Test
    public void testGetValueSetter_Formula_null() throws Exception {
        PropertyValueType.FORMULA.getValueSetter().accept(formula, null);
        verify(formula).setExpression(null);
    }

    @Test
    public void testGetValueSetter_ConfiguredDefault() throws Exception {

        PropertyValueType.CONFIGURED_DEFAULT.getValueSetter().accept(configuredDefault, "10");
        verify(configuredDefault).setValue("10");

        PropertyValueType.CONFIGURED_DEFAULT.getValueSetter().accept(configuredDefault, null);
        verify(configuredDefault).setValue(null);
    }

    @Test
    public void testGetValueSetter_ConfigElement() throws Exception {
        IValueSet value = mock(IValueSet.class);
        IValueSet copy = mock(IValueSet.class);
        when(value.copy(configuredValueSet, ANY_VALUE)).thenReturn(copy);
        IIpsModel ipsModel = mock(IIpsModel.class);
        when(configuredValueSet.getIpsModel()).thenReturn(ipsModel);
        when(ipsModel.getNextPartId(configuredValueSet)).thenReturn(ANY_VALUE);

        PropertyValueType.CONFIGURED_VALUESET.getValueSetter().accept(configuredValueSet, value);
        verify(configuredValueSet).setValueSet(copy);
    }

    @Test
    public void testGetValueSetter_RuleConfig() throws Exception {
        PropertyValueType.VALIDATION_RULE_CONFIG.getValueSetter().accept(ruleConfig, true);
        verify(ruleConfig).setActive(true);
    }

    @Test
    public void testGetValueGetterInternal_attributeValue() throws Exception {
        AttributeValue attributeValue = mock(AttributeValue.class);
        IValueHolder<?> value = mock(IValueHolder.class);
        doReturn(value).when(attributeValue).getValueHolderInternal();

        assertThat(PropertyValueType.ATTRIBUTE_VALUE.getInternalValueGetter().apply(attributeValue), is((Object)value));
    }

    @Test
    public void testGetValueGetterInternal_configuredValueSet() throws Exception {
        ConfiguredValueSet configuredValueSet = mock(ConfiguredValueSet.class);
        doReturn(valueSet1).when(configuredValueSet).getValueSetInternal();

        assertThat(PropertyValueType.CONFIGURED_VALUESET.getInternalValueGetter().apply(configuredValueSet),
                is((Object)valueSet1));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCopyValue() {
        IAttributeValue attributeValue2 = mock(IAttributeValue.class);
        IValueHolder<?> value = mock(IValueHolder.class);
        IValueHolder<?> valueCopy = mock(IValueHolder.class);
        when(value.copy(attributeValue2)).thenReturn((IValueHolder)valueCopy);
        doReturn(value).when(attributeValue).getValueHolder();

        PropertyValueType.ATTRIBUTE_VALUE.copyValue(attributeValue, attributeValue2);

        verify(attributeValue2).setValueHolder(valueCopy);
    }

}
