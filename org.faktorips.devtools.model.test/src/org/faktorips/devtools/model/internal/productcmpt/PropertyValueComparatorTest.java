/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValueComparatorTest {

    @Mock
    private IPropertyValue value1;

    @Mock
    private IPropertyValue value2;

    private PropertyValueComparator propertyValueComparator = new PropertyValueComparator();

    @Test
    public void testCompare_DifferentTypes() {
        when(value1.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE);
        when(value2.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);

        assertThat(propertyValueComparator.compare(value1, value2), lt(0));
        assertThat(propertyValueComparator.compare(value2, value1), gt(0));
    }

    @Test
    public void testCompare_SameType_DifferentName() {
        when(value1.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        when(value1.getPropertyName()).thenReturn("a1");
        when(value2.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        when(value2.getPropertyName()).thenReturn("a2");

        assertThat(propertyValueComparator.compare(value1, value2), lt(0));
        assertThat(propertyValueComparator.compare(value2, value1), gt(0));
    }

    @Test
    public void testCompare_SameType_SameName_DifferentValueType() {
        when(value1.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        when(value1.getPropertyName()).thenReturn("a1");
        when(value1.getPropertyValueType()).thenReturn(PropertyValueType.CONFIGURED_VALUESET);
        when(value2.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        when(value2.getPropertyName()).thenReturn("a2");
        when(value2.getPropertyValueType()).thenReturn(PropertyValueType.CONFIGURED_DEFAULT);

        assertThat(propertyValueComparator.compare(value1, value2), lt(0));
        assertThat(propertyValueComparator.compare(value2, value1), gt(0));
    }

    public static Matcher<Integer> lt(final int i) {
        return new TypeSafeMatcher<>(Integer.class) {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("an integer less than " + i);
            }

            @Override
            protected boolean matchesSafely(Integer arg0) {
                return arg0 < i;
            }
        };
    }

    public static Matcher<Integer> gt(final int i) {
        return new TypeSafeMatcher<>(Integer.class) {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("an integer greater than " + i);
            }

            @Override
            protected boolean matchesSafely(Integer arg0) {
                return arg0 > i;
            }
        };
    }

}
