/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class DateValueConverterTest {
    private MessageList ml;
    private DateValueConverter converter;
    private ITableFormat tableFormat;

    @Before
    public void setUp() throws Exception {
        ml = new MessageList();
        tableFormat = new CSVTableFormat();
        converter = new DateValueConverter();
        tableFormat.addValueConverter(converter);
    }

    @Test
    public void testGetIpsValueUsingCustomDateFormat() {
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        String value = converter.getIpsValue("08.10.2009", ml);
        assertTrue(ml.toString(), ml.isEmpty());
        assertTrue(value, '-' == value.charAt(4) && '-' == value.charAt(7));

        // now use only the year field
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "yyyy");
        value = converter.getIpsValue("1999", ml);
        assertTrue(ml.isEmpty());

        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        value = converter.getIpsValue("xx01.10.2009", ml);
        assertFalse(ml.isEmpty());
    }

    @Test
    public void testGetExternalDataValue() {
        tableFormat.setProperty(CSVTableFormat.PROPERTY_DATE_FORMAT, "dd.MM.yyyy");
        Object extValue = converter.getExternalDataValue("2009-10-15", ml);
        assertTrue(extValue instanceof String);
        assertEquals("15.10.2009", extValue);
    }
}
