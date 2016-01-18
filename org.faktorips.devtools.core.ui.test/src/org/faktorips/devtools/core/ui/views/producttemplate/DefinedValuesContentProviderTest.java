/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.ui.views.producttemplate.DefinedValuesContentProvider.ValueViewItem;
import org.faktorips.devtools.core.util.Histogram;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefinedValuesContentProviderTest {

    @Mock
    private Viewer viewer;
    @Mock
    private TemplatePropertyUsagePmo pmo;
    @Mock
    private IPropertyValue value1;
    @Mock
    private IPropertyValue value2;
    @Mock
    private IPropertyValue value3;
    @Mock
    private IPropertyValue value4;
    @Mock
    private IPropertyValue value5;
    @Mock
    private IPropertyValue value6;

    private DefinedValuesContentProvider definedValuesContentProvider;
    private Histogram<Object, IPropertyValue> histogram;

    @Before
    public void setUp() {
        when(value1.getPropertyValue()).thenReturn("B");
        when(value2.getPropertyValue()).thenReturn("A");
        when(value3.getPropertyValue()).thenReturn("B");
        when(value4.getPropertyValue()).thenReturn("A");
        when(value5.getPropertyValue()).thenReturn("C");
        when(value6.getPropertyValue()).thenReturn("A");
        histogram = new Histogram<Object, IPropertyValue>(getValueFunction(), Lists.newArrayList(value1, value2,
                value3, value4, value5, value6));
        when(pmo.getDefinedValuesHistogram()).thenReturn(histogram);
        when(pmo.getDefinedAbsoluteDistribution()).thenReturn(histogram.getAbsoluteDistribution());
        when(pmo.getCount()).thenReturn(6);

        definedValuesContentProvider = new DefinedValuesContentProvider();
        definedValuesContentProvider.inputChanged(viewer, null, pmo);
    }

    private Function<IPropertyValue, Object> getValueFunction() {
        return new Function<IPropertyValue, Object>() {
            @Override
            public Object apply(IPropertyValue p) {
                return p.getPropertyValue();
            }
        };
    }

    @Test
    public void testGetElements() {
        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        ValueViewItem item1 = (ValueViewItem)elements[0];
        ValueViewItem item2 = (ValueViewItem)elements[1];
        ValueViewItem item3 = (ValueViewItem)elements[2];

        assertEquals(item1.getValue(), "A");
        assertEquals(item2.getValue(), "B");
        assertEquals(item3.getValue(), "C");
    }

    @Test
    public void getChildren() {
        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        ValueViewItem item1 = (ValueViewItem)elements[0];
        ValueViewItem item2 = (ValueViewItem)elements[1];
        ValueViewItem item3 = (ValueViewItem)elements[2];

        assertThat(item1.getChildren(), hasItems(value2, value4, value6));
        assertThat(item2.getChildren(), hasItems(value1, value3));
        assertThat(item3.getChildren(), hasItems(value5));
    }
}
