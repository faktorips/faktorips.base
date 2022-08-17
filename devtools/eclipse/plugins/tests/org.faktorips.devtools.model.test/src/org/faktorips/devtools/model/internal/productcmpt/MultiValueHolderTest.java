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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder.Factory;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Test;

public class MultiValueHolderTest {

    private static final String NULL = IIpsModelExtensions.get().getModelPreferences().getNullPresentation();

    @Test
    public void testSplitMultiDefaultValues_null() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue(null);

        ArrayList<ISingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(0, splitMultiDefaultValues.size());
    }

    @Test
    public void testSplitMultiDefaultValues_nullRepresentation() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue(NULL);

        ArrayList<ISingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(1, splitMultiDefaultValues.size());
        assertEquals(null, splitMultiDefaultValues.get(0).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_empty() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("");

        ArrayList<ISingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(0, splitMultiDefaultValues.size());
    }

    @Test
    public void testSplitMultiDefaultValues_singleValue() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("xyz");

        ArrayList<ISingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(1, splitMultiDefaultValues.size());
        assertEquals("xyz", splitMultiDefaultValues.get(0).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_multipleValues() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("abc|xyz  | 123");

        ArrayList<ISingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(3, splitMultiDefaultValues.size());
        assertEquals("abc", splitMultiDefaultValues.get(0).getStringValue());
        assertEquals("xyz", splitMultiDefaultValues.get(1).getStringValue());
        assertEquals("123", splitMultiDefaultValues.get(2).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_multipleValuesWithNull() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("abc|<null>  | 123");

        ArrayList<ISingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(3, splitMultiDefaultValues.size());
        assertEquals("abc", splitMultiDefaultValues.get(0).getStringValue());
        assertEquals(null, splitMultiDefaultValues.get(1).getStringValue());
        assertEquals("123", splitMultiDefaultValues.get(2).getStringValue());
    }

    @Test
    public void testGetSplitMultiValue_empty() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("");

        assertEquals(0, multiValue.length);
    }

    @Test
    public void testGetSplitMultiValue_nullRepresentation() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue(NULL);

        assertEquals(1, multiValue.length);
        assertEquals(null, multiValue[0]);
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
    public void testGetSplitMultiValue_multipleValuesWithNull() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("abc123|" + NULL + " |  123");

        assertEquals(3, multiValue.length);
        assertEquals("abc123", multiValue[0]);
        assertEquals(null, multiValue[1]);
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

        multiValueHolder.setValue(new ArrayList<>());
        assertThat(multiValueHolder.getValue(), is(notNullValue()));
        assertThat(multiValueHolder.getValue().isEmpty(), is(true));

        SingleValueHolder valueHolder1 = new SingleValueHolder(parent);
        SingleValueHolder valueHolder2 = new SingleValueHolder(parent);
        multiValueHolder.setValue(Lists.newArrayList(valueHolder1, valueHolder2));
        assertThat(multiValueHolder.getValue().size(), is(2));
        assertThat(multiValueHolder.getValue(), hasItems(valueHolder1, valueHolder2));
    }

    @Test
    public void testCompareTo_String() {
        IAttributeValue attributeValue = mock(AttributeValue.class);
        IProductCmptTypeAttribute attribue = mock(IProductCmptTypeAttribute.class);
        when(attributeValue.findAttribute(any(IIpsProject.class))).thenReturn(attribue);
        when(attribue.findValueDatatype(any(IIpsProject.class))).thenReturn(ValueDatatype.STRING);
        SingleValueHolder a = new SingleValueHolder(attributeValue, "a");
        SingleValueHolder b = new SingleValueHolder(attributeValue, "b");
        SingleValueHolder c = new SingleValueHolder(attributeValue, "c");
        SingleValueHolder d = new SingleValueHolder(attributeValue, "d");

        MultiValueHolder empty = new MultiValueHolder(attributeValue, new ArrayList<>());

        MultiValueHolder abc1 = new MultiValueHolder(attributeValue, Lists.newArrayList(a, b, c));
        MultiValueHolder abc2 = new MultiValueHolder(attributeValue, Lists.newArrayList(a, b, c));

        MultiValueHolder cba = new MultiValueHolder(attributeValue, Lists.newArrayList(c, b, a));
        MultiValueHolder abcd = new MultiValueHolder(attributeValue, Lists.newArrayList(a, b, c, d));

        assertThat(empty.compareTo(empty), is(0));
        assertThat(empty.compareTo(null), is(1));
        assertThat(empty.compareTo(abc1), is(-1));
        assertThat(empty.compareTo(abc2), is(-1));
        assertThat(empty.compareTo(abcd), is(-1));
        assertThat(empty.compareTo(cba), is(-1));

        assertThat(abc1.compareTo(abc1), is(0));
        assertThat(abc1.compareTo(abc2), is(0));
        assertThat(abc2.compareTo(abc1), is(0));

        assertThat(abc1.compareTo(null), is(1));
        assertThat(abc1.compareTo(empty), is(1));
        assertThat(abc1.compareTo(cba), is(-2));
        assertThat(cba.compareTo(abc1), is(2));
        assertThat(abc1.compareTo(abcd), is(-1));
        assertThat(abcd.compareTo(abc1), is(1));
    }

    @Test
    public void testCompareTo_Integer() {
        IAttributeValue attributeValue = mock(AttributeValue.class);
        IProductCmptTypeAttribute attribue = mock(IProductCmptTypeAttribute.class);
        when(attributeValue.findAttribute(any(IIpsProject.class))).thenReturn(attribue);
        when(attribue.findValueDatatype(any(IIpsProject.class))).thenReturn(ValueDatatype.INTEGER);
        SingleValueHolder a = new SingleValueHolder(attributeValue, "1");
        SingleValueHolder b = new SingleValueHolder(attributeValue, "12");
        SingleValueHolder c = new SingleValueHolder(attributeValue, "33");
        SingleValueHolder d = new SingleValueHolder(attributeValue, "41");

        MultiValueHolder empty = new MultiValueHolder(attributeValue, new ArrayList<>());

        MultiValueHolder abc1 = new MultiValueHolder(attributeValue, Lists.newArrayList(a, b, c));
        MultiValueHolder abc2 = new MultiValueHolder(attributeValue, Lists.newArrayList(a, b, c));

        MultiValueHolder cba = new MultiValueHolder(attributeValue, Lists.newArrayList(c, b, a));
        MultiValueHolder abcd = new MultiValueHolder(attributeValue, Lists.newArrayList(a, b, c, d));

        assertThat(empty.compareTo(empty), is(0));
        assertThat(empty.compareTo(null), is(1));
        assertThat(empty.compareTo(abc1), is(-1));
        assertThat(empty.compareTo(abc2), is(-1));
        assertThat(empty.compareTo(abcd), is(-1));
        assertThat(empty.compareTo(cba), is(-1));

        assertThat(abc1.compareTo(abc1), is(0));
        assertThat(abc1.compareTo(abc2), is(0));
        assertThat(abc2.compareTo(abc1), is(0));

        assertThat(abc1.compareTo(null), is(1));
        assertThat(abc1.compareTo(empty), is(1));
        assertThat(abc1.compareTo(cba), is(-1));
        assertThat(cba.compareTo(abc1), is(1));
        assertThat(abc1.compareTo(abcd), is(-1));
        assertThat(abcd.compareTo(abc1), is(1));
    }

}
