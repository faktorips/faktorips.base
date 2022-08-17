/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.Locale;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.junit.Test;

public class ValueConverterTest {

    @Test
    public void testConversion() {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsProjectProperties properties = mock(IIpsProjectProperties.class);
        when(properties.getDefaultCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(ipsProject.getReadOnlyProperties()).thenReturn(properties);
        assertEquals("10.00 EUR", ValueConverter.TO_MONEY.convert("10.00", ipsProject));
        assertEquals("10.123456", ValueConverter.TO_MONEY.convert("10.123456", ipsProject));
        assertEquals("10.00 EUR", ValueConverter.TO_MONEY.convert("10", ipsProject));
        assertEquals("", ValueConverter.TO_MONEY.convert("", ipsProject));
        assertEquals("10 11 ", ValueConverter.TO_MONEY.convert("10 11 ", ipsProject));

        when(properties.getDefaultCurrency()).thenReturn(Currency.getInstance("USD"));
        assertEquals("10.00 USD", ValueConverter.TO_MONEY.convert("10.00", ipsProject));
        assertEquals("10.123456", ValueConverter.TO_MONEY.convert("10.123456", ipsProject));
        assertEquals("10.00 USD", ValueConverter.TO_MONEY.convert("10", ipsProject));
        assertEquals("", ValueConverter.TO_MONEY.convert("", ipsProject));
        assertEquals("10 11 ", ValueConverter.TO_MONEY.convert("10 11 ", ipsProject));

        // Test with ¥, because it has no digits after the decimal point
        when(properties.getDefaultCurrency()).thenReturn(Currency.getInstance(Locale.JAPAN));
        assertEquals("10.00", ValueConverter.TO_MONEY.convert("10.00", ipsProject));
        assertEquals("10.123456", ValueConverter.TO_MONEY.convert("10.123456", ipsProject));
        assertEquals("10 JPY", ValueConverter.TO_MONEY.convert("10", ipsProject));
        assertEquals("", ValueConverter.TO_MONEY.convert("", ipsProject));
        assertEquals("10 11 ", ValueConverter.TO_MONEY.convert("10 11 ", ipsProject));
    }
}
