/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.datatype;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.faktorips.datatype.classtypes.DateDatatype;

/**
 * 
 * @author Peter Erzberger
 */
public class DateDatatypeTest extends TestCase {

    private DateDatatype datatype;

    @Override
    public void setUp() {
        datatype = new DateDatatype();
    }

    public void testGetValue() {
        String valueStr = "2000-01-01"; //$NON-NLS-1$
        Date date = (Date)datatype.getValue(valueStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2000, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DATE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));

        assertNull(datatype.getValue(null));

        try {
            datatype.getValue("01.01.2000"); //$NON-NLS-1$
            fail();
        } catch (Exception e) {
            // Expected exception.
        }
    }

    public void testIsParsable() {
        assertTrue(datatype.isParsable("2000-01-01")); //$NON-NLS-1$
        assertTrue(datatype.isParsable("2000-01-01")); //$NON-NLS-1$
        assertTrue(datatype.isParsable(null));
        assertFalse(datatype.isParsable("2000.01.01")); //$NON-NLS-1$
        assertFalse(datatype.isParsable("01-01-2001")); //$NON-NLS-1$
        assertFalse(datatype.isParsable("2001-01-01 ")); //$NON-NLS-1$
        assertFalse(datatype.isParsable(" 2001-01-01")); //$NON-NLS-1$
        assertTrue(datatype.isParsable("2001-1-1")); //$NON-NLS-1$
        assertTrue(datatype.isParsable("2001-1-1")); //$NON-NLS-1$
    }

    public void testValueToString() {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.FEBRUARY, 20);
        String valueStr = datatype.valueToString(cal.getTime());
        String[] tokens = valueStr.split("-"); //$NON-NLS-1$
        assertEquals(3, tokens.length);
        assertEquals(tokens[0], "2000"); //$NON-NLS-1$
        assertEquals(tokens[1], "02"); //$NON-NLS-1$
        assertEquals(tokens[2], "20"); //$NON-NLS-1$

        assertNull(datatype.valueToString(null));

        try {
            datatype.valueToString(cal);
            fail();
        } catch (Exception e) {
            // Expected exception.
        }
    }

}
