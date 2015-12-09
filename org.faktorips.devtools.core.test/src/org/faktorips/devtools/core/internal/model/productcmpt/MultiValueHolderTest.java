/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder.Factory;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.junit.Test;

public class MultiValueHolderTest {

    @Test
    public void testSplitMultiDefaultValues_null() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue(null);

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(0, splitMultiDefaultValues.size());
    }

    @Test
    public void testSplitMultiDefaultValues_empty() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("");

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(1, splitMultiDefaultValues.size());
        assertEquals("", splitMultiDefaultValues.get(0).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_singleValue() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("xyz");

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(1, splitMultiDefaultValues.size());
        assertEquals("xyz", splitMultiDefaultValues.get(0).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_multipleValues() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("abc|xyz  | 123");

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(3, splitMultiDefaultValues.size());
        assertEquals("abc", splitMultiDefaultValues.get(0).getStringValue());
        assertEquals("xyz", splitMultiDefaultValues.get(1).getStringValue());
        assertEquals("123", splitMultiDefaultValues.get(2).getStringValue());
    }

    @Test
    public void testGetSplitMultiValue_empty() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("");

        assertEquals(1, multiValue.length);
        assertEquals("", multiValue[0]);
    }

    @Test
    public void testGetSplitMultiValue_singleValue() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("abc123");

        assertEquals(1, multiValue.length);
        assertEquals("abc123", multiValue[0]);
    }

    @Test
    public void testGetSplitMultiValue_multipleValues() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("abc123|xyz |  123");

        assertEquals(3, multiValue.length);
        assertEquals("abc123", multiValue[0]);
        assertEquals("xyz", multiValue[1]);
        assertEquals("123", multiValue[2]);
    }

    @Test
    public void testGetValue() {
        IAttributeValue parent = mock(IAttributeValue.class);
        MultiValueHolder multiValueHolder = spy(new MultiValueHolder(parent));
        doNothing().when(multiValueHolder).objectHasChanged(anyObject(), anyObject());

        multiValueHolder.setValue(null);
        assertThat(multiValueHolder.getValue(), is(notNullValue()));
        assertThat(multiValueHolder.getValue().isEmpty(), is(true));

        multiValueHolder.setValue(new ArrayList<SingleValueHolder>());
        assertThat(multiValueHolder.getValue(), is(notNullValue()));
        assertThat(multiValueHolder.getValue().isEmpty(), is(true));

        SingleValueHolder valueHolder1 = new SingleValueHolder(parent);
        SingleValueHolder valueHolder2 = new SingleValueHolder(parent);
        multiValueHolder.setValue(Lists.newArrayList(valueHolder1, valueHolder2));
        assertThat(multiValueHolder.getValue().size(), is(2));
        assertThat(multiValueHolder.getValue(), hasItems(valueHolder1, valueHolder2));
    }

}
