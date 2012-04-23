/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValueHolderToFormattedStringWrapperTest {
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
    public void returnFormattedValueIfSingleValue_Date() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        doReturn(new SingleValueHolder(attrValue, "2012-04-02")).when(attrValue).getValueHolder();

        ValueHolderToFormattedStringWrapper wrapper = new ValueHolderToFormattedStringWrapper(attrValue, false,
                ValueDatatype.GREGORIAN_CALENDAR);
        assertEquals("02.04.2012", wrapper.getFormattedValue());
    }

    @Test
    public void returnFormattedValueIfSingleValue_Decimal() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        doReturn(new SingleValueHolder(attrValue, "1.23456")).when(attrValue).getValueHolder();

        ValueHolderToFormattedStringWrapper wrapper = new ValueHolderToFormattedStringWrapper(attrValue, false,
                ValueDatatype.DECIMAL);
        assertEquals("1,23456", wrapper.getFormattedValue());
    }

    @Test
    public void returnFormattedValueIfMultiValue_Date() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        List<SingleValueHolder> holderList = new ArrayList<SingleValueHolder>();
        holderList.add(new SingleValueHolder(attrValue, "2012-04-02"));
        holderList.add(new SingleValueHolder(attrValue, "1999-01-31"));
        doReturn(new MultiValueHolder(attrValue, holderList)).when(attrValue).getValueHolder();

        ValueHolderToFormattedStringWrapper wrapper = new ValueHolderToFormattedStringWrapper(attrValue, true,
                ValueDatatype.GREGORIAN_CALENDAR);
        assertEquals("[02.04.2012 | 31.01.1999]", wrapper.getFormattedValue());
    }

    @Test
    public void returnFormattedValueIfMultiValue_Decimal() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        List<SingleValueHolder> holderList = new ArrayList<SingleValueHolder>();
        holderList.add(new SingleValueHolder(attrValue, "1.23456"));
        holderList.add(new SingleValueHolder(attrValue, "23.42"));
        doReturn(new MultiValueHolder(attrValue, holderList)).when(attrValue).getValueHolder();

        ValueHolderToFormattedStringWrapper wrapper = new ValueHolderToFormattedStringWrapper(attrValue, true,
                ValueDatatype.DECIMAL);
        assertEquals("[1,23456 | 23,42]", wrapper.getFormattedValue());
    }

    @Test
    public void returnEmptyBracketsOnEmptyList() {
        IAttributeValue attrValue = mock(IAttributeValue.class);
        ValueHolderToFormattedStringWrapper wrapper = new ValueHolderToFormattedStringWrapper(attrValue, true,
                ValueDatatype.DECIMAL);
        String convertedString = wrapper.convertToString(new ArrayList<String>());
        assertEquals("[]", convertedString);
    }
}
