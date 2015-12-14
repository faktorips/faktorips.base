/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;

import com.google.common.base.Function;

import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValueTypeTest {

    private static final String ANY_VALUE = "anyValue";

    @Mock
    private IValueSet valueSet1;

    @Mock
    private IValueSet valueSet2;

    @Mock
    private IAttributeValue attributeValue;

    @Mock
    private IConfigElement configElement;

    @Mock
    private ITableContentUsage tableContentUsage;

    @Mock
    private IFormula formula;

    @Mock
    private IValidationRuleConfig ruleConfig;

    @Test
    public void testGetValueComparator_valueSet_eq() throws Exception {
        when(valueSet1.containsValueSet(valueSet2)).thenReturn(true);
        when(valueSet2.containsValueSet(valueSet1)).thenReturn(true);

        Comparator<Object> valueComparator = PropertyValueType.CONFIG_ELEMENT.getValueComparator();
        assertThat(valueComparator.compare(valueSet1, valueSet2), is(0));
    }

    @Test
    public void testGetValueComparator_valueSet_gt() throws Exception {
        when(valueSet1.containsValueSet(valueSet2)).thenReturn(true);

        Comparator<Object> valueComparator = PropertyValueType.CONFIG_ELEMENT.getValueComparator();
        assertThat(valueComparator.compare(valueSet1, valueSet2), is(1));
    }

    @Test
    public void testGetValueComparator_valueSet_lt() throws Exception {
        when(valueSet2.containsValueSet(valueSet1)).thenReturn(true);

        Comparator<Object> valueComparator = PropertyValueType.CONFIG_ELEMENT.getValueComparator();
        assertThat(valueComparator.compare(valueSet1, valueSet2), is(-1));
    }

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
    public void testGetValueFunction_attributeValue() throws Exception {
        IValueHolder<?> value = mock(IValueHolder.class);
        doReturn(value).when(attributeValue).getValueHolder();

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.ATTRIBUTE_VALUE.getValueFunction();
        assertThat(valueFunction.apply(attributeValue), is((Object)value));
    }

    @Test
    public void testGetValueFunction_configElement() throws Exception {
        when(configElement.getValueSet()).thenReturn(valueSet1);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.CONFIG_ELEMENT.getValueFunction();
        assertThat(valueFunction.apply(configElement), is((Object)valueSet1));
    }

    @Test
    public void testGetValueFunction_tableContent() throws Exception {
        when(tableContentUsage.getTableContentName()).thenReturn(ANY_VALUE);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.TABLE_CONTENT_USAGE.getValueFunction();
        assertThat(valueFunction.apply(tableContentUsage), is((Object)ANY_VALUE));
    }

    @Test
    public void testGetValueFunction_formula() throws Exception {
        when(formula.getExpression()).thenReturn(ANY_VALUE);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.FORMULA.getValueFunction();
        assertThat(valueFunction.apply(formula), is((Object)ANY_VALUE));
    }

    @Test
    public void testGetValueFunction_ruleConfig() throws Exception {
        when(ruleConfig.isActive()).thenReturn(true);

        Function<IPropertyValue, Object> valueFunction = PropertyValueType.VALIDATION_RULE_CONFIG.getValueFunction();
        assertThat(valueFunction.apply(ruleConfig), is((Object)true));
    }

}
