package org.faktorips.datatype.classtypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.faktorips.datatype.ValueClassDatatype;


/**
 * A Datatype for the <code>java.util.Date </code> class. The string representation supported by this
 * datatype is <i>yyyy-MM-dd</i>.
 * 
 * @author Peter Erzberger
 */
public class DateDatatype extends ValueClassDatatype {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Creates a new DateDatatype where the name is the short class name.
     */
    public DateDatatype() {
        super(Date.class);
    }

    /**
     * Creates a new DateDatatype with the specified name.
     */
    public DateDatatype(String name) {
        super(Date.class, name);
    }
    
    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#getValue(java.lang.String)
     */
    public Object getValue(String value) {
        
        if(value == null){
            return null;
        }
        
        try{
            return formatter.parse(value);
        }
        catch(ParseException e){
            IllegalArgumentException ill = new IllegalArgumentException(
                    "Unable to convert the provided string parameter: \"" + value + "\"  into a " + 
                    Date.class + " instance");
            ill.initCause(e);
            throw ill;
        }
    }

    /**
     * Overridden Method.
     *
     * @see org.faktorips.datatype.ValueDatatype#valueToString(java.lang.Object)
     */
    public String valueToString(Object value) {
        
        if(value == null){
            return null;
        }
        return formatter.format(value);
    }
    
    /**
     * Calls the <code>getValue(String)</code> method and casts the result to a Date. 
     */
    public Date getDateValue(String value) {
        return (Date) getValue(value);
    }
}
