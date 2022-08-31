/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import org.faktorips.datatype.classtypes.DateDatatype;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Peter Erzberger
 */
public class DateDatatypeTest {

    private DateDatatype datatype;

    @Before
    public void setUp() {
        datatype = new DateDatatype();
    }

    @Test
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

    @Test
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

    @Test
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
