/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AttributeValueFormatterTest {
    private Locale formerLocale;

    @Before
    public void setUp() {
        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        formerLocale = ipsPreferences.getDatatypeFormattingLocale();
        ipsPreferences.setDatatypeFormattingLocale(Locale.GERMANY);
    }

    @After
    public void tearDown() {
        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        if (formerLocale != null) {
            ipsPreferences.setDatatypeFormattingLocale(formerLocale);
        }
    }

    @Test
    public void testGetFormattedValue_SingleValueDate() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        doReturn(new SingleValueHolder(attrValue, "2012-04-02")).when(attrValue).getValueHolder();

        AttributeValueFormatter formatter = new AttributeValueFormatter(attrValue, ValueDatatype.GREGORIAN_CALENDAR);
        assertEquals("02.04.2012", formatter.getFormattedValue());
    }

    @Test
    public void testGetFormattedValue_SingleValueDecimal() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        doReturn(new SingleValueHolder(attrValue, "1.23456")).when(attrValue).getValueHolder();

        AttributeValueFormatter formatter = new AttributeValueFormatter(attrValue, ValueDatatype.DECIMAL);
        assertEquals("1,23456", formatter.getFormattedValue());
    }

    @Test
    public void testGetFormattedValue_MultiValueDate() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        List<ISingleValueHolder> holderList = new ArrayList<>();
        holderList.add(new SingleValueHolder(attrValue, "2012-04-02"));
        holderList.add(new SingleValueHolder(attrValue, "1999-01-31"));
        doReturn(new MultiValueHolder(attrValue, holderList)).when(attrValue).getValueHolder();

        AttributeValueFormatter formatter = new AttributeValueFormatter(attrValue, ValueDatatype.GREGORIAN_CALENDAR);
        assertEquals("02.04.2012 | 31.01.1999", formatter.getFormattedValue());
    }

    @Test
    public void testGetFormattedValue_MultiValueDecimal() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        List<ISingleValueHolder> holderList = new ArrayList<>();
        holderList.add(new SingleValueHolder(attrValue, "1.23456"));
        holderList.add(new SingleValueHolder(attrValue, "23.42"));
        doReturn(new MultiValueHolder(attrValue, holderList)).when(attrValue).getValueHolder();

        AttributeValueFormatter formatter = new AttributeValueFormatter(attrValue, ValueDatatype.DECIMAL);
        assertEquals("1,23456 | 23,42", formatter.getFormattedValue());
    }

    @Test
    public void testConvertToString_EmptyBracketsOnEmptyList() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        AttributeValueFormatter formatter = new AttributeValueFormatter(attrValue, ValueDatatype.DECIMAL);
        String convertedString = formatter.convertToString(new ArrayList<String>());
        assertEquals(StringUtils.EMPTY, convertedString);
    }

    @Test
    public void testConvertToString() throws Exception {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        AttributeValueFormatter formatter = new AttributeValueFormatter(attrValue, ValueDatatype.STRING);
        assertEquals("", formatter.convertToString(Arrays.asList("")));
        assertEquals("a", formatter.convertToString(Arrays.asList("a")));
        assertEquals("a | b | c", formatter.convertToString(Arrays.asList("a", "b", "c")));
        assertEquals(" | b | c", formatter.convertToString(Arrays.asList("", "b", "c")));
        assertEquals("a | b | ", formatter.convertToString(Arrays.asList("a", "b", "")));
        assertEquals(" | b | ", formatter.convertToString(Arrays.asList("", "b", "")));
        assertEquals(" |  | ", formatter.convertToString(Arrays.asList("", "", "")));
    }

}
