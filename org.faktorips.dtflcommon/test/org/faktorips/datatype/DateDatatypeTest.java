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

    private DateDatatype dataType;
    
    public void setUp(){
        dataType = new DateDatatype();
    }
    
    public void testGetValue(){
        
        String valueStr = "2000-01-01";
        Date date = (Date)dataType.getValue(valueStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2000, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DATE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
        
        assertNull(dataType.getValue(null));
        
        try{
            dataType.getValue("01.01.2000");
            fail();
        }
        catch(Exception e){
        }
    }
    
    public void testValueToString(){
        
        Calendar cal = Calendar.getInstance();
        cal.set(2000, Calendar.FEBRUARY, 20);
        String valueStr = dataType.valueToString(cal.getTime());
        String[] tokens = valueStr.split("-");
        assertEquals(3, tokens.length);
        assertEquals(tokens[0], "2000");
        assertEquals(tokens[1], "02");
        assertEquals(tokens[2], "20");
        
        assertNull(dataType.valueToString(null));
        
        try{
            dataType.valueToString(cal);
            fail();
        }
        catch(Exception e){
        }
    }
}
