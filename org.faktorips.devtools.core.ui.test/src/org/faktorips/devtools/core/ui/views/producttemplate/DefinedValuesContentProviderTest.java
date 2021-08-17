/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import com.google.common.collect.Lists;

import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.model.internal.util.Histogram;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
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
    private IPropertyValue valueA1;
    @Mock
    private IPropertyValue valueA2;
    @Mock
    private IPropertyValue valueA3;
    @Mock
    private IPropertyValue valueB1;
    @Mock
    private IPropertyValue valueB2;
    @Mock
    private IPropertyValue valueC;

    private DefinedValuesContentProvider definedValuesContentProvider;
    private Histogram<Object, ITemplatedValue> histogram;

    @Before
    public void setUp() {
        when(valueA1.getPropertyValue()).thenReturn("A");
        when(valueA2.getPropertyValue()).thenReturn("A");
        when(valueA3.getPropertyValue()).thenReturn("A");
        when(valueB1.getPropertyValue()).thenReturn("B");
        when(valueB2.getPropertyValue()).thenReturn("B");
        when(valueC.getPropertyValue()).thenReturn("C");

        histogram = new Histogram<>(getValueFunction(),
                Lists.<ITemplatedValue> newArrayList(valueB1, valueA1, valueB2, valueA2, valueC, valueA3));
        when(pmo.hasData()).thenReturn(true);
        when(pmo.getDefinedValuesHistogram()).thenReturn(histogram);
        when(pmo.getCount()).thenReturn(6);
        when(pmo.getValueComparator()).thenReturn(PropertyValueType.FORMULA.getValueComparator());

        definedValuesContentProvider = new DefinedValuesContentProvider();
        definedValuesContentProvider.inputChanged(viewer, null, pmo);
    }

    private Function<ITemplatedValue, Object> getValueFunction() {
        return templatedValue -> ((IPropertyValue)templatedValue).getPropertyValue();
    }

    @Test
    public void testGetElements() {
        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        TemplateUsageViewItem item1 = (TemplateUsageViewItem)elements[0];
        TemplateUsageViewItem item2 = (TemplateUsageViewItem)elements[1];
        TemplateUsageViewItem item3 = (TemplateUsageViewItem)elements[2];

        assertEquals(item1.getValue(), "A");
        assertEquals(item2.getValue(), "B");
        assertEquals(item3.getValue(), "C");
    }

    @Test
    public void getChildren() {
        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        TemplateUsageViewItem item1 = (TemplateUsageViewItem)elements[0];
        TemplateUsageViewItem item2 = (TemplateUsageViewItem)elements[1];
        TemplateUsageViewItem item3 = (TemplateUsageViewItem)elements[2];

        assertThat(item1.getChildren(), hasItems((ITemplatedValue)valueA1, valueA2, valueA3));
        assertThat(item2.getChildren(), hasItems((ITemplatedValue)valueB1, valueB2));
        assertThat(item3.getChildren(), hasItems((ITemplatedValue)valueC));
    }

    @Test
    public void getChildren_ItemsWithSameValueAsTemplateAreSortedAtTheBeginning() {
        when(pmo.getActualTemplateValue()).thenReturn("C");
        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        TemplateUsageViewItem item1 = (TemplateUsageViewItem)elements[0];
        TemplateUsageViewItem item2 = (TemplateUsageViewItem)elements[1];
        TemplateUsageViewItem item3 = (TemplateUsageViewItem)elements[2];

        assertThat(item1.getChildren(), hasItems((ITemplatedValue)valueC));
        assertThat(item2.getChildren(), hasItems((ITemplatedValue)valueA1, valueA2, valueA3));
        assertThat(item3.getChildren(), hasItems((ITemplatedValue)valueB1, valueB2));
    }

    @Test
    public void getChildren_DeletedItemsAreSortedAtTheBeginning() {
        when(valueC.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        TemplateUsageViewItem item1 = (TemplateUsageViewItem)elements[0];
        TemplateUsageViewItem item2 = (TemplateUsageViewItem)elements[1];
        TemplateUsageViewItem item3 = (TemplateUsageViewItem)elements[2];

        assertThat(item1.getChildren(), hasItems((ITemplatedValue)valueC));
        assertThat(item2.getChildren(), hasItems((ITemplatedValue)valueA1, valueA2, valueA3));
        assertThat(item3.getChildren(), hasItems((ITemplatedValue)valueB1, valueB2));
    }

    @Test
    public void getChildren_DeletedItemsAreSortedAfterItemsWithSameValue() {
        // C is the same value as in the template and should be sorted at the top
        when(pmo.getActualTemplateValue()).thenReturn("C");
        // valueB1 and valueB2 are UNDEFINED and should be sorted at the top below the items with
        // the same value as the template
        when(valueB1.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(valueB2.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);

        Object[] elements = definedValuesContentProvider.getElements(null);
        assertThat(elements.length, is(3));
        TemplateUsageViewItem item1 = (TemplateUsageViewItem)elements[0];
        TemplateUsageViewItem item2 = (TemplateUsageViewItem)elements[1];
        TemplateUsageViewItem item3 = (TemplateUsageViewItem)elements[2];

        assertThat(item1.getChildren(), hasItems((ITemplatedValue)valueC));
        assertThat(item2.getChildren(), hasItems((ITemplatedValue)valueB1, valueB2));
        assertThat(item3.getChildren(), hasItems((ITemplatedValue)valueA1, valueA2, valueA3));
    }
}
