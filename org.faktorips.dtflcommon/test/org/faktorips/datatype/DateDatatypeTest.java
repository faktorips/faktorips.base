/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;
import java.util.Calendar;
import java.util.Date;

import org.faktorips.datatype.classtypes.DateDatatype;

import junit.framework.TestCase;



/**
 * 
 * @author Peter Erzberger
 */
public class DateDatatypeTest extends TestCase {

    private DateDatatype datatype;
    
    public void setUp(){
        datatype = new DateDatatype();
    }
    
    public void testGetValue(){
        String valueStr = "2000-01-01";
        Date date = (Date)datatype.getValue(valueStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2000, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DATE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        
        assertNull(datatype.getValue(null));
        
        try{
            datatype.getValue("01.01.2000");
            fail();
        }
        catch(Exception e){
        }
    }
    
    public void testIsParsable() {
        assertTrue(datatype.isParsable("2000-01-01"));
        assertTrue(datatype.isParsable(null));
        assertFalse(datatype.isParsable("2000.01.01"));
        assertFalse(datatype.isParsable("01-01-2001"));
        
    }
    
    public void testValueToString(){
        
        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.FEBRUARY, 20);
        String valueStr = datatype.valueToString(cal.getTime());
        String[] tokens = valueStr.split("-");
        assertEquals(3, tokens.length);
        assertEquals(tokens[0], "2000");
        assertEquals(tokens[1], "02");
        assertEquals(tokens[2], "20");
        
        assertNull(datatype.valueToString(null));
        
        try{
            datatype.valueToString(cal);
            fail();
        }
        catch(Exception e){
        }
    }
}
