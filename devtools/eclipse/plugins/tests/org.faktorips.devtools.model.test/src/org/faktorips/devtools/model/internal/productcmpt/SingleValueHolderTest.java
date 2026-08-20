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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class SingleValueHolderTest {


    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IAttributeValue attributeValue;

    private MockitoSession mockito;

    private ValueDatatype datatype = ValueDatatype.STRING;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        lenient().when(attributeValue.getIpsProject()).thenReturn(ipsProject);
        lenient().when(attributeValue.findAttribute(ipsProject)).thenReturn(attribute);
        lenient().when(attribute.findValueDatatype(ipsProject)).thenReturn(datatype);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
