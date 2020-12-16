/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SingleValueHolderTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IAttributeValue attributeValue;

    private ValueDatatype datatype = ValueDatatype.STRING;

    @Before
    public void setUp() {
        when(attributeValue.getIpsProject()).thenReturn(ipsProject);
        when(attributeValue.findAttribute(ipsProject)).thenReturn(attribute);
        when(attribute.findValueDatatype(ipsProject)).thenReturn(datatype);
    }

    @Test
    public void testGetValueType() {
        SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, "abc");
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new StringValue("abc"));
        assertEquals(ValueType.STRING, singleValueHolder.getValueType());

        singleValueHolder = new SingleValueHolder(attributeValue, new InternationalStringValue());
        assertEquals(ValueType.INTERNATIONAL_STRING, singleValueHolder.getValueType());
    }

    @Test
    public void testCompareTo_null() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (IValue<?>)null);

        assertTrue(v1.compareTo(null) < 0);
    }

    @Test
    public void testCompareTo_null_eq_null() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (IValue<?>)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (IValue<?>)null);

        assertThat(v1.compareTo(v2), is(0));
    }

    @Test
    public void testCompareTo_nullContent_eq_nullContent() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (String)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (String)null);

        assertThat(v1.compareTo(v2), is(0));
        assertThat(v2.compareTo(v1), is(0));
    }

    @Test
    public void testCompareTo_null_lt_any() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (IValue<?>)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareTo_nullContent_lt_any() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, (String)null);
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareTo_any_lt_null() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (IValue<?>)null);

        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v2.compareTo(v1) < 0);
    }

    @Test
    public void testCompareTo_any_lt_nullContent() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, (String)null);

        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v2.compareTo(v1) < 0);
    }

    @Test
    public void testCompareTo_eq() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) == 0);
        assertTrue(v2.compareTo(v1) == 0);
    }

    @Test
    public void testCompareTo_lt() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asd");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asx");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareTo_gt() throws Exception {
        SingleValueHolder v1 = new SingleValueHolder(attributeValue, "asx");
        SingleValueHolder v2 = new SingleValueHolder(attributeValue, "asd");

        assertTrue(v1.compareTo(v2) > 0);
        assertTrue(v2.compareTo(v1) < 0);
    }

}
