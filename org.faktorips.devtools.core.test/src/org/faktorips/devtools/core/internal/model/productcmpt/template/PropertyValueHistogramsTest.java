/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.util.Histogram;
import org.faktorips.values.Decimal;
import org.junit.Test;

public class PropertyValueHistogramsTest {

    private static final List<String> CMPT_PROPERTIES = Arrays.asList("p1", "p2", "p3", "p4", "p5");
    private static final List<String> GEN_PROPERTIES = Arrays.asList("g1", "g2", "g3", "g4", "g5");

    @Test
    public void testCreateForSingleValue() throws Exception {
        List<IProductCmpt> list = Arrays.asList(mockProductCmpt());

        PropertyValueHistograms valueHistograms = PropertyValueHistograms.createFor(list);

        for (String name : CMPT_PROPERTIES) {
            Histogram<Object, IPropertyValue> histogram = valueHistograms.get(name);
            assertThat(histogram.getAbsoluteDistribution().get(name + "value"), is(1));
            assertThat(histogram.getRelativeDistribution().get(name + "value"), is(Decimal.valueOf(1)));
        }
    }

    @Test
    public void testCreateFor() throws Exception {
        List<IProductCmpt> list = Arrays.asList(mockProductCmpt(), mockProductCmpt(), mockProductCmpt());

        PropertyValueHistograms valueHistograms = PropertyValueHistograms.createFor(list);

        for (String name : CMPT_PROPERTIES) {
            Histogram<Object, IPropertyValue> histogram = valueHistograms.get(name);
            assertThat(histogram.getAbsoluteDistribution().get(name + "value"), is(3));
            assertThat(histogram.getRelativeDistribution().get(name + "value"), is(Decimal.valueOf(1)));
        }
    }

    @Test
    public void testCreateFor_Generation() throws Exception {
        List<IProductCmpt> list = Arrays.asList(mockProductCmptWithGenerationProperties());

        PropertyValueHistograms valueHistograms = PropertyValueHistograms.createFor(list);

        for (String name : CMPT_PROPERTIES) {
            Histogram<Object, IPropertyValue> histogram = valueHistograms.get(name);
            assertThat(histogram.getAbsoluteDistribution().get(name + "value"), is(1));
            assertThat(histogram.getRelativeDistribution().get(name + "value"), is(Decimal.valueOf(1)));
        }
        for (String name : GEN_PROPERTIES) {
            Histogram<Object, IPropertyValue> histogram = valueHistograms.get(name);
            assertThat(histogram.getAbsoluteDistribution().get(name + "value"), is(1));
            assertThat(histogram.getRelativeDistribution().get(name + "value"), is(Decimal.valueOf(1)));
        }
    }

    private IProductCmpt mockProductCmpt() {
        IProductCmpt productCmpt = mock(IProductCmpt.class);
        addMockedProperties(productCmpt, CMPT_PROPERTIES);
        IProductCmptGeneration productCmptGeneration = mock(IProductCmptGeneration.class);
        when(productCmpt.getLatestProductCmptGeneration()).thenReturn(productCmptGeneration);
        return productCmpt;
    }

    private IProductCmpt mockProductCmptWithGenerationProperties() {
        IProductCmpt productCmpt = mockProductCmpt();
        IProductCmptGeneration productCmptGeneration = mock(IProductCmptGeneration.class);
        addMockedProperties(productCmptGeneration, GEN_PROPERTIES);
        when(productCmpt.getLatestProductCmptGeneration()).thenReturn(productCmptGeneration);
        return productCmpt;
    }

    private void addMockedProperties(IPropertyValueContainer mockedContainer, List<String> propertyNames) {
        List<IPropertyValue> values = new ArrayList<IPropertyValue>();
        for (String name : propertyNames) {
            ITableContentUsage propertyValue = mock(ITableContentUsage.class);
            when(propertyValue.getPropertyName()).thenReturn(name);
            when(propertyValue.getPropertyValueType()).thenReturn(PropertyValueType.TABLE_CONTENT_USAGE);
            when(propertyValue.getTableContentName()).thenReturn(name + "value");
            values.add(propertyValue);
        }
        when(mockedContainer.getAllPropertyValues()).thenReturn(values);
    }
}
